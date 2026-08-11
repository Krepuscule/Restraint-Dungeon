package com.twi.restraint_dungeon.entity.npc.utils.goal;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindLegs;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBlindfold;

/**
 * 当有一个或多个玩家靠近 5 格以内，NPC 会立刻停止移动并看向目标玩家。
 */
public class NPCStopAndLookPlayerGoal extends Goal {
    private final BaseNPCEntity npc;
    private final float lookDistance;
    private Player closestPlayer;

    public NPCStopAndLookPlayerGoal(BaseNPCEntity npc, float lookDistance) {
        this.npc = npc;
        this.lookDistance = lookDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {

        if(isBeenBlindfold(npc) || isBeenBindLegs(npc)) return false;

        List<Player> nearbyPlayers = this.npc.level().getEntitiesOfClass(
                Player.class,
                this.npc.getBoundingBox().inflate(this.lookDistance),
                player -> player.isAlive() && !player.isSpectator()
        );

        if (nearbyPlayers.isEmpty()) {
            this.closestPlayer = null;
            return false;
        }

        Player localClosest = null;
        double minDistanceSqr = Double.MAX_VALUE;

        for (Player player : nearbyPlayers) {
            double distSqr = this.npc.distanceToSqr(player);
            if (distSqr < minDistanceSqr) {
                minDistanceSqr = distSqr;
                localClosest = player;
            }
        }

        this.closestPlayer = localClosest;
        return this.closestPlayer != null;
    }

    @Override
    public boolean canContinueToUse() {
        if(isBeenBlindfold(npc) || isBeenBindLegs(npc)) return false;
        if (this.closestPlayer == null || !this.closestPlayer.isAlive()) {
            return false;
        }
        return this.npc.distanceToSqr(this.closestPlayer) <= (this.lookDistance * this.lookDistance);
    }

    @Override
    public void start() {
        this.npc.getNavigation().stop();
        Vec3 velocity = this.npc.getDeltaMovement();
        this.npc.setDeltaMovement(0.0D, velocity.y, 0.0D);
    }

    @Override
    public void stop() {
        this.closestPlayer = null;
    }

    @Override
    public void tick() {
        this.canUse();

        if (this.closestPlayer != null) {
            this.npc.getLookControl().setLookAt(
                    this.closestPlayer.getX(),
                    this.closestPlayer.getEyeY(),
                    this.closestPlayer.getZ(),
                    10.0F,
                    (float) this.npc.getMaxHeadXRot()
            );
        }
    }
}