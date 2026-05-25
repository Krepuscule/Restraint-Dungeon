package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

public class RestraintPositionChangeEvent extends LivingEvent {
    private final RestraintPosition prev;
    private final RestraintPosition next;

    public RestraintPositionChangeEvent(LivingEntity entity, RestraintPosition prev,RestraintPosition next) {
        super(entity);
        this.prev = prev;
        this.next = next;
    }

    public @NotNull LivingEntity getEntity() {
        return super.getEntity();
    }

    public RestraintPosition getPrev() {return prev;}

    public RestraintPosition getNext() { return next; }

    /**
     * 当一件拘束具被装备时触发
     */
    public static class Pre extends RestraintPositionChangeEvent {
        public Pre(LivingEntity entity, RestraintPosition prev,RestraintPosition next) {
            super(entity, prev,next);
        }
    }

    /**
     * 当一件拘束具被卸下时触发
     */
    public static class Post extends RestraintPositionChangeEvent {
        public Post(LivingEntity entity, RestraintPosition prev,RestraintPosition next) {
            super(entity, prev, next);
        }
    }
}
