package com.twi.restraint_dungeon.event.mod_event.player_carry;

import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;

@EventBusSubscriber(modid = MODID)
public class PlayerCarryEvent {

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if(event.getEntity() instanceof Player player && isCarrier(player)
                || event.getEntity() instanceof LivingEntity living && isBeingCarried(living)) {
            PlayerCarryUtils.checkTicks((LivingEntity) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity instanceof Player player){
            if(isCarrier(player)){
                stopCarrying(player);
            }else if(isBeingCarried(player)){
                Player carrier = getCarrier(player);
                if(carrier != null) {
                    stopCarrying(carrier);
                }
            }
        }else{
            if(isBeingCarried(entity)){
                Player carrier = getCarrier(entity);
                clearCarryData(entity);
                if(carrier != null){
                    stopCarrying(carrier);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
       Player player = event.getEntity();
        if(isCarrier(player)){
            stopCarrying(player);
        }else if(isBeingCarried(player)){
            Player carrier = getCarrier(player);
            clearCarryData(player);
            if(carrier != null){
                stopCarrying(carrier);
            }
        }
    }

    @SubscribeEvent
    public static void onPortalTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer carrier)) return;

        if (PlayerCarryUtils.isCarrier(carrier)) {
            LivingEntity passenger = PlayerCarryUtils.getCarriedPassenger(carrier);
            if (passenger != null) {
                passenger.stopRiding();
            }
        }
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer carrier)) return;

        // 使用 server.execute 确保在主线程执行，避免异步获取实体导致的线程安全问题
        carrier.getServer().execute(() -> {
            if (PlayerCarryUtils.isCarrier(carrier)) {
                UUID pUUID = PlayerCarryUtils.getPartnerUUID(carrier);
                if (pUUID == null) return;

                LivingEntity passenger = findPassengerEntity(carrier.getServer(), carrier, pUUID);

                if (passenger != null && passenger.level() != carrier.level()) {
                    passenger.teleportTo(
                            (ServerLevel) carrier.level(),
                            carrier.getX(), carrier.getY(), carrier.getZ(),
                            java.util.Set.of(), // 无特殊相对位移标记
                            carrier.getYRot(),
                            carrier.getXRot()
                    );
                }
            }
        });
    }

    private static LivingEntity findPassengerEntity(MinecraftServer server, ServerPlayer carrier, UUID uuid) {

        ServerPlayer targetPlayer = server.getPlayerList().getPlayer(uuid);
        if (targetPlayer != null) return targetPlayer;

        Entity localEntity = carrier.serverLevel().getEntity(uuid);
        if (localEntity instanceof LivingEntity living) return living;

        for (ServerLevel level : server.getAllLevels()) {
            if (level == carrier.serverLevel()) continue;

            Entity remoteEntity = level.getEntity(uuid);
            if (remoteEntity instanceof LivingEntity living) {
                return living;
            }
        }

        return null;
    }
}