package com.twi.restraint_dungeon.event.custom_event;


import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 高潮值变化事件监听
 */
public abstract class PleasantValueEvent extends Event {
    private final LivingEntity entity;
    private double amount;

    public PleasantValueEvent(LivingEntity entity, double amount) {
        this.entity = entity;
        this.amount = amount;
    }

    public LivingEntity getEntity() { return entity; }
    public double getAmount() { return amount; }

    /** 高潮值增加事件 (可取消) */
    public static class Increase extends PleasantValueEvent implements ICancellableEvent {
        public Increase(LivingEntity entity, double amount) { super(entity, amount); }
    }

    /** 高潮值减少事件 (可取消) */
    public static class Decrease extends PleasantValueEvent implements ICancellableEvent {
        public Decrease(LivingEntity entity, double amount) { super(entity, amount); }
    }
}