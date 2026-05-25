package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface IInteractHandler {
    boolean isTarget(BlockState state);
    boolean canDo(Player player);
    double getInteractRange(Player player, BlockState state);
    int getInteractTime(Player player, BlockState state);
    void onComplete(ServerPlayer player, BlockPos pos, InteractionHand hand);
}
