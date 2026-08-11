package com.twi.restraint_dungeon.entity.npc.utils.goal;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindLegs;

/**
 * 当 NPC 超出设置的 home 半径距离后，会尝试移动返回原点。
 */
public class ReturnToHomeGoal extends Goal {
    private final BaseNPCEntity npc;
    private final double speedModifier;

    public ReturnToHomeGoal(BaseNPCEntity npc, double speedModifier) {
        this.npc = npc;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.npc.getHomePos() == null) return false;

        if(isBeenBindLegs(npc)) return false;

        double distanceSqr = this.npc.distanceToSqr(this.npc.getHomePos().getCenter());
        double radius = this.npc.getHomeRadius();

        return distanceSqr > (radius * radius);
    }

    @Override
    public boolean canContinueToUse() {
        if(isBeenBindLegs(npc)) return false;
        return this.npc.distanceToSqr(this.npc.getHomePos().getCenter()) > 2.25D;
    }

    @Override
    public void tick() {
        BlockPos home = this.npc.getHomePos();
        if (this.npc.getNavigation().isDone()) {
            this.npc.getNavigation().moveTo(home.getX(), home.getY(), home.getZ(), this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.npc.getNavigation().stop();
    }
}