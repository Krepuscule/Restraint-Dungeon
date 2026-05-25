package com.twi.restraint_dungeon.action.type;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenFullyBind;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBusyState;

public abstract class AnimAction extends BaseAction {

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

        if(living.isPassenger() && !(living.getVehicle() instanceof SeatEntity)) {
            return Component.translatable("action." + MODID + ".fail_common.target_riding").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBusyState(actionPlayer)) {
            return Component.translatable("action." + MODID + ".fail_common.cant_action_state").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }

    @Override
    public boolean canContinueUse(ServerPlayer actionPlayer, LivingEntity targetEntity) {
        return super.canContinueUse(actionPlayer, targetEntity);
    }

    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target, String CarryingState, Boolean isCarryTarget){
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

        if(isBeenFullyBind(actionPlayer)) {
            return false;
        }

        return true;
    }

    @Override
    public void onStart(ServerPlayer carrier, LivingEntity target) {
        float yaw = carrier.getYRot();

        carrier.setYRot(yaw);
        carrier.setYBodyRot(yaw);
        carrier.setYHeadRot(yaw);
        carrier.connection.teleport(carrier.getX(), carrier.getY(), carrier.getZ(), yaw, carrier.getXRot());

        target.setYRot(yaw);
        target.setYBodyRot(yaw);
        target.setYHeadRot(yaw);
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.connection.teleport(target.getX(), target.getY(), target.getZ(), yaw, target.getXRot());
        } else {
            target.moveTo(target.getX(), target.getY(), target.getZ(), yaw, target.getXRot());
        }
    }

    @Override
    public void onTick(ServerPlayer carrier, LivingEntity target, int ticksRemaining) {
        float syncYaw = carrier.getYRot();
        carrier.setYBodyRot(syncYaw);
        target.setYRot(syncYaw);
        target.setYBodyRot(syncYaw);
    }

    @Override
    public void onAbort(ServerPlayer carrier, LivingEntity target) {

    }

    @Override
    public void onFinish(ServerPlayer carrier, LivingEntity target) {

    }
}