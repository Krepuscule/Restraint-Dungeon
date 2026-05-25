package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.CarryAction;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public class ShoulderAction extends CarryAction {

    @Override
    public String getActionId() {
        return "SHOULDER";
    }

    @Override
    public int getAnimTicks() {
        return 20;
    }

    @Override
    public String getCarryType(Player carrier, LivingEntity target) {

        return "CARRY_SHOULDER";
    }

    @Override
    public Component canUse(Player actionPlayer, @Nullable HitResult result) {
        if(!(result instanceof EntityHitResult) || !(((EntityHitResult) result).getEntity() instanceof LivingEntity living)) {
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(!actionPlayer.isAlive() || !living.isAlive()){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBusyState(living)) {
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(living.distanceToSqr(actionPlayer) >= getMaxDistance() * getMaxDistance()){
            return Component.translatable("action." + MODID + ".fail_common.too_far").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBusyState(actionPlayer)) {
            return Component.translatable("action." + MODID + ".fail_common.cant_action_state").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBeenBindArms(actionPlayer) || isBeenBindHands(actionPlayer) || isBeenBindLegs(actionPlayer)) {
            return Component.translatable("action." + MODID + ".fail_common.is_being_binding").withStyle(ChatFormatting.DARK_RED);
        }

        if(!isBeenFullyBind(living)) {
            return Component.translatable("action." + MODID + ".fail_carry.need_bind").withStyle(ChatFormatting.DARK_RED);
        }

        if(isRidingRestraintDevice(living)
                && getRestraintDevice(living) instanceof RestraintDevice rd && !rd.canDismount(
                living.level(),
                Objects.requireNonNull(living.getVehicle()).blockPosition(),
                living)
        ){
            return Component.translatable("action." + MODID + ".fail_carry.locked_by_block").withStyle(ChatFormatting.DARK_RED);
        }

        if(getRestraintPosition(living) != RestraintPosition.STANDING){
            return Component.translatable("action." + MODID + ".fail_shoulder.need_target_standing").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }

    @Override
    public boolean canContinueUse(ServerPlayer actionPlayer, LivingEntity targetEntity) {
        return actionPlayer.isAlive() && targetEntity.isAlive()
                && actionPlayer.distanceToSqr(targetEntity) < getMaxDistance() * getMaxDistance()
                && isBeenFullyBind(targetEntity);
    }

    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target,String CarryingState,Boolean isCarryTarget) {

        if(target == null || !actionPlayer.isAlive() || !target.isAlive()){
            return false;
        }

        if(isBusyState(target)) {
            return false;
        }

        if(target.distanceToSqr(actionPlayer) >= getMaxDistance() * getMaxDistance()){
            return false;
        }

        if(isBusyState(actionPlayer)) {
            return false;
        }

        if(isBeenBindArms(actionPlayer) || isBeenBindHands(actionPlayer) || isBeenBindLegs(actionPlayer)) {
            return false;
        }

        if(!isBeenFullyBind(target)) {
            return false;
        }

        if(isRidingRestraintDevice(target)
                && getRestraintDevice(target) instanceof RestraintDevice rd && !rd.canDismount(
                target.level(),
                Objects.requireNonNull(target.getVehicle()).blockPosition(),
                target)
        ){
            return false;
        }

        return true;
    }
}