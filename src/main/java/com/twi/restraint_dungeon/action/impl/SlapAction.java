package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.event.mod_event.player_carry.type.CarryHug;
import com.twi.restraint_dungeon.event.mod_event.player_carry.type.CarryShoulder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenFullyBind;

public class SlapAction extends CarryingAction {

    @Override
    public String getActionId() {
        return "SLAP";
    }

    @Override
    public int getAnimTicks() {
        return 10;
    }

    @Override
    public @Nullable Component canUse(Player carrier, HitResult result) {
        if(getCarriedPassenger(carrier) == null || carrier == null){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        LivingEntity target = getCarriedPassenger(carrier);

        if(!(target instanceof Player)){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(!isCarrier(carrier) || !isBeingCarried(target)){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(!carrier.isAlive() || !target.isAlive()){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(target.distanceToSqr(carrier) >= getMaxDistance() * getMaxDistance()){
            return Component.translatable("action." + MODID + ".fail_common.too_far").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBeenBindArms(carrier) || isBeenBindHands(carrier) || isBeenBindLegs(carrier)) {
            return Component.translatable("action." + MODID + ".fail_common.is_being_binding").withStyle(ChatFormatting.DARK_RED);
        }

        if(!isBeenFullyBind(target)) {
            return Component.translatable("action." + MODID + ".fail_carry.need_bind").withStyle(ChatFormatting.DARK_RED);
        }

        if(!target.isPassenger() || !(target.getVehicle() instanceof Player) || getPartnerUUID(target) != carrier.getUUID()) {
            return Component.translatable("action." + MODID + ".fail_carrying.not_carrying").withStyle(ChatFormatting.DARK_RED);
        }

        if(!(getCurrentCarryType(carrier) instanceof CarryHug || getCurrentCarryType(carrier) instanceof CarryShoulder)){
            return Component.translatable("action." + MODID + ".fail_slap.incorrect_carry_type")
                    .withStyle(ChatFormatting.RED);
        }

        return null;
    }

    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target,HitResult result,String CarryingState,Boolean isCarryTarget) {
        if(!(target instanceof Player)){
            return false;
        }

        return super.shouldShowInMenu(actionPlayer, target, result, CarryingState, isCarryTarget);
    }

    @Override
    public void onStart(ServerPlayer carrier, LivingEntity target,HitResult hitResult) {
        carrier.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);

        target.hurt(carrier.damageSources().playerAttack(carrier), 1.0F);
    }

    @Override
    public void onFinish(ServerPlayer carrier, LivingEntity target,HitResult result) {

    }
}