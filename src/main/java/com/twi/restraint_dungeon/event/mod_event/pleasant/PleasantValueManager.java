package com.twi.restraint_dungeon.event.mod_event.pleasant;

import com.twi.restraint_dungeon.event.custom_event.PleasantValueEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.DecreasePleasantValue;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.IncreasePleasantValue;

public class PleasantValueManager {
    private static final ConcurrentMap<UUID, Integer> lastIncreaseTicks = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, Integer> lastDecayTicks = new ConcurrentHashMap<>();



    public static void performUpdate(LivingEntity entity, double amount) {
        if (entity.level().isClientSide || amount <= 0) return;

        PleasantValueEvent.Increase event = new PleasantValueEvent.Increase(entity, amount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;

        IncreasePleasantValue(entity, event.getAmount());
        lastIncreaseTicks.put(entity.getUUID(), entity.tickCount);
    }

    public static void performDecay(LivingEntity entity, double amount) {
        if (entity.level().isClientSide || amount <= 0) return;

        PleasantValueEvent.Decrease event = new PleasantValueEvent.Decrease(entity, amount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;

        DecreasePleasantValue(entity, event.getAmount());
        lastDecayTicks.put(entity.getUUID(), entity.tickCount);
    }

    public static void clearData(UUID uuid) {
        lastIncreaseTicks.remove(uuid);
        lastDecayTicks.remove(uuid);
    }

    /**
     * 获取实体最后一次高潮值增加的 Tick 时间
     * 如果没有记录，返回 0
     */
    public static int getLastIncreaseTick(UUID uuid) {
        return lastIncreaseTicks.getOrDefault(uuid, 0);
    }

    /**
     * 获取实体最后一次高潮值减少（衰减）的 Tick 时间
     * 如果没有记录，返回 0
     */
    public static int getLastDecayTick(UUID uuid) {
        return lastDecayTicks.getOrDefault(uuid, 0);
    }
}