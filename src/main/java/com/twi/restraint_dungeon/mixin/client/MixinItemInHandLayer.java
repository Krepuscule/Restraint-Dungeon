package com.twi.restraint_dungeon.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.utils.ActionManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.getCurrentActionId;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isCarrier;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindArms;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

@Mixin(ItemInHandLayer.class)
public class MixinItemInHandLayer {

    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderArmWithItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {

        if(isDoingAction(entity)){
            if(entity instanceof Player player) {
                BaseAction action = ActionManager.get(getCurrentActionId(player));
                if(action != null  && !action.renderMainHandItem(player)) {
                    ci.cancel();
                }
            }
        }else{
            if (isBeenBindHands(entity) || isBeenBindArms(entity) || (entity instanceof Player player && isCarrier(player))) {
                ci.cancel();
            }
        }
    }
}
