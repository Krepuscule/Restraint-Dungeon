package com.twi.restraint_dungeon.mixin;

import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onPositionRider(Entity pPassenger, Entity.MoveFunction pCallback, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;

        if (self instanceof Player carrier && pPassenger instanceof LivingEntity passenger) {
            CarryType type = PlayerCarryUtils.getCurrentCarryType(carrier);

            if (type != null) {
                float vehicleYaw;
                float vehicleYawO;


                vehicleYaw = carrier.yBodyRot;
                vehicleYawO = carrier.yBodyRotO;


                float offset = type.getPassengerBodyRotation(carrier, passenger);
//                float finalYaw = vehicleYaw + offset;
//                float finalYawO = vehicleYawO + offset;

                float finalYaw = vehicleYaw;
                float finalYawO = vehicleYawO;

                passenger.yBodyRot = finalYaw;
                passenger.yBodyRotO = finalYawO;

                passenger.setYHeadRot(finalYaw);
                passenger.yHeadRotO = finalYawO;

                passenger.setYRot(finalYaw);
                passenger.yRotO = finalYawO;

                Vec3 localOffset = type.getPassengerRidingOffset(carrier, passenger);

                double rad = Math.toRadians(vehicleYaw);
                double tx = localOffset.x * Math.cos(rad) + localOffset.z * Math.sin(rad);
                double ty = localOffset.y;
                double tz = localOffset.x * Math.sin(rad) - localOffset.z * Math.cos(rad);

                double targetX = carrier.getX() + tx;
                double targetY = carrier.getY() + ty;
                double targetZ = carrier.getZ() + tz;

                passenger.setPos(targetX, targetY, targetZ);
                if (passenger.level().isClientSide) {
                    passenger.xo = targetX;
                    passenger.yo = targetY;
                    passenger.zo = targetZ;
                }

                passenger.setDeltaMovement(Vec3.ZERO);
                passenger.fallDistance = 0;
                passenger.setOnGround(true);

//                if (!self.level().isClientSide && passenger instanceof ServerPlayer serverPassenger) {
//                    serverPassenger.connection.send(new ClientboundMoveEntityPacket.Rot(
//                            passenger.getId(),
//                            (byte) Mth.floor(passenger.getYRot() * 256.0F / 360.0F),
//                            (byte) Mth.floor(passenger.getXRot() * 256.0F / 360.0F),
//                            passenger.onGround()
//                    ));
//                }

                ci.cancel();
            }
        }
    }

    @Inject(method = "canChangeDimensions", at = @At("HEAD"), cancellable = true)
    public void denyPassengerChangeDimension(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        if (self instanceof LivingEntity living && PlayerCarryUtils.isBeingCarried(living)) {
            cir.setReturnValue(false);
        }
    }
}