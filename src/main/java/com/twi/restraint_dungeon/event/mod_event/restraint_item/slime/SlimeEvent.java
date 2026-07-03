package com.twi.restraint_dungeon.event.mod_event.restraint_item.slime;

import com.twi.restraint_dungeon.effect.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class SlimeEvent {

    private static final String TIMER_KEY = MODID + ".sticky_timer";
    private static final int TRIGGER_INTERVAL = 1200;

    @SubscribeEvent
    public static void StickyEffectCounter(EntityTickEvent.Post event) {

        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof LivingEntity livingEntity) {

            if (!livingEntity.hasEffect(ModEffects.STICKY)) {
                if (livingEntity.getPersistentData().contains(TIMER_KEY)) {
                    livingEntity.getPersistentData().remove(TIMER_KEY);
                }
                return;
            }

            if (livingEntity.isInWaterRainOrBubble()) {
                livingEntity.removeEffect(ModEffects.STICKY);
                // 效果消失，重置并清除计时器
                livingEntity.getPersistentData().remove(TIMER_KEY);
                return;
            }

            CompoundTag nbt = livingEntity.getPersistentData();

            int currentTicks = nbt.getInt(TIMER_KEY);

            currentTicks++;

            if (currentTicks >= TRIGGER_INTERVAL) {
                var effectInstance = livingEntity.getEffect(ModEffects.STICKY);
                if (effectInstance != null) {
                    int amplifier = effectInstance.getAmplifier();

                    ModEffects.STICKY.get().applyEffectTick(livingEntity, amplifier);
                }

                nbt.putInt(TIMER_KEY, 0);
            } else {
                nbt.putInt(TIMER_KEY, currentTicks);
            }
        }
    }
}
