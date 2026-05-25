package com.twi.restraint_dungeon.event.mod_event.pleasant.decrease_event;

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
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;

@EventBusSubscriber(modid = MODID)
public class DecayProximityBlockHandler {
    private static final ConcurrentMap<UUID, Long> decayProximityStartTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getPleasantValue(entity) <= 0) return;

        UUID uuid = entity.getUUID();

        if (isNearDecayBlock(entity)) {
            long now = System.currentTimeMillis();
            Long startTime = decayProximityStartTimes.putIfAbsent(uuid, now);
            if (startTime != null && now - startTime >= getDecayInterval(entity)) {
                PleasantValueManager.performDecay(entity, getDecayAmount(entity));
                decayProximityStartTimes.put(uuid, now);
            }
        } else {
            decayProximityStartTimes.remove(uuid);
        }
    }

    private static boolean isNearDecayBlock(LivingEntity entity) {
        Level level = entity.level();
        BlockPos basePos = entity.blockPosition();
        Set<Block> whitelist = getDecayProximityWhitelist();

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

    private static Set<Block> getDecayProximityWhitelist() {
        return Set.of(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE);
    }

    private static double getDecayAmount(LivingEntity entity) {
        return 3.0;
    }

    private static long getDecayInterval(LivingEntity entity) {
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
            decayProximityStartTimes.remove(entity.getUUID());
        }
    }
}