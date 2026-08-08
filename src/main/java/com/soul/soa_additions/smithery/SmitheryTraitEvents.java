package com.soul.soa_additions.smithery;

import com.soul.smithery.item.tool.SmitheryToolData;
import com.soul.smithery.item.tool.SmitheryToolItem;
import com.soul.smithery.item.tool.ToolComposition;
import com.soul.smithery.item.tool.ToolStats;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Smithery trait behaviors that need raw Forge event access (e.g. event cancellation)
 * beyond what the Modifier onBlockBreak hook offers.
 *
 * <p>Mutate (TAIGA nucleum): 5% chance that breaking a "natural" block cancels the
 * break and transforms the block into a random other natural block. Ported from
 * {@code com.sosnitzka.taiga.traits.TraitMutate} (1.12): same 13-block pool, same
 * odds, break cancelled so nothing drops.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class SmitheryTraitEvents {

    private SmitheryTraitEvents() {}

    /** 1.20 equivalents of the 1.12 mutate pool (grass -> grass_block, snow -> snow_block, wheat kept). */
    private static final List<Block> MUTABLE = List.of(
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.SAND,
            Blocks.GRASS_BLOCK, Blocks.CLAY, Blocks.NETHERRACK, Blocks.ICE,
            Blocks.SNOW_BLOCK, Blocks.MAGMA_BLOCK, Blocks.LAVA, Blocks.WATER,
            Blocks.WHEAT);

    @SubscribeEvent
    public static void onMutateBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof SmitheryToolItem)) return;
        if (!MUTABLE.contains(event.getState().getBlock())) return;

        Level level = player.level();
        if (level.getRandom().nextFloat() <= 0.95f) return;
        if (!hasMutate(tool)) return;

        Block replacement = MUTABLE.get(level.getRandom().nextInt(MUTABLE.size()));
        event.setCanceled(true);
        level.setBlockAndUpdate(event.getPos(), replacement.defaultBlockState());
    }

    private static boolean hasMutate(ItemStack tool) {
        return hasTrait(tool, SoaSmitheryModifiers.MUTATE);
    }

    /**
     * True when the composed stack grants {@code modifierId} from any source — material trait,
     * synergy or anvil.
     *
     * <p>Scans {@code allEffects}, not {@code activeEffects}: the latter only holds effects whose
     * modifier declares a runtime hook, so a trait implemented out here in an event handler —
     * which is exactly why it has no hook — would never be found in it.</p>
     */
    static boolean hasTrait(ItemStack stack, ResourceLocation modifierId) {
        ToolComposition comp = SmitheryToolData.getComposition(stack);
        if (comp == null || !comp.isValid()) return false;
        ToolStats stats = ToolStats.compute(comp, SmitheryToolData.getAppliedModifiers(stack));
        for (ToolStats.ResolvedEffect r : stats.allEffects) {
            if (modifierId.equals(r.effect().modifierId())) return true;
        }
        return false;
    }

    // ---------------- Catcher (TAIGA prometheum) ----------------

    /**
     * Catcher: killing a mob may bottle it into the tool instead, to be let out again later.
     *
     * <p>Ported from {@code com.sosnitzka.taiga.traits.TraitCatcher} (1.12), which rolled
     * {@code random.nextInt(maxHealth) < chance} on {@link LivingDeathEvent}, cancelled the death,
     * stored the mob's name on the tool and removed the entity. Odds therefore fall as the target
     * gets beefier — a zombie is a coin-toss away, a boss effectively never. The 1.12 version
     * released on a custom keybind; a keybind needs a client mod and a packet, so this releases on
     * sneak-right-click instead, which is the only deliberate deviation.</p>
     */
    private static final int CATCH_CHANCE = 5;
    private static final String CATCH_TAG = "SoaCatcher";

    @SubscribeEvent
    public static void onCatcherKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return;                       // never bottle a player
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof SmitheryToolItem)) return;
        if (tool.getOrCreateTag().contains(CATCH_TAG)) return;      // one occupant at a time
        if (!hasTrait(tool, SoaSmitheryModifiers.CATCHER)) return;

        int maxHealth = Math.max(1, (int) victim.getMaxHealth());
        if (player.level().getRandom().nextInt(maxHealth) >= CATCH_CHANCE) return;

        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(victim.getType());
        if (type == null) return;

        CompoundTag caught = new CompoundTag();
        caught.putString("Mob", type.toString());
        caught.putString("Name", victim.getDisplayName().getString());
        tool.getOrCreateTag().put(CATCH_TAG, caught);

        event.setCanceled(true);
        victim.discard();
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH,
                SoundSource.PLAYERS, 0.7f, 1.4f);
        player.displayClientMessage(Component.translatable(
                "message.soa_additions.catcher.caught", victim.getDisplayName()), true);
    }

    // ---------------- Blasting (Tinkers' mining modifier) ----------------

    /**
     * Blasting: the tool tears through anything at full speed, and the block gives nothing back.
     *
     * <p>Both halves need raw events. Mining speed on a block the tool is not effective against is
     * decided inside vanilla's destroy-speed math, which no {@code Modifier} hook sees — Forge's
     * {@link PlayerEvent.BreakSpeed} is the only place to override it. And no action in the
     * modifier vocabulary can empty a block's drop list, so the break is cancelled and the block
     * removed without drops instead.</p>
     *
     * <p>Only blocks the tool is NOT already correct for are affected: on its proper targets the
     * tool behaves normally and keeps the drops, which is what made the 1.12 modifier a trade
     * rather than a straight downgrade.</p>
     */
    @SubscribeEvent
    public static void onBlastingSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        if (!(tool.getItem() instanceof SmitheryToolItem)) return;
        if (tool.isCorrectToolForDrops(event.getState())) return;
        if (!hasTrait(tool, SoaSmitheryModifiers.BLASTING_MINING)) return;

        ToolComposition comp = SmitheryToolData.getComposition(tool);
        if (comp == null) return;
        ToolStats stats = ToolStats.compute(comp, SmitheryToolData.getAppliedModifiers(tool));
        event.setNewSpeed(Math.max(event.getNewSpeed(), stats.miningSpeed));
    }

    @SubscribeEvent
    public static void onBlastingBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof SmitheryToolItem)) return;
        if (tool.isCorrectToolForDrops(event.getState())) return;   // normal target: keep the drops
        if (!hasTrait(tool, SoaSmitheryModifiers.BLASTING_MINING)) return;

        event.setCanceled(true);
        player.level().destroyBlock(event.getPos(), false, player);  // false = no drops
        tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
    }

    @SubscribeEvent
    public static void onCatcherRelease(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isShiftKeyDown()) return;
        ItemStack tool = event.getItemStack();
        CompoundTag tag = tool.getTag();
        if (tag == null || !tag.contains(CATCH_TAG)) return;

        CompoundTag caught = tag.getCompound(CATCH_TAG);
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(
                ResourceLocation.tryParse(caught.getString("Mob")));
        tag.remove(CATCH_TAG);                       // clear even if the mob's type is gone now,
        if (type == null) return;                    // so a removed mod cannot brick the tool

        Vec3 where = player.position().add(player.getLookAngle().scale(2.0));
        Entity released = type.create(player.level());
        if (released == null) return;
        released.moveTo(where.x, where.y, where.z, player.getYRot(), 0f);
        player.level().addFreshEntity(released);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY,
                SoundSource.PLAYERS, 0.7f, 1.0f);
        event.setCanceled(true);
    }

    /** Wound level stacked on a Jaded victim, 1..max. */
    private static final String JADED_LEVEL = "soa_jaded_level";
    /** Game time the current Jaded window expires at. */
    private static final String JADED_UNTIL = "soa_jaded_until";

    /**
     * Stacks a Jaded wound on the victim and refreshes its window. Ports PlusTiC's
     * {@code Jaded#applyJaded}: the level climbs by one per hit up to {@code maxLevel}, and every
     * hit restarts the {@code duration}-tick window. Called from the modifier's attack hook, which
     * fires for arrows as well as melee — Jaded was one of the two 1.12 traits implementing
     * {@code IProjectileTrait}, so a jade-tipped arrow wounds exactly like a jade blade.
     */
    public static void applyJaded(Entity target, int maxLevel, int duration) {
        if (!(target instanceof LivingEntity victim) || victim.level().isClientSide()) return;
        CompoundTag data = victim.getPersistentData();
        long now = victim.level().getGameTime();
        int level = data.getLong(JADED_UNTIL) > now ? data.getInt(JADED_LEVEL) : 0;
        data.putInt(JADED_LEVEL, Math.min(level + 1, Math.max(1, maxLevel)));
        data.putLong(JADED_UNTIL, now + duration);
    }

    /**
     * Throttles healing on a Jaded victim. 1.12 undid the heal from a tick handler, letting
     * through {@code (3 - level) / 3} of it — a third of the victim's healing lost per wound
     * level, nothing at all at level 3. Forge's heal event scales it at the source instead of
     * rubber-banding health after the fact.
     */
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        CompoundTag data = victim.getPersistentData();
        if (!data.contains(JADED_UNTIL)) return;
        if (data.getLong(JADED_UNTIL) <= victim.level().getGameTime()) {
            data.remove(JADED_UNTIL);
            data.remove(JADED_LEVEL);
            return;
        }
        float allowed = (3 - Math.min(3, Math.max(0, data.getInt(JADED_LEVEL)))) / 3.0f;
        if (allowed <= 0f) {
            event.setCanceled(true);
        } else {
            event.setAmount(event.getAmount() * allowed);
        }
    }
}
