package com.twi.restraint_dungeon.event.custom_event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

public class RestraintToolsEquipEvent extends LivingEvent {
    private final int index;
    private final ItemStack stack;

    public RestraintToolsEquipEvent(LivingEntity entity, int index, ItemStack stack) {
        super(entity);
        this.index = index;
        this.stack = stack;

    }

    public @NotNull LivingEntity getEntity() {
        return super.getEntity();
    }

    public int getIndex() { return index; }

    public ItemStack getStack() { return stack; }

    /**
     * 当一件小玩具被装备时触发
     */
    public static class Equipped extends RestraintToolsEquipEvent {
        public Equipped(LivingEntity entity, int index, ItemStack stack) {
            super(entity,index, stack);
        }
    }

    /**
     * 当一件小玩具被卸下时触发
     */
    public static class Unequipped extends RestraintToolsEquipEvent {
        public Unequipped(LivingEntity entity,int index, ItemStack stack) {
            super(entity,index, stack);
        }
    }
}
