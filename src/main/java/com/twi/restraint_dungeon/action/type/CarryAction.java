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
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.clearCarryData;
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

    @Override
    public void onStart(ServerPlayer carrier, LivingEntity target) {
        float lookYaw = carrier.getYRot();

        double rad = Math.toRadians(lookYaw);
        double offsetX = -Math.sin(rad) * 0.5;
        double offsetZ = Math.cos(rad) * 0.5;

        target.teleportTo(
                carrier.serverLevel(),
                carrier.getX() + offsetX,
                carrier.getY(),
                carrier.getZ() + offsetZ,
                Set.of(),
                lookYaw,
                0f
        );
    }

    @Override
    public void onFinish(ServerPlayer carrier, LivingEntity target) {
         PlayerCarryUtils.startCarrying(carrier, target,getCarryType(carrier, target));
    }

    @Override
    public void onAbort(ServerPlayer carrier, LivingEntity target) {
        target.stopRiding();
        clearCarryData(carrier);
        clearCarryData(target);
    }
}