package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 拘束具装备变化事件监听
 */
public class RestraintEquipEvent extends LivingEvent {
    private final PlayerRestraintPart part;
    private final int index;
    private final ItemStack stack;

    public RestraintEquipEvent(LivingEntity entity, PlayerRestraintPart part, int index, ItemStack stack) {
        super(entity);
        this.part = part;
        this.index = index;
        this.stack = stack;

    }

    public @NotNull LivingEntity getEntity() {
        return super.getEntity();
    }

    public PlayerRestraintPart getPart() { return part; }

    public int getIndex() { return index; }

    public ItemStack getStack() { return stack; }

    /**
     * 当一件拘束具被装备时触发
     */
    public static class Equipped extends RestraintEquipEvent {
        public Equipped(LivingEntity entity, PlayerRestraintPart part, int index, ItemStack stack) {
            super(entity, part, index, stack);
        }
    }

    /**
     * 当一件拘束具被卸下时触发
     */
    public static class Unequipped extends RestraintEquipEvent {
        public Unequipped(LivingEntity entity, PlayerRestraintPart part, int index, ItemStack stack) {
            super(entity, part, index, stack);
        }
    }
}
