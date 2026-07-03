package com.twi.restraint_dungeon.block.restraint_device.seat_entity;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SeatEntity extends Entity {
    private BlockPos sourcePos;
    private static final EntityDataAccessor<BlockPos> SOURCE_POS = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.BLOCK_POS);
    private int lifeTicks = 0;

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
    }

    public SeatEntity(Level level, double x, double y, double z, BlockPos sourcePos) {
        this(SeatEntities.SEAT.get(), level);
        this.setPos(x, y, z);
        this.entityData.set(SOURCE_POS, sourcePos);
    }

    @Nullable
    public BlockPos getSourcePos() {
        BlockPos pos = this.entityData.get(SOURCE_POS);
        return pos.equals(BlockPos.ZERO) ? null : pos;
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity passenger, @NotNull EntityDimensions dimensions, float partialTick) {
        BlockPos pos = getSourcePos();
        if (pos != null) {
            BlockState state = this.level().getBlockState(pos);
            if (state.getBlock() instanceof RestraintDevice device) {
                Direction facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
                        ? state.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;

                double offsetX = device.getBaseOffsetX();
                double offsetZ = device.getBaseOffsetZ();
                double offsetY = device.getBaseOffsetY();


                double finalLocalX;
                double finalLocalZ;

                switch (facing) {
                    case SOUTH -> {
                        finalLocalX = -offsetX;
                        finalLocalZ = -offsetZ;
                    }
                    case WEST -> {
                        finalLocalX = offsetZ;
                        finalLocalZ = -offsetX;
                    }
                    case EAST -> {
                        finalLocalX = -offsetZ;
                        finalLocalZ = offsetX;
                    }
                    default -> {
                        finalLocalX = offsetX;
                        finalLocalZ = offsetZ;
                    }
                }

                double finalXOffset = finalLocalX;
                double finalZOffset = finalLocalZ;

                return new Vec3(finalXOffset, offsetY, finalZOffset);
            }
        }
        return getDefaultPassengerAttachmentPoint(this, passenger, dimensions.attachments());
    }

    private void invokeBlockAction(Consumer<RestraintDevice> action) {
        BlockPos pos = getSourcePos();
        if (pos != null && this.level().getBlockState(pos).getBlock() instanceof RestraintDevice device) {
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
    protected void addPassenger(@NotNull Entity passenger) {
        super.addPassenger(passenger);
        if (!this.level().isClientSide && passenger instanceof LivingEntity living) {
            invokeBlockAction(device -> device.onMount(living, this.level(), sourcePos));
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        if (!this.level().isClientSide && passenger instanceof LivingEntity living) {
            invokeBlockAction(device -> device.onDismount(living, this.level(), sourcePos));

            if (this.isAlive() && sourcePos != null) {
                BlockPos targetPos = sourcePos;
                if (living.level().getBlockEntity(targetPos) instanceof GhostBlockEntity ghostBE) {
                    BlockPos master = ghostBE.getMasterPos();
                    if (master != null) {
                        targetPos = master;
                    }
                }

                if (living.level().getBlockState(targetPos).getBlock() instanceof RestraintDevice device) {
                    device.dismount(this.level(), targetPos, living);
                }
            }
        }
        super.removePassenger(passenger);
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (passenger instanceof LivingEntity living) {
            float seatYaw = this.getYRot();
            living.yBodyRot = seatYaw;
            living.yBodyRotO = seatYaw;
            living.setYHeadRot(living.getYRot());
        }
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity passenger) {

        return super.getPassengerRidingPosition(passenger);
    }

    @Override
    public void onPassengerTurned(@NotNull Entity passenger) {
        this.clampPassengerRotation(passenger);
    }


    private void clampPassengerRotation(Entity passenger) {

        passenger.setYBodyRot(this.getYRot());

        float maxRotationAngle = 105.0F;

        BlockPos pos = getSourcePos();
        if (pos != null && this.level().getBlockState(pos).getBlock() instanceof RestraintDevice device) {
            maxRotationAngle = device.getMaxHeadRotation();
        }

        float angleDifference = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());

        float clampedDifference = Mth.clamp(angleDifference, -maxRotationAngle, maxRotationAngle);

        passenger.yRotO += clampedDifference - angleDifference;
        passenger.setYRot(passenger.getYRot() + clampedDifference - angleDifference);

        passenger.setYHeadRot(passenger.getYRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(SOURCE_POS, BlockPos.ZERO);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("SourcePos")) {
            this.entityData.set(SOURCE_POS, BlockPos.of(tag.getLong("SourcePos")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        BlockPos pos = getSourcePos();
        if (pos != null) {
            tag.putLong("SourcePos", pos.asLong());
        }
    }

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    public boolean canBeCollidedWith() { return false; }
}