package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.CarryAction;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import com.twi.restraint_dungeon.item.restraint_item.restraints.RopeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getFirstConnectBind;

public class RopeConnectionCarryAction extends CarryAction {
    @Override
    public String getActionId() {
        return "ROPE_CONNECTION_CARRY";
    }

    @Override
    public int getAnimTicks() {
        return 20;
    }

    @Override
    public String getCarryType(Player carrier, LivingEntity target) {

        return "CARRY_ROPE_CONNECTION";
    }

    @Override
    public Component canUse(Player actionPlayer, @Nullable HitResult result) {

        if(super.canUse(actionPlayer, result) != null){
            return super.canUse(actionPlayer,result);
        }

        LivingEntity entity = null;
        if (result != null) {
            entity = (LivingEntity) ((EntityHitResult) result).getEntity();
        }


        if(getRestraintPosition(entity) != RestraintPositionEvent.RestraintPosition.CONNECTING
                || !(getFirstConnectBind(entity).getItem() instanceof RopeItem)){
            return Component.translatable("action." + MODID + ".fail_rope_connection_carry.need_target_rope_connection").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }

    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target,HitResult result,String CarryingState,Boolean isCarryTarget){

        if(!super.shouldShowInMenu(actionPlayer, target, result, CarryingState, isCarryTarget)){
            return false;
        }

        if(getRestraintPosition(target) != RestraintPositionEvent.RestraintPosition.CONNECTING
                || !(getFirstConnectBind(target).getItem() instanceof RopeItem)){
            return false;
        }

        return true;
    }

    @Override
    public void onStart(ServerPlayer actionPlayer, LivingEntity target, HitResult hitResult) {

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
    public void onTick(ServerPlayer actionPlayer, LivingEntity target,HitResult result, int ticksRemaining) {
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
}
