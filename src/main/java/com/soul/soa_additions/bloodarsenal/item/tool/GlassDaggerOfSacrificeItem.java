package com.soul.soa_additions.bloodarsenal.item.tool;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import wayoftime.bloodmagic.util.helper.PlayerSacrificeHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Glass Dagger of Sacrifice — kills mobs to add LP to nearby Blood Altars.
 * Uses the entity's current health (not max) for LP calculation.
 * LP = currentHealth * lifeEssenceRatio * glassDaggerMultiplier (default 2.0)
 * Animals get purification bonus, children get 0.5x penalty.
 *
 * <p>Ported from: arcaratus.bloodarsenal.item.tool.ItemGlassDaggerOfSacrifice</p>
 */
public class GlassDaggerOfSacrificeItem extends Item {

    public GlassDaggerOfSacrificeItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level().isClientSide()) return false;
        if (!(attacker instanceof Player player) || attacker instanceof FakePlayer) return false;
        if (target instanceof Player) return false;
        if (!target.canChangeDimensions()) return false; // boss mobs
        if (!target.isAlive() || target.getHealth() < 0.5F) return false;

        Level level = target.level();

        // Get per-entity life essence ratio (default 25 for most entities)
        int lifeEssenceRatio = 25; // BM default

        // Calculate LP based on CURRENT health (not max)
        int lifeEssence = (int) (lifeEssenceRatio * target.getHealth());

        // Purification bonus for animals
        if (target instanceof Animal) {
            // In original, this uses PurificationHelper.getCurrentPurity()
            // We apply a base 1.0 multiplier (no purification system in port)
            lifeEssence = (int) (lifeEssence * 1.0);
        }

        // Child penalty
        if (target.isBaby()) {
            lifeEssence = (int) (lifeEssence * 0.5F);
        }

        // Apply glass dagger multiplier (default 2.0)
        lifeEssence = (int) (lifeEssence * BAConfig.GLASS_DAGGER_OF_SACRIFICE_LP_MULTIPLIER.get().doubleValue());

        // Fill altar and kill entity
        if (PlayerSacrificeHelper.findAndFillAltar(level, target, lifeEssence, true)) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
            target.setHealth(-1);
            target.die(player.damageSources().generic());
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.bloodarsenal.glass_dagger_of_sacrifice.desc")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
