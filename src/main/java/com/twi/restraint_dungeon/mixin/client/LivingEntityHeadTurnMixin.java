package com.twi.restraint_dungeon.mixin.client;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHeadTurnMixin {

    @Shadow public float yBodyRot;
    @Shadow public float yHeadRot;

    @Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
    private void preventBodyTurnWhenAction(float p_21260_, float p_21261_, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (isDoingAction(entity)) {
            float f1 = Mth.wrapDegrees(p_21260_ - this.yBodyRot);
            boolean flag = f1 < -90.0F || f1 >= 90.0F;
            if (flag) {
                p_21261_ *= -1.0F;
            }
            cir.setReturnValue(p_21261_);
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void lockHeadAndBodyRotation(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (isDoingAction(entity)) {
             this.yHeadRot = this.yBodyRot;
        }
    }
}