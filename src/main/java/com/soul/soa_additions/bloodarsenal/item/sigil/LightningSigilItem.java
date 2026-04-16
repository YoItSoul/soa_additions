package com.soul.soa_additions.bloodarsenal.item.sigil;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilBase;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import wayoftime.bloodmagic.util.helper.NumeralHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Lightning Sigil — summons lightning at the target location.
 * Sneak+right-click cycles through 6 levels (0-5), each summoning
 * increasingly complex patterns of lightning bolts.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.sigil.ItemSigilLightning</p>
 */
public class LightningSigilItem extends ItemSigilBase {

    private static final String TAG_LEVEL = "ba_lightning_level";

    // LP cost multipliers per level (from original source)
    private static final int[] COST_MULTIPLIERS = {1, 4, 7, 13, 29, 85};

    public LightningSigilItem() {
        super("ba_lightning", BAConfig.SIGIL_LIGHTNING_COST.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isUnusable(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            // Sneak+right-click: cycle level
            if (player.isShiftKeyDown()) {
                int currentLevel = getLightningLevel(stack);
                int nextLevel = currentLevel >= 5 ? 0 : currentLevel + 1;
                stack.getOrCreateTag().putInt(TAG_LEVEL, nextLevel);
                player.displayClientMessage(
                        Component.translatable("chat.bloodarsenal.set_level",
                                NumeralHelper.toRoman(nextLevel + 1)), true);
                return InteractionResultHolder.success(stack);
            }

            // Normal right-click: fire lightning
            Binding binding = getBinding(stack);
            if (binding == null) return InteractionResultHolder.fail(stack);

            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hit.getType() == HitResult.Type.MISS) {
                return InteractionResultHolder.fail(stack);
            }

            double x = hit.getLocation().x;
            double y = hit.getLocation().y;
            double z = hit.getLocation().z;

            // Adjust position based on hit face (from original)
            switch (hit.getDirection()) {
                case DOWN -> y -= 2.0;
                case NORTH -> z -= 0.5;
                case SOUTH -> z += 0.5;
                case WEST -> x -= 0.5;
                case EAST -> x += 0.5;
                default -> {}
            }

            int lightningLevel = getLightningLevel(stack);
            int cost = (int) (COST_MULTIPLIERS[lightningLevel] * (double) BAConfig.SIGIL_LIGHTNING_COST.get());

            SoulNetwork network = NetworkHelper.getSoulNetwork(binding);
            // Check if we can afford it
            if (NetworkHelper.canSyphonFromContainer(stack, cost) || player.getAbilities().instabuild) {
                summonLightningPattern((ServerLevel) level, x, y, z, lightningLevel);
                network.syphonAndDamage(player, SoulTicket.item(stack, level, player, cost));
            } else {
                player.displayClientMessage(
                        Component.translatable("chat.bloodarsenal.too_weak"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private int getLightningLevel(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_LEVEL)) {
            return stack.getTag().getInt(TAG_LEVEL);
        }
        return 0;
    }

    private void summonLightningPattern(ServerLevel level, double x, double y, double z, int lightningLevel) {
        switch (lightningLevel) {
            case 0 -> {
                summonBolt(level, x, y, z);
            }
            case 1 -> {
                summonBolt(level, x, y, z);
                summonBolt(level, x + 1, y, z);
                summonBolt(level, x, y, z + 1);
                summonBolt(level, x + 1, y, z + 1);
            }
            case 2 -> {
                summonBolt(level, x, y, z);
                summonBolt(level, x + 2, y, z);
                summonBolt(level, x - 2, y, z);
                summonBolt(level, x + 1, y, z + 2);
                summonBolt(level, x - 1, y, z + 2);
                summonBolt(level, x + 1, y, z - 2);
                summonBolt(level, x - 1, y, z - 2);
            }
            case 3 -> {
                summonBolt(level, x, y, z);
                summonBolt(level, x + 3, y, z);
                summonBolt(level, x - 3, y, z);
                summonBolt(level, x + 2, y, z + 2);
                summonBolt(level, x + 2, y, z - 2);
                summonBolt(level, x - 2, y, z + 2);
                summonBolt(level, x - 2, y, z - 2);
                summonBolt(level, x, y, z + 3);
                summonBolt(level, x, y, z - 3);
                summonBolt(level, x + 1, y, z + 1);
                summonBolt(level, x + 1, y, z - 1);
                summonBolt(level, x - 1, y, z + 1);
                summonBolt(level, x - 1, y, z - 1);
            }
            case 4 -> {
                // Inner ring (same as level 3)
                summonBolt(level, x, y, z);
                summonBolt(level, x + 3, y, z);
                summonBolt(level, x - 3, y, z);
                summonBolt(level, x + 2, y, z + 2);
                summonBolt(level, x + 2, y, z - 2);
                summonBolt(level, x - 2, y, z + 2);
                summonBolt(level, x - 2, y, z - 2);
                summonBolt(level, x, y, z + 3);
                summonBolt(level, x, y, z - 3);
                summonBolt(level, x + 1, y, z + 1);
                summonBolt(level, x + 1, y, z - 1);
                summonBolt(level, x - 1, y, z + 1);
                summonBolt(level, x - 1, y, z - 1);
                // Outer ring
                summonBolt(level, x + 6, y, z);
                summonBolt(level, x + 4, y, z + 4);
                summonBolt(level, x, y, z + 6);
                summonBolt(level, x + 4, y, z - 4);
                summonBolt(level, x - 6, y, z);
                summonBolt(level, x - 4, y, z - 4);
                summonBolt(level, x, y, z - 6);
                summonBolt(level, x - 4, y, z + 4);
                summonBolt(level, x - 5, y, z - 2);
                summonBolt(level, x - 5, y, z + 2);
                summonBolt(level, x + 5, y, z - 2);
                summonBolt(level, x + 5, y, z + 2);
                summonBolt(level, x - 2, y, z - 5);
                summonBolt(level, x - 2, y, z + 5);
                summonBolt(level, x + 2, y, z - 5);
                summonBolt(level, x + 2, y, z + 5);
            }
            case 5 -> {
                // Inner ring (same as level 3)
                summonBolt(level, x, y, z);
                summonBolt(level, x + 3, y, z);
                summonBolt(level, x - 3, y, z);
                summonBolt(level, x + 2, y, z + 2);
                summonBolt(level, x + 2, y, z - 2);
                summonBolt(level, x - 2, y, z + 2);
                summonBolt(level, x - 2, y, z - 2);
                summonBolt(level, x, y, z + 3);
                summonBolt(level, x, y, z - 3);
                summonBolt(level, x + 1, y, z + 1);
                summonBolt(level, x + 1, y, z - 1);
                summonBolt(level, x - 1, y, z + 1);
                summonBolt(level, x - 1, y, z - 1);
                // Second ring
                summonBolt(level, x - 4, y, z - 1);
                summonBolt(level, x - 4, y, z + 1);
                summonBolt(level, x + 4, y, z - 1);
                summonBolt(level, x + 4, y, z + 1);
                summonBolt(level, x - 1, y, z - 4);
                summonBolt(level, x - 1, y, z + 4);
                summonBolt(level, x + 1, y, z - 4);
                summonBolt(level, x + 1, y, z + 4);
                summonBolt(level, x + 3, y, z + 3);
                summonBolt(level, x + 3, y, z - 3);
                summonBolt(level, x - 3, y, z + 3);
                summonBolt(level, x - 3, y, z - 3);
                // Third ring (same outer as level 4)
                summonBolt(level, x + 6, y, z);
                summonBolt(level, x + 4, y, z + 4);
                summonBolt(level, x, y, z + 6);
                summonBolt(level, x + 4, y, z - 4);
                summonBolt(level, x - 6, y, z);
                summonBolt(level, x - 4, y, z - 4);
                summonBolt(level, x, y, z - 6);
                summonBolt(level, x - 4, y, z + 4);
                summonBolt(level, x - 5, y, z - 2);
                summonBolt(level, x - 5, y, z + 2);
                summonBolt(level, x + 5, y, z - 2);
                summonBolt(level, x + 5, y, z + 2);
                summonBolt(level, x - 2, y, z - 5);
                summonBolt(level, x - 2, y, z + 5);
                summonBolt(level, x + 2, y, z - 5);
                summonBolt(level, x + 2, y, z + 5);
                // Outermost ring
                summonBolt(level, x + 9, y, z);
                summonBolt(level, x - 9, y, z);
                summonBolt(level, x, y, z + 9);
                summonBolt(level, x, y, z - 9);
                summonBolt(level, x + 7, y, z + 1);
                summonBolt(level, x + 7, y, z - 1);
                summonBolt(level, x - 7, y, z + 1);
                summonBolt(level, x - 7, y, z - 1);
                summonBolt(level, x + 1, y, z + 7);
                summonBolt(level, x + 1, y, z - 7);
                summonBolt(level, x - 1, y, z + 7);
                summonBolt(level, x - 1, y, z - 7);
                summonBolt(level, x + 6, y, z + 3);
                summonBolt(level, x + 6, y, z - 3);
                summonBolt(level, x - 6, y, z + 3);
                summonBolt(level, x - 6, y, z - 3);
                summonBolt(level, x + 3, y, z + 6);
                summonBolt(level, x + 3, y, z - 6);
                summonBolt(level, x - 3, y, z + 6);
                summonBolt(level, x - 3, y, z - 6);
                summonBolt(level, x + 5, y, z + 5);
                summonBolt(level, x + 5, y, z - 5);
                summonBolt(level, x - 5, y, z + 5);
                summonBolt(level, x - 5, y, z - 5);
                summonBolt(level, x + 6, y, z + 6);
                summonBolt(level, x + 6, y, z - 6);
                summonBolt(level, x - 6, y, z + 6);
                summonBolt(level, x - 6, y, z - 6);
                summonBolt(level, x + 8, y, z + 2);
                summonBolt(level, x + 8, y, z - 2);
                summonBolt(level, x - 8, y, z + 2);
                summonBolt(level, x - 8, y, z - 2);
                summonBolt(level, x + 2, y, z + 8);
                summonBolt(level, x + 2, y, z - 8);
                summonBolt(level, x - 2, y, z + 8);
                summonBolt(level, x - 2, y, z - 8);
                summonBolt(level, x + 7, y, z + 4);
                summonBolt(level, x + 7, y, z - 4);
                summonBolt(level, x - 7, y, z + 4);
                summonBolt(level, x - 7, y, z - 4);
                summonBolt(level, x + 4, y, z + 7);
                summonBolt(level, x + 4, y, z - 7);
                summonBolt(level, x - 4, y, z + 7);
                summonBolt(level, x - 4, y, z - 7);
            }
        }
    }

    private void summonBolt(ServerLevel level, double x, double y, double z) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(false);
            level.addFreshEntity(bolt);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int lightningLevel = getLightningLevel(stack);
        tooltip.add(Component.translatable("tooltip.soa_additions.sigil_lightning.level",
                NumeralHelper.toRoman(lightningLevel + 1)).withStyle(ChatFormatting.DARK_RED));
    }
}
