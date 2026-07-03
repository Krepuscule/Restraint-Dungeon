package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;

import java.util.Objects;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ReleaseAction extends CarryingAction {

    public static final TagKey<EntityType<?>> ALLOWED_VEHICLES_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(MODID, "release_action_rideable_whitelist")
    );

    @Override
    public String getActionId() {
        return "RELEASE";
    }

    @Override
    public int getAnimTicks() {
        return 1;
    }

    @Override
    public double getMaxDistance() {
        return 3.0;
    }

    @Override
    public boolean shouldEndCarry() {
        return true;
    }

    @Override
    public @Nullable Component canUse(Player carrier, HitResult result) {
        if(super.canUse(carrier, result) != null){
            return super.canUse(carrier,result);
        }

        LivingEntity target = PlayerCarryUtils.getCarriedPassenger(carrier);
        if (target == null) return null;

        ReleaseResult releaseResult = findValidReleasePoint(carrier, target, result);

        if (releaseResult == null) {
            return Component.translatable("action." + MODID + ".fail_release.no_place").withStyle(ChatFormatting.RED);
        }

        if (releaseResult.failureReason != null) {
            return releaseResult.failureReason;
        }

        return null;
    }

    @Override
    public void onFinish(ServerPlayer carrier, LivingEntity target,HitResult hitResult) {
        HitResult raytrace = carrier.pick(getMaxDistance(), 1.0F, false);
        ReleaseResult result = findValidReleasePoint(carrier, target, raytrace);

        PlayerCarryUtils.stopCarrying(carrier);

        boolean handled = false;
        if (result != null && result.failureReason == null) {
            if (result.isRestraintDevice && result.blockPos != null) {

                BlockPos DevicePos = result.blockPos;

                if (carrier.level().getBlockEntity(result.blockPos) instanceof GhostBlockEntity ghostBE) {
                    BlockPos master = ghostBE.getMasterPos();
                    if (master != null) {
                         DevicePos = master;
                    }
                }

                if (carrier.level().getBlockState(DevicePos).getBlock() instanceof RestraintDevice device) {
                    handled = device.mount(carrier.level(), result.blockPos, target);
                }

            } else if (result.vehicle != null) {
                handled = target.startRiding(result.vehicle, true);
            } else if (result.pos != null) {
                target.teleportTo(result.pos.x, result.pos.y, result.pos.z);
                handled = true;
            }
        }

        if (!handled) {
            target.teleportTo(carrier.getX(), carrier.getY(), carrier.getZ());
        }
    }

    private ReleaseResult findValidReleasePoint(Player carrier, LivingEntity target, HitResult result) {
        Level level = carrier.level();

        if (result instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
            BlockPos hitPos = blockHit.getBlockPos();

            BlockPos actualPos = hitPos;
            if (level.getBlockEntity(hitPos) instanceof GhostBlockEntity ghostBE) {
                BlockPos master = ghostBE.getMasterPos();
                if (master != null) actualPos = master;
            }

            if (level.getBlockState(actualPos).getBlock() instanceof RestraintDevice device && device.isMountableDevice(level, actualPos)) {
                if (device.canMount(level, actualPos, target) != null) {
                    return new ReleaseResult(device.canMount(level, actualPos, target));
                } else {
                    return new ReleaseResult(actualPos, true);
                }
            }
        }

        double limitDist = getMaxDistance();
        Vec3 eyePos = carrier.getEyePosition();
        Vec3 lookVec = carrier.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(limitDist));

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                carrier, eyePos, endPos,
                carrier.getBoundingBox().inflate(limitDist),
                (e) -> !e.isSpectator() && e.isPickable() && !e.equals(target) && !e.equals(carrier) && !(e instanceof ItemEntity),
                limitDist * limitDist
        );

        if (entityHit != null) {
            Entity vehicle = entityHit.getEntity();

            if (isAllowedVehicle(vehicle)) {
                if (vehicle.getPassengers().size() < getVehicleMaxCapacity(vehicle)) {
                    return new ReleaseResult(vehicle);
                } else {
                    return new ReleaseResult(Component.translatable("action." + MODID + ".fail_release.vehicle_has_full").withStyle(ChatFormatting.RED));
                }
            }
        }

        if (result instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
            if (blockHit.getDirection() == Direction.UP) {
                BlockPos posAbove = blockHit.getBlockPos().above();
                if (canFitEntity(level, target, posAbove)) {
                    return new ReleaseResult(Vec3.atBottomCenterOf(posAbove).add(0, 0.05, 0));
                }
            }
        }

        BlockHitResult blockClip = level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, carrier));
        if (blockClip.getType() == HitResult.Type.BLOCK) {
            Direction face = blockClip.getDirection();
            BlockPos targetPos = (face == Direction.UP) ? blockClip.getBlockPos().above() : blockClip.getBlockPos().relative(face);
            BlockPos safePos = findSafeSpaceNear(level, target, targetPos);
            if (safePos != null) {
                return new ReleaseResult(Vec3.atBottomCenterOf(safePos).add(0, 0.05, 0));
            }
        }

        return null;
    }


    private boolean isAllowedVehicle(Entity vehicle) {
        if (vehicle == null) return false;

        if (vehicle instanceof Boat ||
                vehicle instanceof AbstractMinecart ||
                vehicle instanceof AbstractHorse) {
            return true;
        }

        return false;
    }

    protected BlockPos findSafeSpaceNear(Level level, LivingEntity target, BlockPos start) {
        BlockPos[] candidates = {start, start.below(), start.north(), start.south(), start.west(), start.east(), start.above()};
        for (BlockPos pos : candidates) {
            if (canFitEntity(level, target, pos)) {
                return pos;
            }
        }
        return null;
    }

    private boolean canFitEntity(Level level, LivingEntity entity, BlockPos pos) {
        if (!isPassable(level, pos) || !isPassable(level, pos.above())) {
            return false;
        }
        if (level.getBlockState(pos.below()).isAir()) {
            return false;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.05;
        double z = pos.getZ() + 0.5;
        AABB box = entity.getType().getDimensions().makeBoundingBox(x, y, z);
        return level.noCollision(entity, box);
    }

    private boolean isPassable(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private int getVehicleMaxCapacity(Entity vehicle) {
        return (vehicle instanceof Boat) ? 2 : 1;
    }

    private static class ReleaseResult {
        final Entity vehicle;
        final Vec3 pos;
        final BlockPos blockPos;
        final boolean isRestraintDevice;
        final Component failureReason;

        ReleaseResult(Entity v) {
            this.vehicle = v; this.pos = null; this.blockPos = null; this.isRestraintDevice = false; this.failureReason = null;
        }
        ReleaseResult(Vec3 p) {
            this.pos = p; this.vehicle = null; this.blockPos = null; this.isRestraintDevice = false; this.failureReason = null;
        }
        ReleaseResult(BlockPos bp, boolean isDevice) {
            this.blockPos = bp; this.isRestraintDevice = isDevice; this.pos = null; this.vehicle = null; this.failureReason = null;
        }
        ReleaseResult(Component reason) {
            this.failureReason = reason; this.vehicle = null; this.pos = null; this.blockPos = null; this.isRestraintDevice = false;
        }
    }
}