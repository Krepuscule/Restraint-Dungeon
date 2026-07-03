package com.twi.restraint_dungeon.mixin;

import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "canChangeDimensions", at = @At("HEAD"), cancellable = true)
    public void denyPassengerChangeDimension(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        if (self instanceof LivingEntity living && PlayerCarryUtils.isBeingCarried(living)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getPassengerAttachmentPoint(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityDimensions;F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetPassengerAttachmentPoint(Entity pPassenger, EntityDimensions pDimensions, float pScale, CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;

        if (self instanceof Player carrier && pPassenger instanceof LivingEntity passenger
                && getRestraintPosition(passenger) == RestraintPositionEvent.RestraintPosition.CARRIED) {
            CarryType type = PlayerCarryUtils.getCurrentCarryType(carrier);

            if (type != null) {
                Vec3 localOffset = type.getPassengerRidingOffset(carrier, passenger);


                float vehicleYaw = carrier.yBodyRot;
                double rad = Math.toRadians(vehicleYaw);

                double tx = localOffset.x * Math.cos(rad) + localOffset.z * Math.sin(rad);
                double ty = localOffset.y;
                double tz = localOffset.x * Math.sin(rad) - localOffset.z * Math.cos(rad);


                cir.setReturnValue(new Vec3(tx, ty, tz));
            }
        }
    }


    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At("TAIL"))
    private void onPositionRiderAngles(Entity pPassenger, Entity.MoveFunction pCallback, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;

        if (self instanceof Player carrier && pPassenger instanceof LivingEntity passenger) {
            CarryType type = PlayerCarryUtils.getCurrentCarryType(carrier);
            if (type != null) {
                float vehicleYaw = carrier.yBodyRot;
                float vehicleYawO = carrier.yBodyRotO;

                passenger.yBodyRot = vehicleYaw;
                passenger.yBodyRotO = vehicleYawO;

                passenger.setYHeadRot(vehicleYaw);
                passenger.yHeadRotO = vehicleYawO;

                passenger.setYRot(vehicleYaw);
                passenger.yRotO = vehicleYawO;

                passenger.fallDistance = 0;
                passenger.setOnGround(true);
            }
        }
    }
}