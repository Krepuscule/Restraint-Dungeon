package com.twi.restraint_dungeon.event.mod_event.pleasant.decrease_event;


import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;

@EventBusSubscriber(modid = MODID)
public class NormalDecayHandler {
    private static final int INACTIVITY_TICKS = 1800;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        // 状态检查
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getPleasantValue(entity) <= 0) return;

        UUID uuid = entity.getUUID();
        int currentTick = entity.tickCount;

        // 检查距离上次数值增加的时间
        int lastInc = PleasantValueManager.getLastIncreaseTick(uuid);
        if (currentTick - lastInc >= INACTIVITY_TICKS) {

            int lastDec = PleasantValueManager.getLastDecayTick(uuid);
            if (currentTick - lastDec >= getDecayInterval(entity)) {
                PleasantValueManager.performDecay(entity, getDecayAmount(entity));
            }
        }
    }

    /**
     * 获取单次自然衰减的数值
     */
    private static double getDecayAmount(LivingEntity entity) {
        return 5.0;
    }

    /**
     * 获取衰减触发的 Tick 间隔
     */
    private static int getDecayInterval(LivingEntity entity) {
        return 300;
    }
}