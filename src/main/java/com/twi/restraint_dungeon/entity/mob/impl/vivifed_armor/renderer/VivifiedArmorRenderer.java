package com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.VivifiedArmorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

public class VivifiedArmorRenderer extends GeoEntityRenderer<VivifiedArmorEntity> {
    private EquipmentSlot currentOverrideSlot = null;

    public VivifiedArmorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VivifiedArmorModel());

        this.addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Override
            public void renderForBone(PoseStack poseStack, VivifiedArmorEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                      VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                if (bone.getName().equals("Right Leg") || bone.getName().equals("Left Leg")) {
                    ItemStack leggings = animatable.getItemBySlot(EquipmentSlot.LEGS);
                    ItemStack boots = animatable.getItemBySlot(EquipmentSlot.FEET);

                    if (!leggings.isEmpty()) {
                        currentOverrideSlot = EquipmentSlot.LEGS;
                        super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                    }
                    if (!boots.isEmpty()) {
                        currentOverrideSlot = EquipmentSlot.FEET;
                        super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                    }
                    currentOverrideSlot = null;
                } else {
                    currentOverrideSlot = null;
                    super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                }
            }

            @Override
            @Nullable
            protected ItemStack getArmorItemForBone(GeoBone bone, VivifiedArmorEntity animatable) {
                if (currentOverrideSlot != null && (bone.getName().equals("Right Leg") || bone.getName().equals("Left Leg"))) {
                    return animatable.getItemBySlot(currentOverrideSlot);
                }

                return switch (bone.getName()) {
                    case "Head" -> animatable.getItemBySlot(EquipmentSlot.HEAD);
                    case "Body", "Right Arm", "Left Arm" -> animatable.getItemBySlot(EquipmentSlot.CHEST);
                    case "Right Leg", "Left Leg" -> animatable.getItemBySlot(EquipmentSlot.LEGS);
                    default -> null;
                };
            }

            @Override
            @NotNull
            protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, VivifiedArmorEntity animatable) {
                if (currentOverrideSlot != null) {
                    return currentOverrideSlot;
                }
                return switch (bone.getName()) {
                    case "Head" -> EquipmentSlot.HEAD;
                    case "Body", "Right Arm", "Left Arm" -> EquipmentSlot.CHEST;
                    case "Right Leg", "Left Leg" -> EquipmentSlot.LEGS;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Override
            @NotNull
            protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, VivifiedArmorEntity animatable, HumanoidModel<?> baseModel) {
                // 将每个自定义骨骼精准映射到原版模型的对应部位，解决错位与脱节问题
                return switch (bone.getName()) {
                    case "Head" -> baseModel.head;
                    case "Body" -> baseModel.body;
                    case "Right Arm" -> baseModel.rightArm;
                    case "Left Arm" -> baseModel.leftArm;
                    case "Right Leg" -> baseModel.rightLeg;
                    case "Left Leg" -> baseModel.leftLeg;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }
        });
    }

    @Override
    public RenderType getRenderType(VivifiedArmorEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}