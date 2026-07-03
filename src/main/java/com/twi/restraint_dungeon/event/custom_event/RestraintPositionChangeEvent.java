package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

public class RestraintPositionChangeEvent extends LivingEvent {
    private final RestraintPosition prev;
    private final RestraintPosition next;
    private final ItemStack stack;

    public RestraintPositionChangeEvent(LivingEntity entity, RestraintPosition prev,RestraintPosition next,ItemStack stack) {
        super(entity);
        this.prev = prev;
        this.next = next;
        this.stack = stack;
    }

    public @NotNull LivingEntity getEntity() {
        return super.getEntity();
    }

    public RestraintPosition getPrev() {return prev;}

    public RestraintPosition getNext() { return next; }

    public ItemStack getStack(){return stack;}

    /**
     * 当一件拘束具被装备时触发
     */
    public static class Pre extends RestraintPositionChangeEvent {
        public Pre(LivingEntity entity, RestraintPosition prev,RestraintPosition next,ItemStack stack) {
            super(entity, prev,next,stack);
        }
    }

    /**
     * 当一件拘束具被卸下时触发
     */
    public static class Post extends RestraintPositionChangeEvent {
        public Post(LivingEntity entity, RestraintPosition prev,RestraintPosition next,ItemStack stack) {
            super(entity, prev, next,stack);
        }
    }
}
