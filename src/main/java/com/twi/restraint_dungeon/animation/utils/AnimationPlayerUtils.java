package com.twi.restraint_dungeon.animation.utils;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.network.payload.player_animator.PlayerAnimationSequencePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.animation.utils.PlayerAnimationController.*;
import static com.twi.restraint_dungeon.animation.utils.PlayerAnimationController.getStrugglingBodyAnimation;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isBeingCarried;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isCarrier;

public class AnimationPlayerUtils {

    public static void updateRestraintAnimation(ServerPlayer player) {
        List<String> arms_animations = new ArrayList<>();

        arms_animations.add(getPlayerArmsPoseAnimation(player));

        sendAnimationSequence(player, arms_animations,AnimationLayer.ARMS);

        List<String> legs_animations = new ArrayList<>();
        legs_animations.add(getPlayerLegsPoseAnimation(player));

        sendAnimationSequence(player, legs_animations,AnimationLayer.LEGS);

        List<String> body_animations = new ArrayList<>();
        body_animations.add(getPlayerBodyAnimation(player));

        sendAnimationSequence(player,body_animations,AnimationLayer.BASE_BODY);
    }

    public static void updateRestraintCrouchingAnimation(ServerPlayer player) {
        List<String> arms_animations = new ArrayList<>();
        arms_animations.add(getPlayerCrouchingAnimation(player));

        sendAnimationSequence(player, arms_animations,AnimationLayer.ARMS);
    }

    public static void updateRestraintChangeAnimation(ServerPlayer player, PlayerRestraintPart part) {
        List<String> arms_animations = new ArrayList<>();

        if(getPlayerArmsPoseTransitionAnimation(player) != null){
            arms_animations.add(getPlayerArmsPoseTransitionAnimation(player));
        }
        arms_animations.add(getPlayerArmsPoseAnimation(player));

        sendAnimationSequence(player, arms_animations,AnimationLayer.ARMS);

        List<String> legs_animations = new ArrayList<>();
        if(getPlayerLegsPoseTransitionAnimation(player) != null){
            legs_animations.add(getPlayerLegsPoseTransitionAnimation(player));
        }
        legs_animations.add(getPlayerLegsPoseAnimation(player));

        List<String> body_animations = new ArrayList<>();
        body_animations.add(getPlayerBodyAnimation(player));

        sendAnimationSequence(player,body_animations,AnimationLayer.BASE_BODY);

        sendAnimationSequence(player, legs_animations,AnimationLayer.LEGS);
    }

    public static void updatePositionChangeAnimation(ServerPlayer player, RestraintPosition prev,RestraintPosition next) {

        List<String> arms_animations = new ArrayList<>();
        if(getPositionChangeArmsPoseAnimation(player,prev,next) != null){
            arms_animations.add(getPositionChangeArmsPoseAnimation(player,prev,next));
        }
        arms_animations.add(getPlayerArmsPoseAnimation(player));
        sendAnimationSequence(player, arms_animations,AnimationLayer.ARMS);

        List<String> legs_animations = new ArrayList<>();
        if(getPositionChangeLegsPoseAnimation(player,prev,next) != null){
            legs_animations.add(getPositionChangeLegsPoseAnimation(player,prev,next));
        }
        legs_animations.add(getPlayerLegsPoseAnimation(player));
        sendAnimationSequence(player, legs_animations,AnimationLayer.LEGS);

        List<String> body_animations = new ArrayList<>();

        body_animations.add(getPositionChangeBodyAnimation(player,prev,next));

        // 解决动画中180度到-180度的偏转问题
        if(getPositionChangeBodyAnimation(player,prev,next) != null){
            if(prev == RestraintPosition.LYING_RIGHT && next == RestraintPosition.LYING_DOWN){
                body_animations.add(getPlayerBodyAnimation(player) + "_02");
            }else{
                body_animations.add(getPlayerBodyAnimation(player));
            }
        }
        sendAnimationSequence(player, body_animations,AnimationLayer.BASE_BODY);
    }

    public static void updateStrugglingAnimation(ServerPlayer player,PlayerRestraintPart part) {
        List<String> arms_animations = new ArrayList<>();
        if(getStrugglingArmsPoseAnimation(player,part) != null){
            arms_animations.add(getStrugglingArmsPoseAnimation(player,part));
        }

        sendAnimationSequence(player, arms_animations, AnimationLayer.ARMS);

        List<String> legs_animations = new ArrayList<>();
        if(getStrugglingLegsPoseAnimation(player,part) != null){
            legs_animations.add(getStrugglingLegsPoseAnimation(player, part));
        }
        sendAnimationSequence(player, legs_animations,AnimationLayer.LEGS);

        List<String> body_animations = new ArrayList<>();
        if(getStrugglingBodyAnimation(player,part) != null){
            body_animations.add(getStrugglingBodyAnimation(player,part));
        }
        sendAnimationSequence(player, body_animations,AnimationLayer.BASE_BODY);
    }

