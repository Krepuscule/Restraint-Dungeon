package com.twi.restraint_dungeon.entity.npc.utils.goal;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindLegs;

public class NPCRandomStrollGoal extends Goal {
    private final BaseNPCEntity npc;
    private final double speedModifier;

    public NPCRandomStrollGoal(BaseNPCEntity npc, double speedModifier) {
        this.npc = npc;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.npc.isVehicle() || !this.npc.getNavigation().isDone()) return false;

        if (this.npc.getRandom().nextInt(reducedTickDelay(30)) != 0) return false;

        if(isBeenBindLegs(npc)) return false;

        Vec3 randomPos = DefaultRandomPos.getPos(this.npc, 5, 3);
        if (randomPos == null) return false;

        double distanceSqr = randomPos.distanceToSqr(this.npc.getHomePos().getCenter());
        double radius = this.npc.getHomeRadius();
        
        if (distanceSqr > (radius * radius)) {
            return false;
        }

        return this.npc.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, this.speedModifier);
    }
}