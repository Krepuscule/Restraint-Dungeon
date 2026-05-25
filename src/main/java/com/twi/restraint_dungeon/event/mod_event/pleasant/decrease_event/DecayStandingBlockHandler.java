package com.twi.restraint_dungeon.event.mod_event.pleasant.decrease_event;

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
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;

@EventBusSubscriber(modid = MODID)
public class DecayStandingBlockHandler {
    private static final ConcurrentMap<UUID, Long> decayStandingStartTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getPleasantValue(entity) <= 0) return;

        UUID uuid = entity.getUUID();

        if (isStandingOnDecayBlock(entity)) {
            long now = System.currentTimeMillis();
            Long startTime = decayStandingStartTimes.putIfAbsent(uuid, now);
            if (startTime != null && now - startTime >= getDecayInterval(entity)) {
                PleasantValueManager.performDecay(entity, getDecayAmount(entity));
                decayStandingStartTimes.put(uuid, now);
            }
        } else {
            decayStandingStartTimes.remove(uuid);
        }
    }

    private static boolean isStandingOnDecayBlock(LivingEntity entity) {
        BlockPos pos = entity.getOnPos();
        BlockState state = entity.level().getBlockState(pos);
        return getDecayBlockWhitelist().contains(state.getBlock());
    }

    private static Set<Block> getDecayBlockWhitelist() {
        return Set.of(Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
    }

    private static double getDecayAmount(LivingEntity entity) {
        return 5.0;
    }

    private static long getDecayInterval(LivingEntity entity) {
        return 5000L; // 5秒
    }

    @SubscribeEvent
    public static void onEntityRemove(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        Entity.RemovalReason reason = entity.getRemovalReason();
        if (reason == null) return;

        if (reason == Entity.RemovalReason.KILLED || 
            reason == Entity.RemovalReason.DISCARDED || 
            reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            decayStandingStartTimes.remove(entity.getUUID());
        }
    }
}