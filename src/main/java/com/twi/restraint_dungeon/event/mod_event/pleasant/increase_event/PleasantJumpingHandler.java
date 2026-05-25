package com.twi.restraint_dungeon.event.mod_event.pleasant.increase_event;

import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;

@EventBusSubscriber(modid = MODID)
public class PleasantJumpingHandler {
    private static class JumpRecord {
        public int count = 0;
        public long firstJumpTime = 0;
    }

    private static final ConcurrentMap<UUID, JumpRecord> jumpMap = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getThrillLevel(entity) <= 0) return;

        UUID uuid = entity.getUUID();
        long now = System.currentTimeMillis();
        JumpRecord record = jumpMap.computeIfAbsent(uuid, k -> new JumpRecord());

        if (now - record.firstJumpTime > getIncreaseInterval(entity)) {
            record.count = 1;
            record.firstJumpTime = now;
        } else {
            record.count++;
            if (record.count >= 5) {
                PleasantValueManager.performUpdate(entity, getIncreaseAmount(entity));
                record.count = 0;
                record.firstJumpTime = 0;
            }
        }
    }

    public static double getIncreaseAmount(LivingEntity entity) {
        return getThrillLevel(entity);
    }

    private static long getIncreaseInterval(LivingEntity entity) {
        return 10000L;
    }

    @SubscribeEvent
    public static void onEntityRemove(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        Entity.RemovalReason reason = entity.getRemovalReason();
        if (reason == null) return;

        if (reason == Entity.RemovalReason.KILLED || 
            reason == Entity.RemovalReason.DISCARDED || 
            reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            jumpMap.remove(entity.getUUID());
        }
    }
}