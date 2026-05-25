package com.twi.restraint_dungeon.block.restraint_device;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import java.util.Objects;

public class RestraintDeviceModel<T extends RestraintDeviceEntity> extends GeoModel<T> {

    public RestraintDeviceModel() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        String modId = Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(animatable.getType())).getNamespace();
        return ResourceLocation.fromNamespaceAndPath(
                modId,
                "geo/block/restraint_device/" + animatable.getModelName() + ".geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        String modId = Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(animatable.getType())).getNamespace();
        return ResourceLocation.fromNamespaceAndPath(
                modId,
                "textures/block/restraint_device/" + animatable.getModelName() + "/textures/" + animatable.getTextureName() + ".png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        String modId = Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(animatable.getType())).getNamespace();
        return ResourceLocation.fromNamespaceAndPath(
                modId,
                "animations/block/restraint_device/" + animatable.getModelName() + ".animation.json"
        );
    }
}