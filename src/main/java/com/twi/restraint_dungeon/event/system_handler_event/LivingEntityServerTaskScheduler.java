package com.twi.restraint_dungeon.event.system_handler_event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class LivingEntityServerTaskScheduler {

    private static class GenericDelayTask {
        final WeakReference<LivingEntity> entityRef;
        final Runnable action;
        int remainingTicks;

        GenericDelayTask(LivingEntity entity, int ticks, Runnable action) {
            this.entityRef = new WeakReference<>(entity);
            this.remainingTicks = ticks;
            this.action = action;
        }
    }

    private static final List<GenericDelayTask> PENDING_TASKS = new ArrayList<>();
    private static final List<GenericDelayTask> TO_ADD = new ArrayList<>();

    /**
     * 在指定 ticks 后对实体执行一段代码
     * @param entity   绑定的实体（当实体死亡或离线时会自动取消任务）
     * @param ticks    延迟的 tick 数 (20 ticks = 1秒)
     * @param action   延迟结束后要执行的方法 (采用Lambda 表达式)
     */
    public static void runDelayed(LivingEntity entity, int ticks, Runnable action) {
        if (entity == null || entity.level().isClientSide || action == null) return;

        GenericDelayTask task = new GenericDelayTask(entity, ticks, action);

        synchronized (PENDING_TASKS) {
            TO_ADD.add(task);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        synchronized (PENDING_TASKS) {
            if (!TO_ADD.isEmpty()) {
                PENDING_TASKS.addAll(TO_ADD);
                TO_ADD.clear();
            }
        }

        if (PENDING_TASKS.isEmpty()) return;

        for (int i = PENDING_TASKS.size() - 1; i >= 0; i--) {
            GenericDelayTask task = PENDING_TASKS.get(i);
            LivingEntity entity = task.entityRef.get();

            if (entity == null || !entity.isAlive()) {
                PENDING_TASKS.remove(i);
                continue;
            }

            task.remainingTicks--;

            if (task.remainingTicks <= 0) {
                PENDING_TASKS.remove(i);

                try {
                    task.action.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}