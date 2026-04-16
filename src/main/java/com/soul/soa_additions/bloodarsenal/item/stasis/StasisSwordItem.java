package com.soul.soa_additions.bloodarsenal.item.stasis;

import com.google.common.collect.Multimap;
import com.soul.soa_additions.bloodarsenal.BAConfig;
import com.soul.soa_additions.bloodarsenal.modifier.StasisModifiable;
import net.minecraft.ChatFormatting;
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
import wayoftime.bloodmagic.common.item.IActivatable;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Stasis Sword — bound tool with modifier support.
 * Sneak+right-click toggles activation. While active, drains LP from soul network.
 * Hold right-click to charge, release triggers ability modifier.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.stasis.ItemStasisSword</p>
 */
public class StasisSwordItem extends SwordItem implements IBindable, IActivatable {

    private static final int CHARGE_TIME = 30;

    public StasisSwordItem(Tier tier, int attackDamage, float attackSpeed, Properties props) {
        super(tier, attackDamage, attackSpeed, props);
    }

    // ── Activation toggle ───────────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            // Toggle activation
            if (!level.isClientSide()) {
                boolean active = getActivated(stack);
                setActivatedState(stack, !active);
                player.displayClientMessage(Component.translatable(
                        active ? "tooltip.soa_additions.stasis.deactivated" : "tooltip.soa_additions.stasis.activated")
                        .withStyle(ChatFormatting.GOLD), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // Start charging for ability modifier release
        if (getActivated(stack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        int charge = Math.min(getUseDuration(stack) - timeLeft, CHARGE_TIME);
        if (charge > 0) {
            StasisModifiable modifiable = StasisModifiable.fromStack(stack);
            modifiable.onRelease(level, player, stack, charge);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    // ── Tick — LP drain + modifier updates ──────────────────────────────

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        if (getActivated(stack)) {
            // Drain LP every N ticks
            int drainInterval = BAConfig.STASIS_DRAIN_INTERVAL.get();
            if (level.getGameTime() % drainInterval == 0) {
                int drainCost = BAConfig.STASIS_DRAIN_LP.get();
                Binding binding = getBinding(stack);
                if (binding != null) {
                    NetworkHelper.getSoulNetwork(binding).syphonAndDamage(player,
                            SoulTicket.item(stack, level, player, drainCost));
                }
            }

            // Delegate to modifiers
            StasisModifiable modifiable = StasisModifiable.fromStack(stack);
            modifiable.onUpdate(level, player, stack, slot);
        }
    }

    // ── Combat — modifier delegation ────────────────────────────────────

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide() || !(attacker instanceof Player player)) return super.hurtEnemy(stack, target, attacker);

        if (getActivated(stack)) {
            StasisModifiable modifiable = StasisModifiable.fromStack(stack);
            modifiable.hitEntity(attacker.level(), player, stack, target);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    // ── Attributes — combine base + modifiers ───────────────────────────

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getDefaultAttributeModifiers(slot);

        Multimap<Attribute, AttributeModifier> base = super.getDefaultAttributeModifiers(slot);

        if (getActivated(stack)) {
            // Active: combine base attributes with modifier attributes
            StasisModifiable modifiable = StasisModifiable.fromStack(stack);
            Multimap<Attribute, AttributeModifier> modMods = modifiable.getAttributeModifiers();
            com.google.common.collect.HashMultimap<Attribute, AttributeModifier> combined = com.google.common.collect.HashMultimap.create(base);
            combined.putAll(modMods);
            return combined;
        } else {
            // Deactivated: ShadowTool passive mode — reduced damage (6.7 * level / 3)
            StasisModifiable modifiable = StasisModifiable.fromStack(stack);
            int shadowLevel = modifiable.getModifierLevel("shadow_tool");
            if (shadowLevel > 0) {
                com.google.common.collect.HashMultimap<Attribute, AttributeModifier> deactivated = com.google.common.collect.HashMultimap.create();
                double shadowDamage = 6.7 * shadowLevel / 3.0;
                deactivated.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", shadowDamage, AttributeModifier.Operation.ADDITION));
                deactivated.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4, AttributeModifier.Operation.ADDITION));
                return deactivated;
            }
            return base;
        }
    }

    // ── Tooltip ─────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Binding binding = getBinding(stack);
        if (binding != null) {
            tooltip.add(Component.translatable("tooltip.soa_additions.stasis.bound_to", binding.getOwnerName())
                    .withStyle(ChatFormatting.GRAY));
        }

        boolean active = getActivated(stack);
        tooltip.add(Component.translatable(active ? "tooltip.soa_additions.stasis.active" : "tooltip.soa_additions.stasis.inactive")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));

        // Show modifiers
        StasisModifiable modifiable = StasisModifiable.fromStack(stack);
        for (var pair : modifiable.getAllModifiers()) {
            tooltip.add(Component.literal(" - ")
                    .append(Component.translatable(pair.getLeft().getTranslationKey()))
                    .append(Component.literal(" " + toRoman(pair.getRight().getLevel() + 1)))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            default -> String.valueOf(n);
        };
    }
}
