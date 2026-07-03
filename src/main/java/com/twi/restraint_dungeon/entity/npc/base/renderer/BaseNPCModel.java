package com.twi.restraint_dungeon.entity.npc.base.renderer;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class BaseNPCModel extends GeoModel<BaseNPCEntity> {

    @Override
    public ResourceLocation getModelResource(BaseNPCEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(animatable.getModId(), animatable.getModelLocation());
    }

    @Override
    public ResourceLocation getTextureResource(BaseNPCEntity animatable) {
        int index = animatable.getSkinIndex();
        String path = animatable.getTextureFolder() + index + ".png";
        return ResourceLocation.fromNamespaceAndPath(animatable.getModId(), path);
    }

    @Override
    public ResourceLocation getAnimationResource(BaseNPCEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(animatable.getModId(), animatable.getAnimationLocation());
    }

    @Override
    public void setCustomAnimations(BaseNPCEntity animatable, long instanceId, AnimationState<BaseNPCEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        GeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
            head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
        }
    }
}