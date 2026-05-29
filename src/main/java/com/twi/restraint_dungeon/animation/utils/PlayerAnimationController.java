package com.twi.restraint_dungeon.animation.utils;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.type.AnimAction;
import com.twi.restraint_dungeon.action.type.CarryAction;
import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.ArmsPose;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.LegsPose;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;

import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public class PlayerAnimationController {

    // --------------------------------------- 基础拘束具动画 ---------------------------------------------------
    public static String getPlayerArmsPoseAnimation(ServerPlayer player) {

        ArmsPose armsPose = getArmsPose(player);
        RestraintPosition position = getRestraintPosition(player);

        if(armsPose == ArmsPose.NONE){
            return "arms_none_" + position.toString().toLowerCase();
        }else {
            return "arms_bind_0" + armsPose.ordinal() + "_" + position.toString().toLowerCase();
        }
    }

    public static String getPlayerLegsPoseAnimation(ServerPlayer player) {
        LegsPose legsPose = getLegsPose(player);
        RestraintPosition position = getRestraintPosition(player);

        if(legsPose == LegsPose.NONE){
            return "legs_none_" + position.toString().toLowerCase();
        }else{
            return "legs_bind_0" + legsPose.ordinal() + "_" + position.toString().toLowerCase();
        }
    }

    public static String getPlayerBodyAnimation(ServerPlayer player) {

        RestraintPosition position = getRestraintPosition(player);

        return "body_" + position.toString().toLowerCase();
    }
    // ----------------------------------------- 潜行切换动画 ---------------------------------------------------
    public static String getPlayerCrouchingAnimation(ServerPlayer player) {
        ArmsPose armsPose = getArmsPose(player);
        RestraintPosition position = getRestraintPosition(player);
        
        if(player.isCrouching()){
            return "arms_bind_0" + armsPose.ordinal() + "_" + position.toString().toLowerCase() + "_crouching";
        }

        return null;
    }

    // --------------------------------------- 拘束具切换动画 ---------------------------------------------------
    public static String getPlayerArmsPoseTransitionAnimation(ServerPlayer player){
        ArmsPose prev = getPrevArmsPose(player);
        ArmsPose curr = getArmsPose(player);

        RestraintPosition pos = getRestraintPosition(player);

        if(prev != curr){
            if(prev == ArmsPose.NONE){
                return "arms_bind_0" + curr.ordinal() + "_to_" + pos.toString().toLowerCase();
            }else if(curr == ArmsPose.NONE){
                return "arms_bind_0" + prev.ordinal() + "_back_" + pos.toString().toLowerCase();
            }
        }

        return null;
    }

    public static String getPlayerLegsPoseTransitionAnimation(ServerPlayer player){
        LegsPose prev = getPrevLegsPose(player);
        LegsPose curr = getLegsPose(player);
        RestraintPosition pos = getRestraintPosition(player);

        if(prev != curr){
            if(prev == LegsPose.NONE){
                return "legs_bind_0" + curr.ordinal() + "_to_" + pos.toString().toLowerCase();
            }else if(curr == LegsPose.NONE){
                return "legs_bind_0" + prev.ordinal() + "_back_" + pos.toString().toLowerCase();
            }
        }

        return null;
    }

    // --------------------------------------- 姿势切换动画 ---------------------------------------------------
    public static String getPositionChangeArmsPoseAnimation(ServerPlayer player,RestraintPosition prev,RestraintPosition next) {
        ArmsPose armsPose = getArmsPose(player);

        if(prev != next){
            if(armsPose == ArmsPose.NONE){
                return "arms_none_" + prev.toString().toLowerCase() + "_to_" + next.toString().toLowerCase();
            }else{
                return "arms_bind_0" +armsPose.ordinal() + "_" + prev.toString().toLowerCase() + "_to_" + next.toString().toLowerCase();
            }
        }
        return null;
    }

    public static String getPositionChangeLegsPoseAnimation(ServerPlayer player,RestraintPosition prev,RestraintPosition next) {
        LegsPose legsPose = getLegsPose(player);
        if(prev != next){
            if(legsPose == LegsPose.NONE){
                return "legs_none_" + prev.toString().toLowerCase() + "_to_" + next.toString().toLowerCase();
            }else{
                return "legs_bind_0" +legsPose.ordinal() + "_" + prev.toString().toLowerCase() + "_to_" + next.toString().toLowerCase();
            }
        }
        return null;
    }

    public static String getPositionChangeBodyAnimation(ServerPlayer player,RestraintPosition prev,RestraintPosition next) {
        if(prev != next){
            return "body_" + prev.toString().toLowerCase() + "_to_" + next.toString().toLowerCase();
        }
        return null;
    }

    // --------------------------------------- 挣扎动画 ---------------------------------------------------

    public static String getStrugglingArmsPoseAnimation(ServerPlayer player, PlayerRestraintPart part) {
        ArmsPose armsPose = getArmsPose(player);
        RestraintPosition position = getRestraintPosition(player);
        if(armsPose == ArmsPose.NONE && !isBeenBindArms(player)){
            if(part != PlayerRestraintPart.restraint_connection && part != PlayerRestraintPart.restraint_legs_bind){
                return "arms_none_struggle_" + part.toString().toLowerCase();
            }else{
                return null;
            }
        }else{
            if(position == RestraintPosition.CARRIED){
                CarryType type = getCurrentCarryType(player);
                if (type != null && isBeingCarried(player)) {
                    return "arms_bind_0" + armsPose.ordinal() + "_struggle_" + type.getID().toLowerCase() + "_target";
                }
            }else{
                return "arms_bind_0" + armsPose.ordinal() + "_struggle_" + position.toString().toLowerCase();
            }
        }

        return null;
    }

    public static String getStrugglingLegsPoseAnimation(ServerPlayer player,PlayerRestraintPart part) {
        LegsPose legsPose = getLegsPose(player);
        RestraintPosition position = getRestraintPosition(player);
        if(legsPose == LegsPose.NONE){
            if(isBeenBindArms(player) && part != PlayerRestraintPart.restraint_connection){
                return "legs_none_struggle_" + position.toString().toLowerCase();
            }else{
                return null;
            }
        }else{
            if((isBeenBindArms(player) || part == PlayerRestraintPart.restraint_legs_bind) && part != PlayerRestraintPart.restraint_connection){
                if(position == RestraintPosition.CARRIED){
                    CarryType type = getCurrentCarryType(player);
                    if (type != null && isBeingCarried(player)) {
                        return "legs_bind_0" + legsPose.ordinal() + "_struggle_" + type.getID().toLowerCase() + "_target";
                    }else{
                        return null;
                    }
                }else{
                    return "legs_bind_0" + legsPose.ordinal() + "_struggle_" + position.toString().toLowerCase();
                }
            }else{
                return null;
            }
        }
    }

    public static String getStrugglingBodyAnimation(ServerPlayer player,PlayerRestraintPart part) {
        RestraintPosition position = getRestraintPosition(player);
        if(!isBeenBindArms(player) && part != PlayerRestraintPart.restraint_legs_bind){
            return "body_" + position.toString().toLowerCase();
        }else{
            if(position == RestraintPosition.CARRIED){
                CarryType type = getCurrentCarryType(player);
                if(type != null && isBeingCarried(player)){
                    return "body_struggle_" + type.getID().toLowerCase() + "_target";
                }else{
                    return null;
                }
            }else{
                return "body_struggle_" + position.toString().toLowerCase();
            }
        }
    }

    // --------------------------------------- 拘束移动动画 ---------------------------------------------------

    public static String getRestraintMovePreAnimation(ServerPlayer player,String moveType,String direction){

        return "restraint_move_" + moveType + "_" + direction.toLowerCase() + "_pre";
    }

    public static String getRestraintMoveMidAnimation(ServerPlayer player,String moveType,String direction){
        if(isPlayerRestraintMoving(player)){
            return "restraint_move_" + moveType + "_" + direction.toLowerCase() + "_mid";
        }

        return null;
    }

    public static String getRestraintMoveEndAnimation(ServerPlayer player,String moveType,String direction){
        if(isPlayerRestraintMoving(player)){
            return "restraint_move_" + moveType + "_" + direction.toLowerCase() + "_end";
        }

        return null;
    }

    // --------------------------------------- 动作相关动画 ---------------------------------------------------

    public static String getActionFullBodyAnimation(ServerPlayer player, BaseAction action, HitResult hitResult,boolean isTarget){

        if(player == null || action == null || action.getActionId().equals("NONE")) return null;

        if(action instanceof AnimAction animAction){
            if(isTarget){
                return "action_anim_" + animAction.getActionId().toLowerCase() + "_target";
            }else{
                return "action_anim_" + animAction.getActionId().toLowerCase() + "_action";
            }
        }else if(action instanceof CarryAction carryAction){
            if(isTarget){
                return "action_carry_" + carryAction.getActionId().toLowerCase() + "_target";
            }else{
                return "action_carry_" + carryAction.getActionId().toLowerCase() + "_action";
            }
        }else if(action instanceof CarryingAction carryingAction){
            CarryType type = getCurrentCarryType(player);
            if(type == null || type.getID().equals("NONE")) return null;

            if(isTarget){
                return "action_" + type.getID().toLowerCase() + "_" + carryingAction.getActionId().toLowerCase() + "_target";
            }else{
                return "action_" + type.getID().toLowerCase() + "_" + carryingAction.getActionId().toLowerCase() + "_action";
            }
        }


        return null;
    }

    public static String getCarryingArmsPoseAnimation(ServerPlayer player,boolean isTarget){
        ArmsPose armsPose = getArmsPose(player);
        CarryType type = getCurrentCarryType(player);

        if(isTarget){
            if (type != null && isBeenBindArms(player)) {
                return "arms_bind_0" + armsPose.ordinal() + "_" + type.getID().toLowerCase() + "_target";
            }
        }else{
            if (type != null) {
                return "arms_none_" + type.getID().toLowerCase() + "_action";
            }
        }
        return null;
    }

    public static String getCarryingLegsPoseAnimation(ServerPlayer player,boolean isTarget){
        LegsPose legsPose = getLegsPose(player);
        CarryType type = getCurrentCarryType(player);

        if(isTarget){
            if(type != null && isBeenBindLegs(player)){
                return "legs_bind_0" + legsPose.ordinal() + "_" + type.getID().toLowerCase() + "_target";
            }
        }else{
            return null;
        }

        return null;
    }

    public static String getCarryingBodyAnimation(ServerPlayer player,boolean isTarget){
        CarryType type = getCurrentCarryType(player);

        if(isTarget){
            if(type != null){
                return "base_body_" + type.getID().toLowerCase() + "_target";
            }
        }else{
            return null;
        }

        return null;
    }

}
