package com.twi.restraint_dungeon.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RestraintItemLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {
    private final M innerModel;

    public RestraintItemLayer(RenderLayerParent<T, M> parent, M innerModel) {
        super(parent);
        this.innerModel = innerModel;
    }

    @Override
    public void render(@NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource,
                       int packedLight,
                       @NotNull T player,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        M parentModel = this.getParentModel();

        parentModel.copyPropertiesTo(this.innerModel);

        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            var list = RestraintStackUtils.getAllRestraintsByPart(player, part);

            for (int index = 0; index < list.size(); index++) {
                ItemStack stack = list.get(index);
                if (stack.getItem() instanceof RestraintItem item) {

                    item.renderRestraintLayer(
                            this.innerModel,
                            parentModel,
                            player,
                            part,
                            index,
                            stack,
                            poseStack,
                            bufferSource,
                            packedLight
                    );
                }
            }
        }
    }
}