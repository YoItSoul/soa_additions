package com.soul.soa_additions.tconstructevo.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Projectile fired by the Sceptre tool. Ports the spirit of
 * {@code xyz.phanta.tconevo.entity.EntityMagicMissile}. Damage is configured
 * by the Sceptre itself (via NBT on the projectile stack) rather than being
 * hardcoded into the entity so the same projectile class can serve every
 * sceptre material combination.
 *
 * <p>Only the scaffold lives here for now — the detailed hit logic, client
 * sync data and render integration will land with the Sceptre tool itself
 * so all the behaviour stays in one readable place.</p>
 */
public class MagicMissileEntity extends ThrowableProjectile {

    private float damage = 1.0F;

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, Level level) {
        super(type, level);
    }

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    @Override
    protected void defineSynchedData() {
        // Damage is carried in NBT; nothing needs syncing through the entity data manager.
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!this.level().isClientSide && hit.getEntity() instanceof LivingEntity target) {
            var src = this.damageSources().mobProjectile(this, this.getOwner() instanceof LivingEntity le ? le : null);
            target.hurt(src, damage);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected float getGravity() {
        return 0.0F; // missiles fly straight until something is hit
    }
}
