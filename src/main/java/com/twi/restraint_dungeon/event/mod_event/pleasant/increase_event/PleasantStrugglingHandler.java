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
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;

@EventBusSubscriber(modid = MODID)
public class PleasantStrugglingHandler {
    private static final ConcurrentMap<UUID, Long> struggleStartTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;
        
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getThrillLevel(entity) <= 0) return;

        UUID uuid = entity.getUUID();

        // TODO:实现NPC功能后补充
//        if ((entity instanceof Player player || entity instanceof BaseNPCEntity npc)
        if (entity instanceof Player player
                && getIsStruggling(entity)) {
            long now = System.currentTimeMillis();
            Long startTime = struggleStartTimes.putIfAbsent(uuid, now);
            if (startTime != null && now - startTime >= getIncreaseInterval(entity)) {
                PleasantValueManager.performUpdate(entity, getIncreaseAmount(entity));
                struggleStartTimes.put(uuid, now);
            }
        } else{
            struggleStartTimes.remove(uuid);
        }
    }

    public static double getIncreaseAmount(LivingEntity entity) {
        return getThrillLevel(entity);
    }

    private static long getIncreaseInterval(LivingEntity entity) {
        return 5000L;
    }

    @SubscribeEvent
    public static void onEntityRemove(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        Entity.RemovalReason reason = entity.getRemovalReason();
        if (reason == null) return;

        if (reason == Entity.RemovalReason.KILLED || 
            reason == Entity.RemovalReason.DISCARDED || 
            reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            struggleStartTimes.remove(entity.getUUID());
        }
    }
}