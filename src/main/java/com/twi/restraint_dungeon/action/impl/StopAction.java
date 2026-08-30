package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.getCurrentAction;

public class StopAction extends BaseAction {

    @Override
    public String getActionId() {
        return "STOP";
    }

    @Override
    public int getAnimTicks() {
        return 20;
    }

    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target, HitResult result, String CarryingState, Boolean isCarryTarget) {
        return PlayerActionUtils.isDoingAction(actionPlayer) && Objects.requireNonNull(getCurrentAction(actionPlayer)).isInfinite();
    }

    @Override
    public Component canUse(Player actionPlayer, @Nullable HitResult result) {
        return null;
    }
}
