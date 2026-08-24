package com.soul.soa_additions.bloodarsenal.event;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import com.soul.soa_additions.bloodarsenal.BAItems;
import com.soul.soa_additions.bloodarsenal.item.bauble.*;
import com.soul.soa_additions.bloodarsenal.item.tool.WarpBladeItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.api.compat.IDemonWill;
import wayoftime.bloodmagic.common.item.IActivatable;

/**
 * Game-event handler for Blood Arsenal runtime behaviour.
 * Handles: Vampire Ring healing, Sacrifice/Self-Sacrifice amulet LP,
 * Divinity Sigil damage cancellation, Soul Pendant will pickup,
 * Warp Blade teleport-to-impact.
 */
public final class BAEventHandler {

    private BAEventHandler() {}

    // ── Vampire Ring — heal attacker on hit ─────────────────────────────

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            // Vampire Ring: heal attacker
            ItemStack ring = BACuriosHelper.findEquipped(attacker, BAItems.VAMPIRE_RING.get());
            if (!ring.isEmpty()) {
                float healing = event.getAmount() * (float) BAConfig.VAMPIRE_RING_MULTIPLIER.get().doubleValue();
                if (healing > 0) {
                    attacker.heal(healing);
                }
            }

            // Sacrifice Amulet: store LP from damage dealt to entities
            ItemStack amulet = BACuriosHelper.findEquipped(attacker, BAItems.SACRIFICE_AMULET.get());
            if (!amulet.isEmpty()) {
                int lp = (int) (event.getAmount() * BAConfig.SACRIFICE_AMULET_MULTIPLIER.get().doubleValue());
                SacrificeAmuletItem.addLP(amulet, lp);
            }
        }
    }

    // ── Self-Sacrifice Amulet — store LP from damage taken ──────────────

    @SubscribeEvent
    public static void onSelfSacrificeHurt(LivingHurtEvent event) {
        // LivingAttackEvent fires on both sides and before mitigation: walking into a cactus
        // credited LP on the client's copy of the amulet, and damage that was later cancelled
        // (i-frames, a shield, a lower-priority cancel) paid out anyway.
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof Player player) {
            // Self-Sacrifice Amulet: convert incoming damage to LP
            ItemStack amulet = BACuriosHelper.findEquipped(player, BAItems.SELF_SACRIFICE_AMULET.get());
            if (!amulet.isEmpty()) {
                int lp = (int) (event.getAmount() * BAConfig.SELF_SACRIFICE_AMULET_MULTIPLIER.get().doubleValue());
                // Reduce by regeneration effects
                if (player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) {
                    int regenAmplifier = player.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getAmplifier();
                    lp = (int) (lp * Math.max(0.0, 1.0 - 0.15 * (regenAmplifier + 1)));
                }
                SelfSacrificeAmuletItem.addLP(amulet, lp);
            }
        }
    }

    // ── Divinity Sigil — cancel ALL damage while active ─────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDivinityDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check all inventory slots (main, offhand, armor) for active Divinity Sigil
            for (ItemStack stack : player.getAllSlots()) {
                if (!stack.isEmpty() && stack.getItem() instanceof wayoftime.bloodmagic.common.item.IActivatable activatable) {
                    if (stack.getItem() == BAItems.SIGIL_DIVINITY.get() && activatable.getActivated(stack)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    // ── Soul Pendant — absorb demon will items on pickup ────────────────

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack pickedUp = event.getItem().getItem();

        if (pickedUp.getItem() instanceof IDemonWill willItem) {
            // Find equipped soul pendant
            for (var pendant : findAllPendants(player)) {
                if (pendant.getItem() instanceof SoulPendantItem soulPendant) {
                    var type = willItem.getType(pickedUp);
                    double will = willItem.getWill(type, pickedUp);
                    double added = soulPendant.addWill(pendant, will);
                    if (added > 0) {
                        double remaining = will - added;
                        if (remaining <= 0) {
                            pickedUp.setCount(0);
                            event.setCanceled(true);
                            return;
                        }
                        willItem.drainWill(type, pickedUp, added);
                    }
                }
            }
        }
    }

    // ── Swimming Sigil — triple mining speed in water/lava ────────────

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.isInWater() || player.isInLava()) {
            // Check if player has an active swimming sigil in inventory
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.getItem() instanceof IActivatable activatable
                        && stack.getItem() == BAItems.SIGIL_SWIMMING.get()
                        && activatable.getActivated(stack)) {
                    event.setNewSpeed(event.getOriginalSpeed() * 3F);
                    return;
                }
            }
            // Also check offhand
            for (ItemStack stack : player.getInventory().offhand) {
                if (!stack.isEmpty() && stack.getItem() instanceof IActivatable activatable
                        && stack.getItem() == BAItems.SIGIL_SWIMMING.get()
                        && activatable.getActivated(stack)) {
                    event.setNewSpeed(event.getOriginalSpeed() * 3F);
                    return;
                }
            }
        }
    }

    // ── Warp Blade — teleport wielder to projectile impact ──────────────
    // 1.12 EntityWarpBlade.onImpact: living hit → revenge-target + melee
    // attack + attemptTeleport to the target's center; block hit →
    // attemptTeleport to pos y+2; projectile dies. The arrow's 6.0 base
    // damage supplies the hit + aggro here, so only the warp is added.

    @SubscribeEvent
    public static void onWarpBladeImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow) || arrow.level().isClientSide()) {
            return;
        }
        if (!arrow.getPersistentData().getBoolean(WarpBladeItem.WARP_TAG)) {
            return;
        }
        if (!(arrow.getOwner() instanceof Player player) || !player.isAlive()) {
            return;
        }

        HitResult hit = event.getRayTraceResult();
        if (hit instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() != player && entityHit.getEntity() instanceof LivingEntity target) {
                warpTo(player, target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ());
            }
        } else if (hit instanceof BlockHitResult blockHit) {
            var pos = blockHit.getBlockPos();
            warpTo(player, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5);
            arrow.discard();
        }
    }

    private static void warpTo(Player player, double x, double y, double z) {
        // randomTeleport = 1.20 analog of 1.12 attemptTeleport (safe-spot search).
        if (player.randomTeleport(x, y, z, true)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private static java.util.List<ItemStack> findAllPendants(Player player) {
        java.util.List<ItemStack> pendants = new java.util.ArrayList<>();
        // Check curios and inventory for all pendant tiers
        for (var pendantReg : java.util.List.of(
                BAItems.SOUL_PENDANT_PETTY, BAItems.SOUL_PENDANT_LESSER,
                BAItems.SOUL_PENDANT_COMMON, BAItems.SOUL_PENDANT_GREATER,
                BAItems.SOUL_PENDANT_GRAND)) {
            ItemStack found = BACuriosHelper.findEquipped(player, pendantReg.get());
            if (!found.isEmpty()) {
                pendants.add(found);
            }
        }
        return pendants;
    }
}
