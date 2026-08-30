package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.AnimAction;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBusyState;

public class TouchAction extends AnimAction {

    @Override
    public String getActionId() {
        return "TOUCH";
    }

    public boolean isInfinite() {
        return true;
    }

    @Override
    public int getAnimTicks() {
        return 20;
    }

    @Override
    public boolean shouldAnimFaceActor() {
        return true;
    }


    @Override
    public double getMaxDistance() {
        return 2.0;
    }

    @Override
    public Component canUse(Player actionPlayer, @Nullable HitResult result){

        if(super.canUse(actionPlayer,result) != null){
            return super.canUse(actionPlayer, result);
        }

        LivingEntity entity = null;
        if (result != null) {
            entity = (LivingEntity) ((EntityHitResult) result).getEntity();
        }

        if(!(entity instanceof Player || entity instanceof BaseNPCEntity)){
            return Component.translatable("action." + MODID + ".fail_touch.invalid_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(getRestraintPosition(entity) != RestraintPositionEvent.RestraintPosition.KNEELING){
            return Component.translatable("action." + MODID + ".fail_touch.invalid_position").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }

    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target, HitResult result, String CarryingState, Boolean isCarryTarget) {

        if(!(target instanceof Player || target instanceof BaseNPCEntity)){
            return false;
        }

        if(getRestraintPosition(target) != RestraintPositionEvent.RestraintPosition.KNEELING){
            return false;
        }

        return super.shouldShowInMenu(actionPlayer, target, result, CarryingState, isCarryTarget);
    }

    private float initialYaw;
    private float initialPitch;

    @Override
    public void onStart(ServerPlayer actionPlayer, LivingEntity target, HitResult hitResult) {
        this.initialYaw = actionPlayer.getYRot();
        this.initialPitch = actionPlayer.getXRot();

        float radians = (float) Math.toRadians(initialYaw);

        double offsetX = -Math.sin(radians) * 1.0F;
        double offsetZ = Math.cos(radians) * 1.0F;

        double targetX = actionPlayer.getX() + offsetX;
        double targetY = actionPlayer.getY();
        double targetZ = actionPlayer.getZ() + offsetZ;

        actionPlayer.setYRot(initialYaw);
        actionPlayer.setXRot(initialPitch);
        actionPlayer.setYBodyRot(initialYaw);
        actionPlayer.setYHeadRot(initialYaw);
        actionPlayer.connection.teleport(actionPlayer.getX(), actionPlayer.getY(), actionPlayer.getZ(), initialYaw, initialPitch);

        float targetYaw = initialYaw + 180.0F;
        target.setYRot(targetYaw);
        target.setXRot(initialPitch);
        target.setYBodyRot(targetYaw);
        target.setYHeadRot(targetYaw);

        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.connection.teleport(targetX, targetY, targetZ, targetYaw, initialPitch);
        } else {
            target.moveTo(targetX, targetY, targetZ, targetYaw, initialPitch);
        }
    }

    @Override
    public void onTick(ServerPlayer actionPlayer, LivingEntity target, HitResult result, int ticksRemaining) {
        float radians = (float) Math.toRadians(initialYaw);

        double offsetX = -Math.sin(radians) * 1.0F;
        double offsetZ = Math.cos(radians) * 1.0F;

        double targetX = actionPlayer.getX() + offsetX;
        double targetY = actionPlayer.getY();
        double targetZ = actionPlayer.getZ() + offsetZ;

        actionPlayer.setYRot(initialYaw);
        actionPlayer.setXRot(initialPitch);
        actionPlayer.setYBodyRot(initialYaw);
        actionPlayer.setYHeadRot(initialYaw);

        float targetYaw = initialYaw + 180.0F;
        target.setYRot(targetYaw);
        target.setXRot(initialPitch);
        target.setYBodyRot(targetYaw);
        target.setYHeadRot(targetYaw);

    }
}
