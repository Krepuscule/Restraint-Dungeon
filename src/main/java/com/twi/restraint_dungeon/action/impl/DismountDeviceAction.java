package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.AnimAction;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;

import java.util.Objects;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;

public class DismountDeviceAction extends AnimAction {

    @Override
    public String getActionId() {
        return "DISMOUNT_DEVICE";
    }

    @Override
    public int getAnimTicks() {
        return 1;
    }

    @Override
    public double getMaxDistance() {
        return 2.0;
    }

    @Override
    public @Nullable Component canUse(Player actionPlayer, HitResult result) {

        if(!isRidingRestraintDevice(actionPlayer)){
            return Component.translatable("action." + MODID + ".fail_dismount_device.not_in_device");
        }

        if(getRestraintDevice(actionPlayer) instanceof RestraintDevice device
                && device.canDismount(
                        actionPlayer.level(),
                Objects.requireNonNull(actionPlayer.getVehicle()).blockPosition(),
                actionPlayer) != null
        ){
            return  device.canDismount(
                    actionPlayer.level(),
                    Objects.requireNonNull(actionPlayer.getVehicle()).blockPosition(),
                    actionPlayer);
        }

        return null;
    }

    @Override
    public boolean canContinueUse(ServerPlayer actionPlayer, LivingEntity targetEntity) {
        return actionPlayer.isAlive() && isRidingRestraintDevice(actionPlayer);
    }

    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target,HitResult result, String CarryingState, Boolean isCarryTarget){

        if(!isRidingRestraintDevice(actionPlayer)){
            return false;
        }


        return true;
    }

    @Override
    public void onStart(ServerPlayer actionPlayer, LivingEntity entity,HitResult hitResult) {

    }

    @Override
    public void onTick(ServerPlayer actionPlayer, LivingEntity target,HitResult result, int ticksRemaining){

    }


    @Override
    public void onAbort(ServerPlayer actionPlayer, LivingEntity target,HitResult result) {

    }

    @Override
    public void onFinish(ServerPlayer actionPlayer, LivingEntity target,HitResult result) {

        if(isRidingRestraintDevice(actionPlayer)
                && getRestraintDevice(actionPlayer) instanceof RestraintDevice device
                && device.canDismount(
                actionPlayer.level(),
                Objects.requireNonNull(actionPlayer.getVehicle()).blockPosition(),
                actionPlayer) == null){
            device.dismount(actionPlayer.level(),
                    Objects.requireNonNull(actionPlayer.getVehicle()).blockPosition()
                    ,actionPlayer);
        }
    }

}