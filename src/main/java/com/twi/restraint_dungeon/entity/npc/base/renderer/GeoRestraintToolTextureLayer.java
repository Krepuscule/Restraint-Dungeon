package com.twi.restraint_dungeon.entity.npc.base.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.partHasBeenBound;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils.getAllRestraintTools;

public class GeoRestraintToolTextureLayer extends GeoRenderLayer<BaseNPCEntity> {

    public GeoRestraintToolTextureLayer(GeoRenderer<BaseNPCEntity> entityRendererIn) {
        super(entityRendererIn);
    }


    @Override
    public void render(PoseStack poseStack, BaseNPCEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        List<String> boneList = List.of("head","torso","left_arm","right_arm","left_leg","right_leg"
                ,"left_arm_bend", "right_arm_bend", "left_leg_bend", "right_leg_bend");

        for (RestraintCapability.PlayerRestraintPart part : RestraintCapability.PlayerRestraintPart.values()) {
            var list = getAllRestraintTools(animatable);
            for (int index = 0; index < list.size(); index++) {
                ItemStack stack = list.get(index);

                if (stack.getItem() instanceof RestraintToolItem restraintTool) {

                    RestraintToolItem.restraintToolRenderData renderData = restraintTool.renderRestraintToolLayer(
                            animatable,
                            index,
                            stack,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay

                    );

                    hideAllGeoModelBones(bakedModel);

                    restraintTool.applyToolsVisibility(null,null,bakedModel,stack,index,animatable);

                    Map<String,List<Float>> originBoneScales = saveScaleForEveryBones(boneList,bakedModel);

                    if(renderData != null){
                        getRenderer().reRender(
                                bakedModel,
                                renderData.stack(),
                                bufferSource,
                                animatable,
                                renderData.renderType(),
                                renderData.baseBuffer(),
                                partialTick,
                                renderData.packedLight(),
                                renderData.packedOverlay(),
                                0xFFFFFFFF
                        );
                    }

                    recoverGeoBoneScales(boneList,bakedModel,originBoneScales);
                    recoverGeoModelVisibility(animatable,bakedModel);
                }
            }
        }
    }

    private void hideAllGeoModelBones(BakedGeoModel geoModel){
        geoModel.getBone("head").get().setHidden(true);
        geoModel.getBone("torso").get().setHidden(true);
        geoModel.getBone("left_arm").get().setHidden(true);
        geoModel.getBone("right_arm").get().setHidden(true);
        geoModel.getBone("left_leg").get().setHidden(true);
        geoModel.getBone("right_leg").get().setHidden(true);
        geoModel.getBone("left_arm_bend").get().setHidden(true);
        geoModel.getBone("right_arm_bend").get().setHidden(true);
        geoModel.getBone("left_leg_bend").get().setHidden(true);
        geoModel.getBone("right_leg_bend").get().setHidden(true);

        geoModel.getBone("headwear").get().setHidden(true);
        geoModel.getBone("jacket").get().setHidden(true);
        geoModel.getBone("left_arm_layer").get().setHidden(true);
        geoModel.getBone("left_arm_bend_layer").get().setHidden(true);
        geoModel.getBone("right_arm_layer").get().setHidden(true);
        geoModel.getBone("right_arm_bend_layer").get().setHidden(true);
        geoModel.getBone("left_leg_layer").get().setHidden(true);
        geoModel.getBone("left_leg_bend_layer").get().setHidden(true);
        geoModel.getBone("right_leg_layer").get().setHidden(true);
        geoModel.getBone("right_leg_bend_layer").get().setHidden(true);
    }

    private void recoverGeoModelVisibility(BaseNPCEntity npc,BakedGeoModel geoModel){
        geoModel.getBone("head").get().setHidden(false);
        geoModel.getBone("torso").get().setHidden(false);
        geoModel.getBone("left_arm").get().setHidden(false);
        geoModel.getBone("right_arm").get().setHidden(false);
        geoModel.getBone("left_leg").get().setHidden(false);
        geoModel.getBone("right_leg").get().setHidden(false);
        geoModel.getBone("left_arm_bend").get().setHidden(false);
        geoModel.getBone("right_arm_bend").get().setHidden(false);
        geoModel.getBone("left_leg_bend").get().setHidden(false);
        geoModel.getBone("right_leg_bend").get().setHidden(false);

        geoModel.getBone("headwear").get().setHidden(false);
        if(partHasBeenBound(npc, RestraintCapability.PlayerRestraintPart.restraint_body_bind)){
            geoModel.getBone("jacket").get().setHidden(true);
        }else{
            geoModel.getBone("jacket").get().setHidden(false);
        }
        if(partHasBeenBound(npc, RestraintCapability.PlayerRestraintPart.restraint_arms_bind) || partHasBeenBound(npc, RestraintCapability.PlayerRestraintPart.restraint_hands_bind)){
            geoModel.getBone("left_arm_layer").get().setHidden(true);
            geoModel.getBone("left_arm_bend_layer").get().setHidden(true);
            geoModel.getBone("right_arm_layer").get().setHidden(true);
            geoModel.getBone("right_arm_bend_layer").get().setHidden(true);
        }else{
            geoModel.getBone("left_arm_layer").get().setHidden(false);
            geoModel.getBone("left_arm_bend_layer").get().setHidden(false);
            geoModel.getBone("right_arm_layer").get().setHidden(false);
            geoModel.getBone("right_arm_bend_layer").get().setHidden(false);
        }
        if(partHasBeenBound(npc, RestraintCapability.PlayerRestraintPart.restraint_legs_bind)){
            geoModel.getBone("left_leg_layer").get().setHidden(true);
            geoModel.getBone("left_leg_bend_layer").get().setHidden(true);
            geoModel.getBone("right_leg_layer").get().setHidden(true);
            geoModel.getBone("right_leg_bend_layer").get().setHidden(true);
        }else{
            geoModel.getBone("left_leg_layer").get().setHidden(false);
            geoModel.getBone("left_leg_bend_layer").get().setHidden(false);
            geoModel.getBone("right_leg_layer").get().setHidden(false);
            geoModel.getBone("right_leg_bend_layer").get().setHidden(false);
        }
    }

    private Map<String,List<Float>> saveScaleForEveryBones(List<String> boneList,BakedGeoModel geoModel){

        Map<String,List<Float>> bonesScale = new HashMap<>();
        for(String bone : boneList){
            bonesScale.put(bone,List.of(
                            geoModel.getBone(bone).get().getScaleX(),
                            geoModel.getBone(bone).get().getScaleY(),
                            geoModel.getBone(bone).get().getScaleZ()
                    )
            );
        }

        return bonesScale;
    }

    private void recoverGeoBoneScales(List<String> boneList,BakedGeoModel geoModel,Map<String,List<Float>> originBoneScales){

        for(String bone : boneList){
            geoModel.getBone(bone).get().setScaleX(originBoneScales.get(bone).get(0));
            geoModel.getBone(bone).get().setScaleY(originBoneScales.get(bone).get(1));
            geoModel.getBone(bone).get().setScaleZ(originBoneScales.get(bone).get(2));
        }
    }
}
