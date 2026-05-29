package com.twi.restraint_dungeon.action.type;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public abstract class CarryAction extends BaseAction {

    public String getCarryType(Player carrier,LivingEntity target) {
        return "NONE";
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

        return null;
    }

    @Override
    public boolean canContinueUse(ServerPlayer actionPlayer, LivingEntity targetEntity) {
        return actionPlayer.isAlive() && targetEntity.isAlive()
                && actionPlayer.distanceToSqr(targetEntity) < getMaxDistance() * getMaxDistance()
                && isBeenFullyBind(targetEntity);
    }

    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target,String CarryingState,Boolean isCarryTarget){
//        if(target instanceof Player || target instanceof BaseNPCEntity)
        if(!actionPlayer.isAlive() || (target != null && !target.isAlive()) || target == null){
            return false;
        }

        if(isBusyState(target)) {
            return false;
        }

        if(target.distanceToSqr(actionPlayer) >= getMaxDistance() * getMaxDistance()){
            return false;
        }

        if(target.isPassenger() && !(target.getVehicle() instanceof SeatEntity)) {
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

    // TODO:动画状态机重构为本地的纯动画播放

    @Override
    public void onStart(ServerPlayer actionPlayer, LivingEntity target) {

        float yaw = actionPlayer.getYRot();
        float radians = (float) Math.toRadians(yaw);

        double offsetX = -Math.sin(radians) * 1.0F;
        double offsetZ = Math.cos(radians) * 1.0F;

        double targetX = actionPlayer.getX() + offsetX;
        double targetY = actionPlayer.getY();
        double targetZ = actionPlayer.getZ() + offsetZ;

        actionPlayer.setYRot(yaw);
        actionPlayer.setYBodyRot(yaw);
        actionPlayer.setYHeadRot(yaw);
        actionPlayer.connection.teleport(actionPlayer.getX(), actionPlayer.getY(), actionPlayer.getZ(), yaw, actionPlayer.getXRot());

        target.setYRot(yaw);
        target.setYBodyRot(yaw);
        target.setYHeadRot(yaw);

        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.connection.teleport(targetX, targetY, targetZ, yaw, target.getXRot());
        } else {
            target.moveTo(targetX, targetY, targetZ, yaw, target.getXRot());
        }
    }

    @Override
    public void onTick(ServerPlayer actionPlayer, LivingEntity target, int ticksRemaining) {
        float syncYaw = actionPlayer.getYRot();
        float radians = (float) Math.toRadians(syncYaw);

        double offsetX = -Math.sin(radians) * 1.0F;
        double offsetZ = Math.cos(radians) * 1.0F;

        double targetX = actionPlayer.getX() + offsetX;
        double targetY = actionPlayer.getY();
        double targetZ = actionPlayer.getZ() + offsetZ;

        actionPlayer.setYBodyRot(syncYaw);
        actionPlayer.setYHeadRot(syncYaw);

        target.setYRot(syncYaw);
        target.setYBodyRot(syncYaw);
        target.setYHeadRot(syncYaw);

        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.connection.teleport(targetX, targetY, targetZ, syncYaw, targetPlayer.getXRot());
        } else {
            target.moveTo(targetX, targetY, targetZ, syncYaw, target.getXRot());
        }
    }


    @Override
    public void onFinish(ServerPlayer actionPlayer, LivingEntity target) {
         startCarrying(actionPlayer, target,getCarryType(actionPlayer, target));
    }

    @Override
    public void onAbort(ServerPlayer actionPlayer, LivingEntity target) {
        target.stopRiding();
        clearCarryData(actionPlayer);
        clearCarryData(target);
    }
}