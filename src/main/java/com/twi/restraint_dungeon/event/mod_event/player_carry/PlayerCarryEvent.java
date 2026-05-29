package com.twi.restraint_dungeon.event.mod_event.player_carry;

import com.twi.restraint_dungeon.event.custom_event.PlayerCarryStateEvent;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindLegs;

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
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
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

        carrier.getServer().execute(() -> {
            if (PlayerCarryUtils.isCarrier(carrier)) {
                UUID pUUID = PlayerCarryUtils.getPartnerUUID(carrier);
                CarryType type = getCurrentCarryType(carrier);
                if (pUUID == null || type == null) return;

                LivingEntity passenger = findPassengerEntity(carrier.getServer(), carrier, pUUID);

                if (passenger != null && passenger.level() != carrier.level()) {
                    passenger.teleportTo(
                            (ServerLevel) carrier.level(),
                            carrier.getX(), carrier.getY(), carrier.getZ(),
                            java.util.Set.of(),
                            carrier.getYRot(),
                            carrier.getXRot()
                    );

                    startCarrying(carrier,passenger,type.getID());

                    NeoForge.EVENT_BUS.post(new PlayerCarryStateEvent.Start(carrier, type, passenger));
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


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && getRestraintPosition(player) == RestraintPosition.CARRIED) {
            if (event.getName().equals(VanillaGuiLayers.VEHICLE_HEALTH)) {
                event.setCanceled(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onCarryingLivingEntity(MovementInputUpdateEvent event){
        LocalPlayer player = (LocalPlayer) event.getEntity();
        Input input = event.getInput();

        if (isCarrier(player)) {
            input.shiftKeyDown = false;
        }else if(isBeingCarried(player)){
            input.leftImpulse = 0;
            input.forwardImpulse = 0;
            input.jumping = false;
            input.shiftKeyDown = false;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
        }
    }
}