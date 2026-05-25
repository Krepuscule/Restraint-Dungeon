package com.twi.restraint_dungeon.effect.ModEffect;

import com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ClimaxDenyEffect extends MobEffect {
    private final Random random = new Random();

    public ClimaxDenyEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return true;

        double currentPleasant = PleasantUtils.getPleasantValue(entity);

        // 如果快感值达到或超过 100，将其压制回 85-95 之间
        if (currentPleasant >= 100) {
            double clampedValue = 85 + random.nextInt(11);
            PleasantUtils.updatePleasantValue(entity, clampedValue);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}