package com.twi.restraint_dungeon.block.restraint_device;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RestraintDeviceRenderer<T extends RestraintDeviceEntity> extends GeoBlockRenderer<T> {
    public RestraintDeviceRenderer() {
        super(new RestraintDeviceModel<>());
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            default -> {} // NORTH
        }
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(T animatable) {
        return new AABB(animatable.getBlockPos()).inflate(2.0);
    }
}