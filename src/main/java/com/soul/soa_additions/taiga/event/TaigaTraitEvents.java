package com.soul.soa_additions.taiga.event;

import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Forge-bus handler for TAIGA trait side effects that don't fit a TC3 modifier
 * hook cleanly — mostly per-mob drop list mutations, XP scaling, and persistent
 * NBT updates keyed off living-death events.
 *
 * <p>Behaviours are ports of the event-bus chunks of the 1.12.2 trait classes
 * (TraitSlaughtering, TraitDissolving, TraitReviving, TraitAnalysing,
 * TraitHollow, TraitUnstable, TraitCurvature, TraitGarishly, TraitSoulEater,
 * TraitCongenial).</p>
 */
public final class TaigaTraitEvents {

    private static final Random RNG = new Random();

    private static final ModifierId SLAUGHTERING = id("slaughtering");
    private static final ModifierId DISSOLVING   = id("dissolving");
    private static final ModifierId REVIVE       = id("revive");
    private static final ModifierId ANALYSING    = id("analysing");
    private static final ModifierId HOLLOW       = id("hollow");
    private static final ModifierId UNSTABLE     = id("unstable");
    private static final ModifierId CURVATURE    = id("curvature");
    private static final ModifierId GARISHLY     = id("garishly");
    private static final ModifierId SOULEATER    = id("souleater");
    private static final ModifierId CONGENIAL    = id("congenial");

    private TaigaTraitEvents() {}

    private static ModifierId id(String name) { return new ModifierId(new ResourceLocation("taiga", name)); }

    private static ToolStack toolOf(Player p) {
        ItemStack stack = p.getMainHandItem();
        if (stack.isEmpty()) return null;
        try { return ToolStack.from(stack); } catch (RuntimeException e) { return null; }
    }

    private static boolean has(ToolStack tool, ModifierId mod) {
        return tool != null && tool.getModifiers().getLevel(mod) > 0;
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ToolStack tool = toolOf(player);
        if (tool == null) return;

        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        double x = victim.getX(), y = victim.getY(), z = victim.getZ();
        List<ItemEntity> drops = (List<ItemEntity>) event.getDrops();

        if (has(tool, SLAUGHTERING) && victim instanceof Mob && !(victim instanceof Player) && !drops.isEmpty()) {
            ItemStack base = drops.get(RNG.nextInt(drops.size())).getItem();
            ItemStack copy = base.copy();
            copy.setCount(RNG.nextInt(4) + 1);
            drops.add(new ItemEntity(level, x, y, z, copy));
        }

        if (has(tool, HOLLOW) && RNG.nextFloat() <= 0.9F && victim instanceof Monster) {
            drops.clear();
        }

        if (has(tool, ANALYSING) && RNG.nextFloat() < 0.1F && victim instanceof Monster) {
            drops.clear();
        }

        if (has(tool, UNSTABLE) && RNG.nextFloat() < 0.05F && victim instanceof Monster) {
            drops.add(0, new ItemEntity(level, x, y, z, new ItemStack(Items.GUNPOWDER, Math.max(1, RNG.nextInt(2)))));
        }

        if (has(tool, CURVATURE) && victim instanceof Monster) {
            drops.add(0, new ItemEntity(level, x, y, z, new ItemStack(Items.ENDER_PEARL, Math.max(1, RNG.nextInt(2)))));
        }

        if (has(tool, GARISHLY) && victim instanceof Monster) {
            int r = RNG.nextInt(3);
            ItemStack bonus = switch (r) {
                case 0 -> new ItemStack(Items.BLAZE_POWDER, Math.max(1, RNG.nextInt(3)));
                case 1 -> new ItemStack(Items.BLAZE_ROD, Math.max(1, RNG.nextInt(3)));
                default -> new ItemStack(Items.COAL, Math.max(1, RNG.nextInt(3)));
            };
            drops.add(0, new ItemEntity(level, x, y, z, bonus));
        }
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null || player.level().isClientSide()) return;
        ToolStack tool = toolOf(player);
        if (tool == null) return;

        if (has(tool, DISSOLVING)) {
            if (RNG.nextFloat() <= 0.8F) {
                event.setDroppedExperience(0);
            } else {
                event.setDroppedExperience(event.getDroppedExperience() * (RNG.nextInt(3) + 2));
            }
        }

        if (has(tool, ANALYSING) && event.getDroppedExperience() > 0) {
            event.setDroppedExperience(analysingUpdateXp(event.getDroppedExperience()));
        }
    }

    @SubscribeEvent
    public static void onBlockBreakXp(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getLevel().isClientSide()) return;
        ToolStack tool = toolOf(player);
        if (tool == null) return;
        if (has(tool, ANALYSING) && event.getExpToDrop() > 0) {
            event.setExpToDrop(analysingUpdateXp(event.getExpToDrop()));
        }
    }

    private static int analysingUpdateXp(int xp) {
        float exp = RNG.nextFloat() * RNG.nextFloat() * RNG.nextFloat() * (xp + RNG.nextInt(xp) * (1.0F + RNG.nextFloat()));
        return Math.round(exp);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return;
        ToolStack tool;
        try { tool = ToolStack.from(stack); } catch (RuntimeException e) { return; }

        LivingEntity victim = event.getEntity();
        Level level = victim.level();

        if (has(tool, REVIVE) && victim instanceof PathfinderMob && RNG.nextFloat() <= 0.15F) {
            EntityType<?> type = victim.getType();
            Entity revived = type.create(level);
            if (revived != null) {
                revived.moveTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), victim.getXRot());
                level.addFreshEntity(revived);
                player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }

        if (has(tool, SOULEATER) && victim instanceof Mob mob) {
            CompoundTag persistent = tool.getPersistentData().getCompound(SOULEATER_KEY);
            float health = mob.getMaxHealth();
            int killcount = persistent.getInt("killcount") + 1;
            float bonus = persistent.getFloat("bonus")
                        + Math.round(RNG.nextFloat() * health * 100.0F) / 25000.0F;
            bonus = Math.round(bonus * 100.0F) / 100.0F;
            persistent.putInt("killcount", killcount);
            persistent.putFloat("health", health);
            persistent.putFloat("bonus", bonus);
            tool.getPersistentData().put(SOULEATER_KEY, persistent);
        }

        if (has(tool, CONGENIAL) && victim instanceof PathfinderMob) {
            CompoundTag persistent = tool.getPersistentData().getCompound(CONGENIAL_KEY);
            if (persistent.getString("name").isEmpty()) {
                persistent.putString("name", victim.getName().getString());
                tool.getPersistentData().put(CONGENIAL_KEY, persistent);
            }
        }
    }

    private static final ResourceLocation SOULEATER_KEY = new ResourceLocation("taiga", "souleater");
    private static final ResourceLocation CONGENIAL_KEY = new ResourceLocation("taiga", "congenial");
}
