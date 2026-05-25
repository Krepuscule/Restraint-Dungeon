package com.twi.restraint_dungeon.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;

@Mixin(Camera.class)
public class PlayerCameraMixin {
    @Shadow private Vec3 position;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow private float roll;
    @Shadow private final Quaternionf rotation = new Quaternionf();
    @Shadow private final Vector3f forwards = new Vector3f();
    @Shadow private final Vector3f up = new Vector3f();
    @Shadow private final Vector3f left = new Vector3f();

    private static final Vector3f BASE_FORWARDS = new Vector3f(0.0F, 0.0F, -1.0F);
    private static final Vector3f BASE_UP = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final Vector3f BASE_LEFT = new Vector3f(-1.0F, 0.0F, -0.0F);

    @Inject(method = "setup", at = @At("RETURN"))
    private void onSetupFinalize(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if(getRestraintPosition(player) != RestraintPosition.CARRIED){
            Camera camera = (Camera) (Object) this;

            if (detached) return;

            Vec3 currentOffset = getCurrentCameraOffset();


            boolean hasOffset = !currentOffset.equals(Vec3.ZERO);
            float offPitch = getCurrentPitchOffset();
            float offYaw = getCurrentYawOffset();
            float offRoll = getCurrentRollOffset();
            boolean hasRotation = offPitch != 0 || offYaw != 0 || offRoll != 0;

            if (!hasOffset && !hasRotation) return;

            Quaternionf mouseRotation = new Quaternionf();
            mouseRotation.rotationYXZ(
                    (float) Math.toRadians(180.0F - this.yRot),
                    (float) Math.toRadians(-this.xRot),
                    0.0F
            );

            Quaternionf poseOffsetRotation = new Quaternionf();
            poseOffsetRotation.rotationYXZ(
                    (float) Math.toRadians(-offYaw),
                    (float) Math.toRadians(-offPitch),
                    (float) Math.toRadians(-offRoll)
            );

            this.rotation.set(mouseRotation).mul(poseOffsetRotation);

            BASE_FORWARDS.rotate(this.rotation, this.forwards);
            BASE_UP.rotate(this.rotation, this.up);
            BASE_LEFT.rotate(this.rotation, this.left);


            this.roll = offRoll;

            if (hasOffset) {
                float absoluteBodyYaw;
                if (player.isSleeping() && player.getBedOrientation() != null) {
                    absoluteBodyYaw = player.getBedOrientation().toYRot();
                } else {
                    absoluteBodyYaw = player.yBodyRot;
                }

                Quaternionf bodyRotation = new Quaternionf().rotationY((float) Math.toRadians(180.0F - absoluteBodyYaw));

                Vector3f worldOffsetVec = new Vector3f((float) currentOffset.x, (float) currentOffset.y, (float) -currentOffset.z);
                worldOffsetVec.rotate(bodyRotation);

                double finalX = this.position.x + worldOffsetVec.x;
                double finalY = this.position.y + worldOffsetVec.y;
                double finalZ = this.position.z + worldOffsetVec.z;

                this.position = new Vec3(finalX, finalY, finalZ);
                ((BlockPos.MutableBlockPos) camera.getBlockPosition()).set(finalX, finalY, finalZ);
            }
        }else{

        }
    }
}
