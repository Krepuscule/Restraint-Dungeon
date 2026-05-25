package com.twi.restraint_dungeon.block.restraint_device.seat_entity;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class SeatEntity extends Entity {
    private BlockPos sourcePos;
    private int lifeTicks = 0;

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
    }

    public SeatEntity(Level level, double x, double y, double z) {
        this(SeatEntities.SEAT.get(), level);
        this.setPos(x, y, z);
        this.sourcePos = BlockPos.containing(x, y, z);
    }

    private void invokeBlockAction(Consumer<RestraintDevice> action) {
        if (this.sourcePos != null && this.level().getBlockState(sourcePos).getBlock() instanceof RestraintDevice device) {
            action.accept(device);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof LivingEntity living) {
                invokeBlockAction(device -> device.onRiderTick(living, this.level(), sourcePos));
            }


            lifeTicks++;
            if (lifeTicks > 2 && this.getPassengers().isEmpty()) {
                this.discard();
            }
        }
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (!this.level().isClientSide && passenger instanceof LivingEntity living) {
            invokeBlockAction(device -> device.onMount(living, this.level(), sourcePos));
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (!this.level().isClientSide && passenger instanceof LivingEntity living) {
            invokeBlockAction(device -> device.onDismount(living, this.level(), sourcePos));
        }
        super.removePassenger(passenger);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (passenger instanceof LivingEntity living) {
            float seatYaw = this.getYRot();
            living.yBodyRot = seatYaw;
            living.yBodyRotO = seatYaw;
            living.setYHeadRot(living.getYRot());
        }
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return super.getPassengerRidingPosition(passenger);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("SourcePos")) this.sourcePos = BlockPos.of(tag.getLong("SourcePos"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.sourcePos != null) tag.putLong("SourcePos", this.sourcePos.asLong());
    }

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    public boolean canBeCollidedWith() { return false; }
}