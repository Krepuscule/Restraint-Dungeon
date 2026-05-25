package com.twi.restraint_dungeon.block.addon_block.placed_sword;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlacedSwordBlockRenderer implements BlockEntityRenderer<PlacedSwordBlockEntity> {
    private final ItemRenderer itemRenderer;

    public PlacedSwordBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(PlacedSwordBlockEntity be,
                       float partialTicks,
                       @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int combinedLight,
                       int combinedOverlay) {
        ItemStack stack = be.getSword();
        if (stack.isEmpty()) return;

        Direction face = be.getBlockState().getValue(PlacedSwordBlock.FACING);

        poseStack.pushPose();

        // 沿用你的坐标偏置
        switch (face) {
            case NORTH -> {
                poseStack.translate(0.5, 0.5, 0.7);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-45f));
            }
            case SOUTH -> {
                poseStack.translate(0.5, 0.5, 0.2);
                poseStack.mulPose(Axis.XP.rotationDegrees(270f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(135f));
            }
            case WEST -> {
                poseStack.translate(0.7, 0.5, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(45f));
            }
            case EAST -> {
                poseStack.translate(0.2, 0.5, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-135f));
            }
            // UP/DOWN 保持原样或根据需要调整
            default -> poseStack.translate(0.5, 0.5, 0.5);
        }

        this.itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                combinedLight,
                combinedOverlay,
                poseStack,
                buffer,
                be.getLevel(),
                0
        );

        poseStack.popPose();
    }
}