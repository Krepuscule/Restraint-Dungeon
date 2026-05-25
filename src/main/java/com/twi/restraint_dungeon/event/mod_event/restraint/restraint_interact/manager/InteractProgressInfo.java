package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class InteractProgressInfo {
    public final BlockPos pos;
    public final Entity targetEntity;
    public final Vec3 startPos;
    public final InteractionHand hand;
    public final int targetTicks;
    public final double maxRange;
    public int ticks = 0;
    public int timeoutTicks = 0;
    public boolean isStopping = false;
    public final Consumer<ServerPlayer> onComplete;

    public InteractProgressInfo(BlockPos pos, Entity entity, Vec3 startPos, InteractionHand hand, int targetTicks, double maxRange, Consumer<ServerPlayer> onComplete) {
        this.pos = pos;
        this.targetEntity = entity;
        this.startPos = startPos;
        this.hand = hand;
        this.targetTicks = targetTicks;
        this.maxRange = maxRange;
        this.onComplete = onComplete;
    }
}