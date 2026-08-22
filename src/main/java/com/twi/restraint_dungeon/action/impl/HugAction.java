package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.CarryAction;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
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
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getLegsPose;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public class HugAction extends CarryAction {

    @Override
    public String getActionId() {
        return "HUG";
    }

    @Override
    public int getAnimTicks() {
        return 40;
    }

    @Override
    public String getCarryType(Player carrier, LivingEntity target) {

        return "CARRY_HUG";
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


        if(getRestraintPosition(entity) != RestraintPosition.SITTING){
            return Component.translatable("action." + MODID + ".fail_hug.need_target_sitting").withStyle(ChatFormatting.DARK_RED);
        }

        if(getLegsPose(entity) != RestraintCapability.LegsPose.LEGS_TOGETHER){
            return Component.translatable("action." + MODID + ".fail_hug.invalid_legs_pose").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }
}