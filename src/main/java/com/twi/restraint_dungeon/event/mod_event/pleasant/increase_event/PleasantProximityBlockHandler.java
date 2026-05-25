package com.twi.restraint_dungeon.event.mod_event.pleasant.increase_event;

import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;

@EventBusSubscriber(modid = MODID)
public class PleasantProximityBlockHandler {
    private static final ConcurrentMap<UUID, Long> proximityStartTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;
        
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getThrillLevel(entity) <= 0) return;

        UUID uuid = entity.getUUID();

        if (isNearValidBlock(entity)) {
            long now = System.currentTimeMillis();
            Long startTime = proximityStartTimes.putIfAbsent(uuid, now);
            if (startTime != null && now - startTime >= getIncreaseInterval(entity)) {
                PleasantValueManager.performUpdate(entity, getIncreaseAmount(entity));
                proximityStartTimes.put(uuid, now);
            }
        } else {
            proximityStartTimes.remove(uuid);
        }
    }

    private static boolean isNearValidBlock(LivingEntity entity) {
        Level level = entity.level();
        BlockPos basePos = entity.blockPosition();
        Set<Block> whitelist = getProximityWhitelist();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockState state = level.getBlockState(basePos.offset(x, y, z));
                    if (whitelist.contains(state.getBlock())) return true;
                }
            }
        }
        return false;
    }

    private static Set<Block> getProximityWhitelist() {
        return Set.of(Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND);
    }

    private static double getIncreaseAmount(LivingEntity entity) {
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
            proximityStartTimes.remove(entity.getUUID());
        }
    }
}