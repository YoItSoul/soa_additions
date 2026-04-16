package com.soul.soa_additions.bloodarsenal.item.stasis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.soul.soa_additions.bloodarsenal.BAConfig;
import com.soul.soa_additions.bloodarsenal.modifier.StasisModifiable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.item.IActivatable;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Stasis Axe — bound axe with modifier support.
 * Enhanced efficiency vs wood/plants when active.
 */
public class StasisAxeItem extends AxeItem implements IBindable, IActivatable {

    public StasisAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties props) {
        super(tier, attackDamage, attackSpeed, props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                boolean active = getActivated(stack);
                setActivatedState(stack, !active);
                player.displayClientMessage(Component.translatable(
                        active ? "tooltip.soa_additions.stasis.deactivated" : "tooltip.soa_additions.stasis.activated")
                        .withStyle(ChatFormatting.GOLD), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (getActivated(stack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        int charge = Math.min(getUseDuration(stack) - timeLeft, 30);
        if (charge > 0) {
            StasisModifiable.fromStack(stack).onRelease(level, player, stack, charge);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 72000; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float base = super.getDestroySpeed(stack, state);
        return getActivated(stack) ? base * 1.5f : base;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (!level.isClientSide() && miner instanceof Player player && getActivated(stack)) {
            StasisModifiable.fromStack(stack).onBlockDestroyed(level, player, stack, pos, state);
        }
        return result;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        if (getActivated(stack)) {
            if (level.getGameTime() % BAConfig.STASIS_DRAIN_INTERVAL.get() == 0) {
                Binding binding = getBinding(stack);
                if (binding != null) {
                    NetworkHelper.getSoulNetwork(binding).syphonAndDamage(player,
                            SoulTicket.item(stack, level, player, BAConfig.STASIS_DRAIN_LP.get()));
                }
            }
            StasisModifiable.fromStack(stack).onUpdate(level, player, stack, slot);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player player && getActivated(stack)) {
            StasisModifiable.fromStack(stack).hitEntity(attacker.level(), player, stack, target);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getDefaultAttributeModifiers(slot);
        Multimap<Attribute, AttributeModifier> base = super.getDefaultAttributeModifiers(slot);
        if (getActivated(stack)) {
            HashMultimap<Attribute, AttributeModifier> combined = HashMultimap.create(base);
            combined.putAll(StasisModifiable.fromStack(stack).getAttributeModifiers());
            return combined;
        } else {
            StasisModifiable modifiable = StasisModifiable.fromStack(stack);
            int shadowLevel = modifiable.getModifierLevel("shadow_tool");
            if (shadowLevel > 0) {
                HashMultimap<Attribute, AttributeModifier> deactivated = HashMultimap.create();
                double shadowDamage = 4.7 * shadowLevel / 3.0;
                deactivated.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", shadowDamage, AttributeModifier.Operation.ADDITION));
                deactivated.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.0, AttributeModifier.Operation.ADDITION));
                return deactivated;
            }
            return base;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        StasisTooltipHelper.addTooltip(stack, tooltip, this);
    }

    @Override
    public boolean isFoil(ItemStack stack) { return false; }
}
