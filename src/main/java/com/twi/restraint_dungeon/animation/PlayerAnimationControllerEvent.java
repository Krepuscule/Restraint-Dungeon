package com.twi.restraint_dungeon.animation;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.type.AnimAction;
import com.twi.restraint_dungeon.action.type.CarryAction;
import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.animation.utils.AnimationLayer;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.*;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerAnimationData;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.event.custom_event.*;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_animator.PlayerAnimationSequencePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.animation.utils.AnimationPlayerUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.getCarriedPassenger;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isTargetFlag;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getPlayerStrugglingItem;

@EventBusSubscriber(modid = MODID)
public class PlayerAnimationControllerEvent {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onConnectionRestraintPosition(RestraintPositionChangeEvent.Post event){
        LivingEntity entity = event.getEntity();
        RestraintPosition prev = event.getPrev();
        RestraintPosition next = event.getNext();
        ItemStack stack = event.getStack();

        if(!(entity instanceof ServerPlayer player)) return;

        if(prev != RestraintPosition.CONNECTING && next == RestraintPosition.CONNECTING
                && stack.getItem() instanceof RestraintItem ri){
            updateConnectionRestraintEquipAnimation(player,ri);
        }else if(prev == RestraintPosition.CONNECTING && next != RestraintPosition.CONNECTING
                && stack.getItem() instanceof RestraintItem ri){
            updateConnectionRestraintUnequipAnimation(player,ri);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRestraintPoseChange(PoseChangeEvent event){
        LivingEntity entity = event.getEntity();
        ArmsPose Prev_ArmsPose = event.getPrevArmsPose();
        ArmsPose Curr_ArmsPose = event.getCurrArmsPose();
        LegsPose Prev_LegsPose = event.getPrevLegsPose();
        LegsPose Curr_LegsPose = event.getCurrLegsPose();

        if(!(entity instanceof ServerPlayer player)) return;

        if(Prev_ArmsPose != Curr_ArmsPose || Prev_LegsPose != Curr_LegsPose){
            updateRestraintChangeAnimation(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPositionChange(RestraintPositionChangeEvent.Post event) {
        LivingEntity entity = event.getEntity();
        RestraintPosition prev = event.getPrev();
        RestraintPosition next = event.getNext();

        if (!(entity instanceof ServerPlayer player)) return;

        if(prev != RestraintPosition.CONNECTING
                && prev != RestraintPosition.CARRIED
                && prev != RestraintPosition.RIDING
                && next != RestraintPosition.CONNECTING
                && next != RestraintPosition.CARRIED
                && next != RestraintPosition.RIDING) {
            updatePositionChangeAnimation(player,prev,next);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMountRestraintDevice(RestraintDeviceMountEvent.Post event){
        LivingEntity entity = event.getEntity();
        RestraintDevice device = event.getDevice();

        if(device == null || !(entity instanceof ServerPlayer player)) return;

        updateRestraintAnimation(player);

    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDismountRestraintDevice(RestraintDeviceDismountEvent.Post event){
        LivingEntity entity = event.getEntity();
        RestraintDevice device = event.getDevice();

        if(device == null || !(entity instanceof ServerPlayer player)) return;

        updateRestraintAnimation(player);
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


        updateStrugglingAnimation(serverPlayer,part);

    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void playerStrugglingStateEnd(PlayerStrugglingEvent.End event) {
        Player player = event.getEntity();
        PlayerRestraintPart part = event.getStrugglingPart();
        RestraintPosition position = getRestraintPosition(player);
        ItemStack stack = getPlayerStrugglingItem(player);

        if (!(player instanceof ServerPlayer serverPlayer)
                || !(stack.getItem() instanceof RestraintItem ri)) return;

        if(event.isSuccess()) {
            if (part == PlayerRestraintPart.restraint_connection) {
                updateConnectionRestraintUnequipAnimation(serverPlayer, ri);
            }else if(position == RestraintPosition.CARRIED){
                updateCarryingAnimation(serverPlayer,isTargetFlag(serverPlayer));
            }else{
                updateRestraintChangeAnimation(serverPlayer);
            }
        }else{
            if(position == RestraintPosition.CARRIED){
                updateCarryingAnimation(serverPlayer,isTargetFlag(serverPlayer));
            }else{
                updateRestraintAnimation(serverPlayer);
            }
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
    public static void onPlayerCarrying(PlayerCarryStateEvent.Start event){

        Player player = event.getEntity();
        LivingEntity target = event.getTarget();
        CarryType type = event.getCarryType();

        if(type == null || type.getID().equals("NONE")) return;

        if(!(player instanceof ServerPlayer serverPlayer)) return;

        updateCarryingAnimation(serverPlayer,false);

        if(target instanceof ServerPlayer targetPlayer){
            updateCarryingAnimation(targetPlayer,true);
        }

    }

    @SubscribeEvent
    public static void onPlayerActionStart(PlayerActionEvent.Start event){
        Player player = event.getEntity();
        BaseAction action = event.getAction();
        HitResult hitResult = event.getHitResult();

        if(action == null || !(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide) return;

        LivingEntity target = null;

        if(action instanceof AnimAction){

            if(hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living){
                target = living;
            }

        }else if(action instanceof CarryAction){

            if(hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living){
                target = living;
            }


        }else if(action instanceof CarryingAction){
            target = getCarriedPassenger(player);

        }

        updateActionAnimation(serverPlayer,action,hitResult,false);
        if(target instanceof ServerPlayer targetPlayer){
            updateActionAnimation(targetPlayer,action,hitResult,true);
        }
    }

    @SubscribeEvent
    public static void onPlayerCarryFinish(PlayerCarryStateEvent.Stop event){

        Player player = event.getEntity();
        LivingEntity target = event.getTarget();
        CarryType type = event.getCarryType();

        if(type == null || type.getID().equals("NONE")) return;

        if(!(player instanceof ServerPlayer serverPlayer)) return;

        updateRestraintAnimation(serverPlayer);

        if(target instanceof ServerPlayer targetPlayer){
            updateRestraintAnimation(targetPlayer);
        }

    }
    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer target) {

            PlayerAnimationData attachment = target.getData(ModAttachments.PLAYER_ANIMATION.get());

            if (!attachment.isEmpty()) {
                for (AnimationLayer layer : AnimationLayer.values()) {
                    var animList = attachment.getSequence(layer);
                    if (!animList.isEmpty()) {
                        PacketDistributor.sendToPlayer(observer,
                                new PlayerAnimationSequencePayload(target.getUUID(), animList, layer));
                    }
                }
            }
        }
    }
}
