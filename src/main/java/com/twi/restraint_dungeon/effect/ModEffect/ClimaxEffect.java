package com.twi.restraint_dungeon.effect.ModEffect;

import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.NotNull;

public class ClimaxEffect extends MobEffect {

    public ClimaxEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            PleasantUtils.updatePleasantValue(entity, 100.0);
        }
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return true;

        ServerLevel serverLevel = (ServerLevel) entity.level();

        // 1. 生成粒子效果
        for (int i = 0; i < 1 + amplifier; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth(),
                    entity.getY(0.5) + entity.getRandom().nextDouble() * entity.getBbHeight(),
                    entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth(),
                    1, 0.0, 0.0, 0.0, 0.03
            );
        }

        if (PleasantUtils.getPleasantValue(entity) != 100) {
            PleasantUtils.updatePleasantValue(entity, 100);
        }
        return true;
    }

    @Override
    public void removeAttributeModifiers(@NotNull AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
    }


    public void onEffectEnded(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            entity.addEffect(new MobEffectInstance(
                    ModEffects.CALM,
                    1200, 0, false, false
            ));
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
