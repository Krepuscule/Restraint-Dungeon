package com.twi.restraint_dungeon.event.mod_event.pleasant.increase_event;

import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;

@EventBusSubscriber(modid = MODID)
public class PleasantCrouchingHandler {
    private static final ConcurrentMap<UUID, Long> crouchStartTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        // 状态检查
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getThrillLevel(entity) <= 0) return;

        UUID uuid = entity.getUUID();
        boolean isCrouchMoving = entity.isCrouching() && entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;

        if (isCrouchMoving) {
            long now = System.currentTimeMillis();
            Long startTime = crouchStartTimes.putIfAbsent(uuid, now);
            if (startTime != null) {
                if (now - startTime >= getIncreaseInterval(entity)) {
                    PleasantValueManager.performUpdate(entity, getIncreaseAmount(entity));
                    crouchStartTimes.put(uuid, now);
                }
            }
        } else {
            crouchStartTimes.remove(uuid);
        }
    }

    public static double getIncreaseAmount(LivingEntity entity) {
        return getThrillLevel(entity);
    }

    private static long getIncreaseInterval(LivingEntity entity) {
        return 10000L; // 10秒
    }

    @SubscribeEvent
    public static void onEntityRemove(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        Entity.RemovalReason reason = entity.getRemovalReason();

        if (reason == null) return;

        if (reason == Entity.RemovalReason.KILLED ||                    // 死亡
                reason == Entity.RemovalReason.DISCARDED ||             // 指令等逻辑清理
                reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {  // 离线

            // 执行清理
            crouchStartTimes.remove(entity.getUUID());
        }
    }
}