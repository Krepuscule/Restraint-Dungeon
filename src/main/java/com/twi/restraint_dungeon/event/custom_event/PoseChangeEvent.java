package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.*;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

public class PoseChangeEvent extends LivingEvent {

    private final ArmsPose Prev_ArmsPose;
    private final ArmsPose Curr_ArmsPose;
    private final LegsPose Prev_LegsPose;
    private final LegsPose Curr_LegsPose;

    public PoseChangeEvent(LivingEntity entity,
                           ArmsPose prev_ArmsPose,ArmsPose curr_ArmsPose,
                           LegsPose prev_LegsPose,LegsPose curr_LegsPose) {
        super(entity);
        this.Prev_ArmsPose = prev_ArmsPose;
        this.Curr_ArmsPose = curr_ArmsPose;
        this.Prev_LegsPose = prev_LegsPose;
        this.Curr_LegsPose = curr_LegsPose;
    }

    public @NotNull LivingEntity getEntity(){return super.getEntity();}

    public ArmsPose getPrevArmsPose(){return Prev_ArmsPose;}
    public ArmsPose getCurrArmsPose(){return Curr_ArmsPose;}

    public LegsPose getPrevLegsPose(){return Prev_LegsPose;}
    public LegsPose getCurrLegsPose(){return Curr_LegsPose;}
}