    public static void updateRestraintMoveAnimation(ServerPlayer player, PlayerRestraintMove move,String direction,String stage) {
        List<String> full_body_animations = new ArrayList<>();

        if(stage.equals("PRE")){
            if(getRestraintMovePreAnimation(player,move.getMoveTypeId(),direction) != null){
                full_body_animations.add(getRestraintMovePreAnimation(player,move.getMoveTypeId().toLowerCase(),direction));
            }
        }else if(stage.equals("MID")){
            if(getRestraintMoveMidAnimation(player,move.getMoveTypeId(),direction) != null){
                full_body_animations.add(getRestraintMoveMidAnimation(player,move.getMoveTypeId().toLowerCase(),direction));
            }
        }else if(stage.equals("END")){
            if(getRestraintMoveEndAnimation(player,move.getMoveTypeId(),direction) != null){
                full_body_animations.add(getRestraintMoveEndAnimation(player,move.getMoveTypeId().toLowerCase(),direction));
            }
        }

        if(!full_body_animations.isEmpty()){
            sendAnimationSequence(player, full_body_animations,AnimationLayer.FULL_BODY);
        }
    }

    public static void updateCarryingAnimation(ServerPlayer player, boolean isTarget) {
        List<String> arms_animations = new ArrayList<>();
        if(getCarryingArmsPoseAnimation(player,isTarget) != null){
            arms_animations.add(getCarryingArmsPoseAnimation(player,isTarget));
        }

        sendAnimationSequence(player, arms_animations, AnimationLayer.ARMS);

        List<String> legs_animations = new ArrayList<>();
        if(getCarryingLegsPoseAnimation(player,isTarget) != null){
            legs_animations.add(getCarryingLegsPoseAnimation(player, isTarget));
        }
        sendAnimationSequence(player, legs_animations,AnimationLayer.LEGS);

        List<String> body_animations = new ArrayList<>();
        if(getCarryingBodyAnimation(player,isTarget) != null){
            body_animations.add(getCarryingBodyAnimation(player,isTarget));
        }
        sendAnimationSequence(player, body_animations,AnimationLayer.BASE_BODY);
    }

    public static void updateActionAnimation(ServerPlayer player, BaseAction action, HitResult hitResult,boolean isTarget){
        List<String> full_body_animations = new ArrayList<>();

        if(getActionFullBodyAnimation(player, action, hitResult, isTarget) != null){
            full_body_animations.add(getActionFullBodyAnimation(player, action, hitResult, isTarget));
        }

        sendAnimationSequence(player,full_body_animations,AnimationLayer.FULL_BODY);
    }


    public static void sendAnimationSequence(ServerPlayer player, List<String> anims, AnimationLayer layer) {
        if(!anims.isEmpty()){
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new PlayerAnimationSequencePayload(player.getUUID(), anims, layer));
        }
    }

    // 传送/进入视野时的同步逻辑
    public static void syncPlayerAnimation(ServerPlayer trackedPlayer, ServerPlayer observer) {

        if(isCarrier(trackedPlayer)){
            List<String> arms_animations = new ArrayList<>();
            if(getCarryingArmsPoseAnimation(trackedPlayer,false) != null){
                arms_animations.add(getCarryingArmsPoseAnimation(trackedPlayer,false));
            }

            PacketDistributor.sendToPlayer(observer,new PlayerAnimationSequencePayload(trackedPlayer.getUUID(),arms_animations,AnimationLayer.ARMS));

            List<String> legs_animations = new ArrayList<>();
            if(getCarryingLegsPoseAnimation(trackedPlayer,false) != null){
                legs_animations.add(getCarryingLegsPoseAnimation(trackedPlayer, false));
            }
            PacketDistributor.sendToPlayer(observer,new PlayerAnimationSequencePayload(trackedPlayer.getUUID(),legs_animations,AnimationLayer.LEGS));

            List<String> body_animations = new ArrayList<>();
            if(getCarryingBodyAnimation(trackedPlayer,false) != null){
                body_animations.add(getCarryingBodyAnimation(trackedPlayer,false));
            }
            PacketDistributor.sendToPlayer(observer,new PlayerAnimationSequencePayload(trackedPlayer.getUUID(),body_animations,AnimationLayer.BASE_BODY));

        }else if(isBeingCarried(trackedPlayer)){
            List<String> arms_animations = new ArrayList<>();
            if(getCarryingArmsPoseAnimation(trackedPlayer,true) != null){
                arms_animations.add(getCarryingArmsPoseAnimation(trackedPlayer,true));
            }

            PacketDistributor.sendToPlayer(observer,new PlayerAnimationSequencePayload(trackedPlayer.getUUID(),arms_animations,AnimationLayer.ARMS));

            List<String> legs_animations = new ArrayList<>();
            if(getCarryingLegsPoseAnimation(trackedPlayer,true) != null){
                legs_animations.add(getCarryingLegsPoseAnimation(trackedPlayer, true));
            }
            PacketDistributor.sendToPlayer(observer,new PlayerAnimationSequencePayload(trackedPlayer.getUUID(),legs_animations,AnimationLayer.LEGS));

            List<String> body_animations = new ArrayList<>();
            if(getCarryingBodyAnimation(trackedPlayer,true) != null){
                body_animations.add(getCarryingBodyAnimation(trackedPlayer,true));
            }
            PacketDistributor.sendToPlayer(observer,new PlayerAnimationSequencePayload(trackedPlayer.getUUID(),body_animations,AnimationLayer.BASE_BODY));
        }
    }
}