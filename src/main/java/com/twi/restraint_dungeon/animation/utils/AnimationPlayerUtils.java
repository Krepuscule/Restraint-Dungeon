package com.twi.restraint_dungeon.animation.utils;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.network.payload.player_animator.PlayerAnimationSequencePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.animation.utils.PlayerAnimationController.*;

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
        body_animations.add(getStrugglingBodyAnimation(player,part));
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

    public static void updateCarryAnimation(ServerPlayer player, boolean isTarget) {
        List<String> full_body_animations = new ArrayList<>();
        full_body_animations.add(getPlayerCarryingAnimation(player,isTarget));
        sendAnimationSequence(player, full_body_animations,AnimationLayer.FULL_BODY);
    }


    public static void sendAnimationSequence(ServerPlayer player, List<String> anims, AnimationLayer layer) {
        if(!anims.isEmpty()){
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new PlayerAnimationSequencePayload(player.getUUID(), anims, layer));
        }
    }

    // 传送/进入视野时的同步逻辑
    public static void syncPlayerAnimation(ServerPlayer trackedPlayer, ServerPlayer observer) {
        for (AnimationLayer layer : AnimationLayer.values()) {
            List<String> currentAnims = List.of("hello");

            if (!currentAnims.isEmpty()) {
                PacketDistributor.sendToPlayer(observer,
                        new PlayerAnimationSequencePayload(trackedPlayer.getUUID(), currentAnims, layer));
            }
        }
    }
}