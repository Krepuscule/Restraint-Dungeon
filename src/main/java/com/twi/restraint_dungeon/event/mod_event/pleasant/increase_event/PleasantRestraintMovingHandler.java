package com.twi.restraint_dungeon.event.mod_event.pleasant.increase_event;

import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;

@EventBusSubscriber(modid = MODID)
public class PleasantRestraintMovingHandler {

    private static class MoveRecord {
        public int count = 0;
        public boolean lastState = false;
    }

    private static final ConcurrentMap<UUID, MoveRecord> moveRecords = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity.level().isClientSide) return;

        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getThrillLevel(entity) <= 0) return;

        UUID uuid = entity.getUUID();

        boolean currentState = entity instanceof Player player &&  isPlayerRestraintMoving(player);
        MoveRecord record = moveRecords.computeIfAbsent(uuid, k -> new MoveRecord());

        // --- 边缘检测逻辑 ---
        if (currentState && !record.lastState) {
            record.count++;

            if (record.count >= getRequiredMoveCount(entity)) {
                PleasantValueManager.performUpdate(entity, getMoveIncreaseAmount(entity));
                record.count = 0;
            }
        }

        record.lastState = currentState;
    }

    /**
     * 判定需要多少次移动才触发一次增加
     */
    public static int getRequiredMoveCount(LivingEntity entity) {
        return 5;
    }

    /**
     * 获取每次触发叠加的具体数值
     */
    public static double getMoveIncreaseAmount(LivingEntity entity) {
        return getThrillLevel(entity);
    }

    /**
     * 统一清理逻辑：死亡、离线、被清除
     */
    @SubscribeEvent
    public static void onEntityRemove(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        Entity.RemovalReason reason = entity.getRemovalReason();

        if (reason == null) return;

        if (reason == Entity.RemovalReason.KILLED || 
            reason == Entity.RemovalReason.DISCARDED || 
            reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            
            moveRecords.remove(entity.getUUID());
        }
    }
}