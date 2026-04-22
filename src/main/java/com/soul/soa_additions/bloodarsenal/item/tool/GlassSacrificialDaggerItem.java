package com.soul.soa_additions.bloodarsenal.item.tool;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.event.SacrificeKnifeUsedEvent;
import wayoftime.bloodmagic.util.helper.IncenseHelper;
import wayoftime.bloodmagic.util.helper.PlayerHelper;
import wayoftime.bloodmagic.util.helper.PlayerSacrificeHelper;
import wayoftime.bloodmagic.potion.BloodMagicPotions;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Glass Sacrificial Dagger — self-sacrifice to add LP to nearby Blood Altars.
 * Single right-click drains health and fills altar. Applies Bleeding effect.
 * Gives 5x the LP of the normal sacrificial dagger.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.tool.ItemGlassSacrificialDagger</p>
 */
public class GlassSacrificialDaggerItem extends Item {

    public GlassSacrificialDaggerItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (PlayerHelper.isFakePlayer(player) || player.getAbilities().instabuild) {
            return InteractionResultHolder.pass(stack);
        }

        // Check if player is prepared for incense sacrifice (ceremonial mode)
        if (canUseForSacrifice(stack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        int lpAdded = BAConfig.GLASS_SACRIFICIAL_DAGGER_LP.get();

        // Post BM event
        SacrificeKnifeUsedEvent evt = new SacrificeKnifeUsedEvent(player, true, true, 2, lpAdded);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(evt)) {
            return InteractionResultHolder.pass(stack);
        }

        if (evt.shouldDrainHealth) {
            // Deal tiny attack damage + reduce health directly
            player.invulnerableTime = 0;
            player.hurt(player.damageSources().generic(), 0.001F);
            float healthDrain = (float) (BAConfig.GLASS_DAGGER_DAMAGE.get().doubleValue() + level.random.nextInt(3));
            player.setHealth(Math.max(player.getHealth() - healthDrain, 0.0001F));

            // 50% chance to apply Bleeding effect (use Wither as Bleeding substitute)
            if (!player.hasEffect(net.minecraft.world.effect.MobEffects.WITHER) && level.random.nextBoolean()) {
                player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER,
                        40 + (level.random.nextInt(4) * 20), level.random.nextInt(2)));
            }

            // Death mechanic
            if (player.getHealth() <= 0.001F) {
                player.hurt(player.damageSources().generic(), Float.MAX_VALUE);
            }
        }

        if (!evt.shouldFillAltar) {
            return InteractionResultHolder.pass(stack);
        }

        lpAdded = evt.lpAdded;

        // Sound + particles
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

        // Check for Soul Fray debuff
        if (player.hasEffect(BloodMagicPotions.SOUL_FRAY.get())) {
            return InteractionResultHolder.pass(stack);
        }

        PlayerSacrificeHelper.findAndFillAltar(level, player, lpAdded, false);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            boolean prepared = isPlayerPreparedForSacrifice(level, player);
            setUseForSacrifice(stack, prepared);
            if (IncenseHelper.getHasMaxIncense(stack) && !prepared) {
                IncenseHelper.setHasMaxIncense(stack, player, false);
            }
            if (prepared) {
                boolean isMax = IncenseHelper.getMaxIncense(player) == IncenseHelper.getCurrentIncense(player);
                IncenseHelper.setHasMaxIncense(stack, player, isMax);
            }
        }
    }

    @Override
    public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack, int remainingDuration) {
        if (!level.isClientSide() && entity instanceof Player player) {
            if (PlayerSacrificeHelper.sacrificePlayerHealth(player)) {
                IncenseHelper.setHasMaxIncense(stack, player, false);
            }
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

    @Override
    public boolean isFoil(ItemStack stack) {
        return IncenseHelper.getHasMaxIncense(stack) || super.isFoil(stack);
    }

    private boolean isPlayerPreparedForSacrifice(Level level, Player player) {
        return !level.isClientSide() && PlayerSacrificeHelper.getPlayerIncense(player) > 0;
    }

    private boolean canUseForSacrifice(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getBoolean("sacrifice");
        }
        return false;
    }

    private void setUseForSacrifice(ItemStack stack, boolean sacrifice) {
        stack.getOrCreateTag().putBoolean("sacrifice", sacrifice);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.bloodarsenal.glass_sacrificial_dagger.desc")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
