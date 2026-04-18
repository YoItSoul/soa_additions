package com.soul.soa_additions.tconstructevo.item.sceptre;

import com.soul.soa_additions.tconstructevo.TConEvoEntities;
import com.soul.soa_additions.tconstructevo.entity.MagicMissileEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

/**
 * Magic-missile launcher port of {@code xyz.phanta.tconevo.item.tool.ItemToolSceptre}.
 *
 * <p>Right-click fires three {@link MagicMissileEntity} projectiles in a small
 * spread (±π/12). Each shot consumes 8 durability. Per-missile damage is the
 * tool's current ATTACK_DAMAGE stat — material/modifier contributions therefore
 * scale the projectile damage transparently.</p>
 *
 * <p>If the tool is broken or out of durability the right-click falls through
 * to the standard {@link ModifiableItem} interaction loop so modifier
 * GENERAL_INTERACT hooks still get a chance.</p>
 */
public class SceptreItem extends ModifiableItem {

    private static final int DURABILITY_PER_SHOT = 8;
    private static final int PROJECTILES = 3;
    private static final float SPREAD = (float) (Math.PI / 12.0);
    private static final float MISSILE_VELOCITY = 1.5F;

    public SceptreItem(Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken() || tool.getCurrentDurability() < DURABILITY_PER_SHOT) {
            return super.use(level, player, hand);
        }

        float damage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);
        if (!level.isClientSide) {
            for (int i = 0; i < PROJECTILES; i++) {
                float yawOffset = (i - (PROJECTILES - 1) / 2.0F) * SPREAD;
                MagicMissileEntity missile = new MagicMissileEntity(
                        TConEvoEntities.MAGIC_MISSILE.get(), player, level);
                missile.setDamage(damage);
                missile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                missile.shootFromRotation(player, player.getXRot(), player.getYRot() + (float) Math.toDegrees(yawOffset),
                        0.0F, MISSILE_VELOCITY, 0.0F);
                level.addFreshEntity(missile);
            }
            ToolDamageUtil.damageAnimated(tool, DURABILITY_PER_SHOT, player, hand);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.8F);
        player.getCooldowns().addCooldown(this, 4);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
