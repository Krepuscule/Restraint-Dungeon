package com.twi.restraint_dungeon.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;

public class RestraintItemLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final M innerModel;

    public RestraintItemLayer(RenderLayerParent<T, M> parent, M innerModel) {
        super(parent);
        this.innerModel = innerModel;
    }

    @Override
    public void render(@NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource,
                       int packedLight,
                       @NotNull T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        M parentModel = this.getParentModel();

        parentModel.copyPropertiesTo(this.innerModel);

        this.innerModel.head.visible = true;
        this.innerModel.body.visible = true;
        this.innerModel.rightArm.visible = true;
        this.innerModel.leftArm.visible = true;
        this.innerModel.rightLeg.visible = true;
        this.innerModel.leftLeg.visible = true;

        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {

            var list = getAllRestraintsByPart(entity, part);

            for (int index = 0; index < list.size(); index++) {
                ItemStack stack = list.get(index);
                if (stack.getItem() instanceof RestraintItem item) {


                    int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);

                    item.applyRestraintVisibility(innerModel, parentModel,null,stack,part,index, entity);

                    item.setLayerInflation(innerModel,null,stack,part,index);

                    RestraintItem.restraintRenderData renderData =  item.renderRestraintLayer(
                            entity,
                            part,
                            index,
                            stack,
                            poseStack,
                            bufferSource,
                            packedLight,
                            overlay
                    );

                    if(renderData != null){
                        innerModel.renderToBuffer(renderData.stack(), renderData.baseBuffer(), renderData.packedLight(),renderData.packedOverlay());
                    }
                }
            }
        }
    }
}