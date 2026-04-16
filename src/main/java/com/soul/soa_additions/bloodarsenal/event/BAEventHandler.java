package com.soul.soa_additions.bloodarsenal.event;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.bloodarsenal.BAConfig;
import com.soul.soa_additions.bloodarsenal.BAItems;
import com.soul.soa_additions.bloodarsenal.item.bauble.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wayoftime.bloodmagic.api.compat.IDemonWill;
import wayoftime.bloodmagic.common.item.IActivatable;

/**
 * Game-event handler for Blood Arsenal runtime behaviour.
 * Handles: Vampire Ring healing, Sacrifice/Self-Sacrifice amulet LP,
 * Divinity Sigil damage cancellation, Soul Pendant will pickup.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
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
    public static void onLivingAttack(LivingAttackEvent event) {
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

    private static java.util.List<ItemStack> findAllPendants(Player player) {
        java.util.List<ItemStack> pendants = new java.util.ArrayList<>();
        // Check curios and inventory for all pendant tiers
        for (var pendantReg : new net.minecraftforge.registries.RegistryObject[]{
                BAItems.SOUL_PENDANT_PETTY, BAItems.SOUL_PENDANT_LESSER,
                BAItems.SOUL_PENDANT_COMMON, BAItems.SOUL_PENDANT_GREATER,
                BAItems.SOUL_PENDANT_GRAND}) {
            ItemStack found = BACuriosHelper.findEquipped(player, (net.minecraft.world.item.Item) pendantReg.get());
            if (!found.isEmpty()) {
                pendants.add(found);
            }
        }
        return pendants;
    }
}
