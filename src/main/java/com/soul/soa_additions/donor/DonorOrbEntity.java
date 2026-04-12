package com.soul.soa_additions.donor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * A glowing orb entity that follows its owner around, hovering 1–2 blocks
 * nearby in a slow orbit. The orb is purely cosmetic — it has no collision,
 * no hitbox, and can't be interacted with.
 *
 * <p>Light emission is handled by overriding {@link #isOnFire()} to return
 * true (which makes the engine treat it as a light source) while the actual
 * fire rendering is suppressed in the renderer. For mods that support dynamic
 * lighting (e.g. Rubidium/Embeddium + dynamic lights), the entity also reports
 * a light level via the Forge hook.</p>
 */
public class DonorOrbEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(DonorOrbEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> ORB_COLOR =
            SynchedEntityData.defineId(DonorOrbEntity.class, EntityDataSerializers.INT);

    /** Orbit angle in radians, advanced each tick. */
    private float orbitAngle;
    /** Vertical bob offset in radians. */
    private float bobAngle;
    /** Random orbit radius (1.2–2.0 blocks). */
    private float orbitRadius = 1.5f;
    /** Orbit height above the player's feet. */
    private float orbitHeight = 1.8f;

    public DonorOrbEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(ORB_COLOR, 0xFF55FF55);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setOrbColor(int color) {
        this.entityData.set(ORB_COLOR, color);
    }

    public int getOrbColor() {
        return this.entityData.get(ORB_COLOR);
    }

    @Override
    public void tick() {
        super.tick();

        Player owner = getOwnerUUID()
                .map(uuid -> this.level().getPlayerByUUID(uuid))
                .orElse(null);

        if (owner == null || owner.isRemoved() || !owner.isAlive()) {
            if (!this.level().isClientSide) this.discard();
            return;
        }

        // Remove if owner is no longer a donor (server side)
        if (!this.level().isClientSide && !DonorRegistry.isDonor(owner.getUUID())) {
            this.discard();
            return;
        }

        // Orbit around the owner
        orbitAngle += 0.03f; // ~2 RPM
        bobAngle += 0.06f;

        double targetX = owner.getX() + Math.cos(orbitAngle) * orbitRadius;
        double targetZ = owner.getZ() + Math.sin(orbitAngle) * orbitRadius;
        double targetY = owner.getY() + orbitHeight + Math.sin(bobAngle) * 0.3;

        // Smooth interpolation towards target
        double speed = 0.15;
        double dx = (targetX - this.getX()) * speed;
        double dy = (targetY - this.getY()) * speed;
        double dz = (targetZ - this.getZ()) * speed;

        this.setPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz);

        // If too far from owner (e.g. teleport), snap to them
        if (this.distanceToSqr(owner) > 100) {
            this.setPos(owner.getX() + 1, owner.getY() + 2, owner.getZ());
        }
    }

    /** Makes the entity emit light level 15. The fire visual is suppressed
     *  in the renderer — we only want the light. */
    @Override
    public boolean isOnFire() { return true; }

    /** Hide the actual fire display. */
    @Override
    public boolean displayFireAnimation() { return false; }

    @Override
    public boolean isPickable() { return false; }
    @Override
    public boolean isPushable() { return false; }

    /** Don't save to disk — orbs are respawned on login. */
    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) setOwnerUUID(tag.getUUID("Owner"));
        if (tag.contains("OrbColor")) setOrbColor(tag.getInt("OrbColor"));
        if (tag.contains("OrbitRadius")) orbitRadius = tag.getFloat("OrbitRadius");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        getOwnerUUID().ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putInt("OrbColor", getOrbColor());
        tag.putFloat("OrbitRadius", orbitRadius);
    }

    /** Randomize orbit parameters so multiple orbs don't stack. */
    public void randomizeOrbit() {
        this.orbitAngle = (float) (Math.random() * Math.PI * 2);
        this.bobAngle = (float) (Math.random() * Math.PI * 2);
        this.orbitRadius = 1.2f + (float) (Math.random() * 0.8);
        this.orbitHeight = 1.5f + (float) (Math.random() * 0.6);
    }
}
