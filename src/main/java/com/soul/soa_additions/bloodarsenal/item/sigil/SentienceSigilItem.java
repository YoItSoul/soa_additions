package com.soul.soa_additions.bloodarsenal.item.sigil;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilBase;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.Map;

/**
 * Sentience Sigil — use from OFF-HAND to throw the mainhand tool/weapon as a projectile.
 * LP cost scales with the item's harvest level, attack damage, and enchantments.
 * 20-tick cooldown.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.sigil.ItemSigilSentience</p>
 */
public class SentienceSigilItem extends ItemSigilBase {

    public SentienceSigilItem() {
        super("sentience", BAConfig.SIGIL_SENTIENCE_COST.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isUnusable(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        // Original: only works from offhand, throws mainhand item
        if (hand != InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof SwordItem || heldStack.getItem() instanceof TieredItem)) {
            return InteractionResultHolder.pass(stack);
        }

        player.getCooldowns().addCooldown(this, 20);

        // Spawn projectile (using Arrow as stand-in for EntitySummonedTool)
        Arrow arrow = new Arrow(level, player);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.5f, 1.0f);
        arrow.setBaseDamage(6.0);
        arrow.pickup = Arrow.Pickup.DISALLOWED;
        level.addFreshEntity(arrow);

        if (!player.getAbilities().instabuild) {
            ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
            mainhand.hurtAndBreak(2, player, (p) ->
                    p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            if (mainhand.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }

        if (!level.isClientSide()) {
            syphonCosts(stack, level, player, heldStack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    private void syphonCosts(ItemStack stack, Level level, Player player, ItemStack summonedTool) {
        float cost = 500;

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        if (summonedTool.getItem() instanceof TieredItem tiered && !(summonedTool.getItem() instanceof SwordItem)) {
            // Tool: cost scales with harvest level and damage
            int harvestLevel = tiered.getTier().getLevel();
            cost *= 1 + (harvestLevel / 3.0f);
            cost *= 1 + ((damage / 3.0f) / 3.0f);
        } else if (summonedTool.getItem() instanceof SwordItem) {
            // Sword: cost scales with damage
            cost *= 1 + (damage / 4.0f);
        }

        // Enchantment costs
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(summonedTool);
        for (int lvl : enchantments.values()) {
            cost += 200 * lvl;
        }

        NetworkHelper.getSoulNetwork(player).syphonAndDamage(player,
                SoulTicket.item(stack, level, player, (int) cost));
    }
}
