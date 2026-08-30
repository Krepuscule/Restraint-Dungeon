package com.twi.restraint_dungeon.mixin.client;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;

@Mixin(Entity.class)
public abstract class EntityPoseLockMixin {

    @Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
    private void preventUnwantedPose(Pose pose, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (getRestraintPosition(entity) != RestraintPositionEvent.RestraintPosition.STANDING) {
            if (pose == Pose.SWIMMING) {
                ci.cancel();
            }
        }
    }
}