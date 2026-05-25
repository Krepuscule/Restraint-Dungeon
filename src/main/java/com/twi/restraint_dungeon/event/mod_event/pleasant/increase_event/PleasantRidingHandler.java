package com.twi.restraint_dungeon.event.mod_event.pleasant.increase_event;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_reverse_cross.WoodenReverseCrossBlock;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_triangle_horse.WoodenTriangleHorseBlock;
import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;

@EventBusSubscriber(modid = MODID)
public class PleasantRidingHandler {
    private static final ConcurrentMap<UUID, Long> ridingStartTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;
        
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) return;
        if (getThrillLevel(entity) <= 0) return;

        UUID uuid = entity.getUUID();
        boolean isRidingValid = entity.isPassenger() && isValidPleasantProvider(entity);

        if (isRidingValid) {
            long now = System.currentTimeMillis();
            Long startTime = ridingStartTimes.putIfAbsent(uuid, now);
            if (startTime != null) {
                if (now - startTime >= getIncreaseInterval(entity)) {
                    PleasantValueManager.performUpdate(entity, getIncreaseAmount(entity));
                    ridingStartTimes.put(uuid, now);
                }
            }
        } else {
            ridingStartTimes.remove(uuid);
        }
    }


    private static long getIncreaseInterval(LivingEntity entity) {
        if(isRidingRestraintDevice(entity)){
            if(getRestraintDevice(entity) instanceof RestraintDevice rb){

                if(rb instanceof WoodenTriangleHorseBlock || rb instanceof WoodenReverseCrossBlock){
                    return 5000L;
                }

            }
        }
        return 10000L; // 10秒
    }

    public static double getIncreaseAmount(LivingEntity entity) {
        return getThrillLevel(entity);
    }

    private static boolean isValidPleasantProvider(LivingEntity entity) {
        Entity vehicle = entity.getVehicle();

        if(vehicle instanceof Horse || vehicle instanceof Camel) return true;

        if(isRidingRestraintDevice(entity)){
            if(getRestraintDevice(entity) instanceof RestraintDevice rb){

                if(rb instanceof WoodenTriangleHorseBlock || rb instanceof WoodenReverseCrossBlock){
                    return true;
                }

            }
        }
        return false;
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
            ridingStartTimes.remove(entity.getUUID());
        }
    }
}