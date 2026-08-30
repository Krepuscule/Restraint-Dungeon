package com.twi.restraint_dungeon.entity.npc.base.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindArms;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

public class BaseNPCRenderer extends GeoEntityRenderer<BaseNPCEntity> {

    public BaseNPCRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BaseNPCModel());

        this.addRenderLayer(new GeoRestraintTextureLayer(this));
        this.addRenderLayer(new GeoRestraintToolTextureLayer(this));


        this.addRenderLayer(new BlockAndItemGeoLayer<BaseNPCEntity>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, BaseNPCEntity animatable) {
                if (isBeenBindArms(animatable) || isBeenBindHands(animatable)) {
                    return null;
                }

                return switch (bone.getName()) {
                    case "right_arm_bend" -> animatable.getMainHandItem();
                    case "left_arm_bend" -> animatable.getOffhandItem();
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, BaseNPCEntity animatable) {
                return switch (bone.getName()) {
                    case "left_arm_bend" -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                    case "right_arm_bend" -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, BaseNPCEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                String boneName = bone.getName();

                if (boneName.equals("right_arm_bend") || boneName.equals("left_arm_bend")) {
                    poseStack.pushPose();

                    poseStack.translate(0, 0, -0.125f);

                    if (boneName.equals("left_arm_bend") && stack.getItem() instanceof ShieldItem) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                        poseStack.translate(0, 0.1f, -1.0f);
                    } else {
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    }

                    poseStack.translate(0, 0, -0.4f);

                    super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                    poseStack.popPose();
                } else {
                    super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                }
            }
        });
    }

    @Override
    public void render(@NotNull BaseNPCEntity animatable,
                       float entityYaw,
                       float partialTick,
                       @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource,int packedLight) {
        super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);


    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BaseNPCEntity entity) {
        return this.model.getTextureResource(entity);
    }
}