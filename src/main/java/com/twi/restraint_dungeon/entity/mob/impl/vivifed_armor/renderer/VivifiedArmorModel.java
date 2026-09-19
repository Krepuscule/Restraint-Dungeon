package com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.renderer;

import com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.VivifiedArmorEntity;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class VivifiedArmorModel extends GeoModel<VivifiedArmorEntity> {
    @Override
    public ResourceLocation getModelResource(VivifiedArmorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "geo/entity/vivified_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VivifiedArmorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/vivified_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VivifiedArmorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/entity/vivified_armor.animation.json");
    }

    @Override
    public void setCustomAnimations(VivifiedArmorEntity animatable, long instanceId, AnimationState<VivifiedArmorEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        GeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
            head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
        }
    }
}