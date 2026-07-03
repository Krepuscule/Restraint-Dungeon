package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class RestraintDeviceMountEvent extends LivingEvent implements ICancellableEvent{
    private final RestraintDevice device;
    private final Level level;
    private final BlockPos pos;

    public RestraintDeviceMountEvent(LivingEntity target, RestraintDevice device, Level level,BlockPos pos) {
        super(target);
        this.device = device;
        this.level = level;
        this.pos = pos;
    }

    public RestraintDevice getDevice() { return device; }
    public Level getLevel(){return level;}
    public BlockPos getPos() { return pos; }


    public static class Pre extends RestraintDeviceMountEvent implements ICancellableEvent{
        public Pre(LivingEntity target, RestraintDevice device, Level level,BlockPos pos){
            super(target,device,level,pos);
        }
    }

    public static class Post extends RestraintDeviceMountEvent{



        public Post(LivingEntity target, RestraintDevice device, Level level,BlockPos pos){
            super(target,device,level,pos);
        }

    }
}