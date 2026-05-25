package com.twi.restraint_dungeon.event.mod_event.pleasant.increase_event;

import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
public class PleasantStandingBlockHandler {
    private static final ConcurrentMap<UUID, Long> standingStartTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;
        
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getThrillLevel(entity) <= 0) return;

        UUID uuid = entity.getUUID();

        if (isStandingOnValidBlock(entity)) {
            long now = System.currentTimeMillis();
            Long startTime = standingStartTimes.putIfAbsent(uuid, now);
            if (startTime != null && now - startTime >= getIncreaseInterval(entity)) {
                PleasantValueManager.performUpdate(entity, getIncreaseAmount(entity));
                standingStartTimes.put(uuid, now);
            }
        } else {
            standingStartTimes.remove(uuid);
        }
    }

    private static boolean isStandingOnValidBlock(LivingEntity entity) {
        BlockPos pos = entity.getOnPos();
        BlockState state = entity.level().getBlockState(pos);
        return getBlockWhitelist().contains(state.getBlock());
    }

    private static Set<Block> getBlockWhitelist() {
        return
                Set.of(Blocks.SLIME_BLOCK,
                        Blocks.HONEY_BLOCK);
    }

    private static double getIncreaseAmount(LivingEntity entity) {
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
            standingStartTimes.remove(entity.getUUID());
        }
    }
}