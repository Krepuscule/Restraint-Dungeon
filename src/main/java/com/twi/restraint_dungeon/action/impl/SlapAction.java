package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.CarryingAction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

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
        if(super.canUse(carrier, result) != null){
            return super.canUse(carrier,result);
        }
        return null;
    }

    @Override
    public void onStart(ServerPlayer carrier, LivingEntity target) {
        carrier.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);

        target.hurt(carrier.damageSources().playerAttack(carrier), 1.0F);
        

//        if (target instanceof ServerPlayer targetPlayer) {
//
//        }
    }

    @Override
    public void onFinish(ServerPlayer carrier, LivingEntity target) {

    }
}