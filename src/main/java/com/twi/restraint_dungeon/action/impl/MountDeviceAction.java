package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.AnimAction;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBusyState;

public class MountDeviceAction extends AnimAction {
    @Override
    public String getActionId() {
        return "MOUNT_DEVICE";
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
    public Component canUse(Player actionPlayer, @Nullable HitResult result){

        if(!actionPlayer.isAlive()){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBusyState(actionPlayer)) {
            return Component.translatable("action." + MODID + ".fail_common.cant_action_state").withStyle(ChatFormatting.DARK_RED);
        }


        if(isRidingRestraintDevice(actionPlayer)){
            return Component.translatable("action." + MODID + ".fail_mount_device.already_on_device")
                    .withStyle(ChatFormatting.DARK_RED);
        }

        BlockPos targetPos = findTargetDevicePos(actionPlayer.level(), result);
        if (targetPos == null || !(actionPlayer.level().getBlockState(targetPos).getBlock() instanceof RestraintDevice)) {
            return Component.translatable("action." + MODID + ".fail_mount_device.invalid_block")
                    .withStyle(ChatFormatting.DARK_RED);
        }

        if (actionPlayer.level().getBlockState(targetPos).getBlock() instanceof RestraintDevice device){
            if(!device.isMountableDevice(actionPlayer.level(),targetPos)){
                return Component.translatable("action." + MODID + ".fail_mount_device.invalid_block")
                        .withStyle(ChatFormatting.DARK_RED);
            }

            if(actionPlayer.distanceToSqr(Vec3.atCenterOf(targetPos)) > getMaxDistance() * getMaxDistance()){
               return Component.translatable("action." + MODID + ".fail_mount_device.too_far")
                        .withStyle(ChatFormatting.DARK_RED);
            }

            if(device.canMount(actionPlayer.level(),targetPos,actionPlayer) != null){
                return device.canMount(actionPlayer.level(),targetPos,actionPlayer);
            }
        }

        return null;
    }

    @Override
    public boolean canContinueUse(ServerPlayer actionPlayer, LivingEntity targetEntity) {
        return !isRidingRestraintDevice(actionPlayer);
    }

    public boolean shouldShowInMenu(Player actionPlayer, @javax.annotation.Nullable LivingEntity target, HitResult result, String CarryingState, Boolean isCarryTarget) {

        if(!actionPlayer.isAlive()){
            return false;
        }

        if(isBusyState(actionPlayer)) {
            return false;
        }

        if(isRidingRestraintDevice(actionPlayer)){
            return false;
        }

        BlockPos targetPos = findTargetDevicePos(actionPlayer.level(), result);
        if (targetPos == null || !(actionPlayer.level().getBlockState(targetPos).getBlock() instanceof RestraintDevice)) {
            return false;
        }

        if (actionPlayer.level().getBlockState(targetPos).getBlock() instanceof RestraintDevice device){
            if(!device.isMountableDevice(actionPlayer.level(),targetPos)){
                return false;
            }
            if(device.canMount(actionPlayer.level(),targetPos,actionPlayer) != null){
                return false;
            }
        }

        return true;
    }


    @Override
    public void onStart(ServerPlayer actionPlayer, @Nullable LivingEntity target, HitResult hitResult) {

    }

    @Override
    public void onTick(ServerPlayer actionPlayer, @Nullable LivingEntity target, HitResult hitResult,int ticksRemaining) {

    }


    @Override
    public void onAbort(ServerPlayer actionPlayer, @Nullable LivingEntity target, HitResult hitResult) {

    }

    @Override
    public void onFinish(ServerPlayer actionPlayer, @Nullable LivingEntity target, HitResult hitResult) {

        if (hitResult == null) return;

        Level level = actionPlayer.level();

        BlockPos targetPos = findTargetDevicePos(level, hitResult);
        if (targetPos == null) return;

        if (level.getBlockState(targetPos).getBlock() instanceof RestraintDevice device) {

            if (device.canMount(level, targetPos, actionPlayer) == null) {
                boolean success = device.mount(level, targetPos, actionPlayer);

                if (!success) {
                    actionPlayer.displayClientMessage(
                            Component.translatable("action." + MODID + ".fail_mount_device.fail").withStyle(ChatFormatting.RED),
                            true
                    );
                }
            } else {
                actionPlayer.displayClientMessage(
                        device.canMount(level, targetPos, actionPlayer),
                        true
                );
            }
        }
    }


    @Nullable
    private BlockPos findTargetDevicePos(Level level, HitResult result) {
        if (result instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
            BlockPos hitPos = blockHit.getBlockPos();

            if (level.getBlockEntity(hitPos) instanceof GhostBlockEntity ghostBE) {
                BlockPos master = ghostBE.getMasterPos();
                if (master != null) {
                    hitPos = master;
                }
            }

            if (level.getBlockState(hitPos).getBlock() instanceof RestraintDevice) {
                return hitPos;
            }
        }
        return null;
    }
}
