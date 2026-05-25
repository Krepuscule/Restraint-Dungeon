package com.twi.restraint_dungeon.animation;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.event.custom_event.*;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.animation.utils.AnimationPlayerUtils.*;

@EventBusSubscriber(modid = MODID)
public class PlayerAnimationControllerEvent {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRestraintEquipped(RestraintEquipEvent event) {
        LivingEntity entity = event.getEntity();
        PlayerRestraintPart part = event.getPart();

        if (!(entity instanceof ServerPlayer player)) return;

        updateRestraintChangeAnimation(player,part);

    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRestraintChange(RestraintChangeEvent event) {
        LivingEntity entity = event.getEntity();
        PlayerRestraintPart part = event.getPart();
        if (!(entity instanceof ServerPlayer player)) return;
        updateRestraintChangeAnimation(player,part);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPositionChange(RestraintPositionChangeEvent.Post event) {
        LivingEntity entity = event.getEntity();
        RestraintPosition prev = event.getPrev();
        RestraintPosition next = event.getNext();

        if (!(entity instanceof ServerPlayer player)) return;

        if(prev != RestraintPosition.CONNECTING && next != RestraintPosition.CONNECTING) {
            updatePositionChangeAnimation(player,prev,next);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        updateRestraintAnimation(serverPlayer);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void playerStrugglingStateBegin(PlayerStrugglingEvent.Begin event) {
        Player player = event.getEntity();
        PlayerRestraintPart part = event.getStrugglingPart();

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if(event.getStrugglingPart() == PlayerRestraintPart.restraint_connection){

        }else{
            updateStrugglingAnimation(serverPlayer,part);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void playerStrugglingStateEnd(PlayerStrugglingEvent.End event) {
        Player player = event.getEntity();
        PlayerRestraintPart part = event.getStrugglingPart();

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if(event.isSuccess()){
            updateRestraintChangeAnimation(serverPlayer,part);
        }else{
            updateRestraintAnimation(serverPlayer);
        }

    }

    private static final Map<UUID, Boolean> LAST_CROUCH_STATE = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();
        boolean isCurrentlyCrouching = player.isCrouching();
        boolean wasCrouching = LAST_CROUCH_STATE.getOrDefault(uuid, false);

        if (isCurrentlyCrouching != wasCrouching) {
            if (isCurrentlyCrouching) {
                updateRestraintCrouchingAnimation(player);
            } else {
                updateRestraintAnimation(player);
            }
            LAST_CROUCH_STATE.put(uuid, isCurrentlyCrouching);
        }
    }

    @SubscribeEvent
    public static void onRestraintMovePre(PlayerRestraintMoveEvent.Pre event) {

        Player player = event.getEntity();
        PlayerRestraintMove move = event.getMove();
        String direction = event.getDirection();

        if(!(player instanceof ServerPlayer serverPlayer)) return;

        updateRestraintMoveAnimation(serverPlayer,move,direction,"PRE");
    }

    @SubscribeEvent
    public static void onRestraintMoveMid(PlayerRestraintMoveEvent.Mid event) {

        Player player = event.getEntity();
        PlayerRestraintMove move = event.getMove();
        String direction = event.getDirection();

        if(!(player instanceof ServerPlayer serverPlayer)) return;

        updateRestraintMoveAnimation(serverPlayer,move,direction,"MID");
    }

    @SubscribeEvent
    public static void onRestraintMoveEnd(PlayerRestraintMoveEvent.End event) {

        Player player = event.getEntity();
        PlayerRestraintMove move = event.getMove();
        String direction = event.getDirection();

        if(!(player instanceof ServerPlayer serverPlayer)) return;

        updateRestraintMoveAnimation(serverPlayer,move,direction,"END");
    }

    @SubscribeEvent
    public static void onPlayerCarrying(PlayerActionEvent.Finish event){

        Player player = event.getEntity();
        LivingEntity target = event.getTargetEntity();
        BaseAction action = event.getAction();

        if(action == null) return;

        if(!(player instanceof ServerPlayer serverPlayer)) return;

        updateCarryAnimation(serverPlayer,false);

        if(target instanceof ServerPlayer targetPlayer){
            updateCarryAnimation(targetPlayer,true);
        }

    }

}
