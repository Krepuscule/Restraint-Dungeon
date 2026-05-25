package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class RestraintChangeEvent extends Event {
    private final LivingEntity entity;
    private final PlayerRestraintPart part;
    private final int index;
    private final ItemStack oldStack;
    private final ItemStack newStack;

    public RestraintChangeEvent(LivingEntity entity,PlayerRestraintPart part, int index, ItemStack oldStack, ItemStack newStack) {
        this.entity = entity;
        this.part = part;
        this.index = index;
        this.oldStack = oldStack;
        this.newStack = newStack;
    }


    public LivingEntity getEntity() {
        return this.entity;
    }

    public PlayerRestraintPart getPart() {return this.part;}


    public int getIndex() {
        return this.index;
    }


    public ItemStack getOldStack() {
        return this.oldStack;
    }


    public ItemStack getNewStack() {
        return this.newStack;
    }
}