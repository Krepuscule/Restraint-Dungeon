package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.*;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

public class PoseChangeEvent extends LivingEvent {

    public PoseChangeEvent(LivingEntity entity) {
        super(entity);
    }

    public @NotNull LivingEntity getEntity(){return super.getEntity();}


    public static class ArmsPoseChangeEvent extends PoseChangeEvent{
        private final ArmsPose Prev_ArmsPose;
        private final ArmsPose Curr_ArmsPose;

        public ArmsPoseChangeEvent(LivingEntity entity,
                               ArmsPose prev_ArmsPose,ArmsPose curr_ArmsPose) {
            super(entity);
            this.Prev_ArmsPose = prev_ArmsPose;
            this.Curr_ArmsPose = curr_ArmsPose;
        }

        public ArmsPose getPrevArmsPose(){return Prev_ArmsPose;}
        public ArmsPose getCurrArmsPose(){return Curr_ArmsPose;}
    }

    public static class LegsPoseChangeEvent extends PoseChangeEvent{
        private final LegsPose Prev_LegsPose;
        private final LegsPose Curr_LegsPose;

        public LegsPoseChangeEvent(LivingEntity entity, LegsPose prev_LegsPose,LegsPose curr_LegsPose) {
            super(entity);
            this.Prev_LegsPose = prev_LegsPose;
            this.Curr_LegsPose = curr_LegsPose;
        }

        public LegsPose getPrevLegsPose(){return Prev_LegsPose;}
        public LegsPose getCurrLegsPose(){return Curr_LegsPose;}
    }
}
