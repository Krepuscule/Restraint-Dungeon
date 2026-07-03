package com.twi.restraint_dungeon.entity.npc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModNPCs {

    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GenericNPCEntity>> GENERIC_NPC =
            ENTITIES.register("generic_npc", () -> EntityType.Builder.<GenericNPCEntity>of(GenericNPCEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("generic_npc")
            );


    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}