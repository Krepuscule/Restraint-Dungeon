package com.twi.restraint_dungeon.attachment.attributes;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class ModAttributesRegisterEvent {

    @SubscribeEvent
    public static void modifyAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.RESTRAINT_STRENGTH);
        event.add(EntityType.PLAYER, ModAttributes.STRUGGLE_STRENGTH);
        event.add(EntityType.PLAYER, ModAttributes.STRUGGLE_SPEED);
        event.add(EntityType.PLAYER, ModAttributes.STRUGGLE_RANGE);
        event.add(EntityType.PLAYER,ModAttributes.RESTRAINT_MOVE_ATTRIBUTES);
    }
}