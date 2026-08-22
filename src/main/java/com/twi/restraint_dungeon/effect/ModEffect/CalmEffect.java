package com.twi.restraint_dungeon.effect.ModEffect;

import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class CalmEffect extends MobEffect {

    public CalmEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            PleasantUtils.updatePleasantValue(entity, 0.0);
        }
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return true;


        if (entity.hasEffect(ModEffects.CLIMAX)) {
            entity.removeEffect(ModEffects.CLIMAX);
        }
        if (entity.hasEffect(ModEffects.CLIMAX_DENY)) {
            entity.removeEffect(ModEffects.CLIMAX_DENY);
        }

        if (PleasantUtils.getPleasantValue(entity) != 0) {
            PleasantUtils.updatePleasantValue(entity, 0);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}