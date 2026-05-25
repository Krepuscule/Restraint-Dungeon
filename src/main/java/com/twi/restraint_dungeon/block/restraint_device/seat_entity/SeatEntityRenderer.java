package com.twi.restraint_dungeon.block.restraint_device.seat_entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class SeatEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public SeatEntityRenderer(EntityRendererProvider.Context context) {

        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return null;
    }
}