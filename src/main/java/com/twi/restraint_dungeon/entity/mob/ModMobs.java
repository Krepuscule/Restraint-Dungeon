package com.twi.restraint_dungeon.entity.mob;

import com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.VivifiedArmorEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModMobs {

    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<VivifiedArmorEntity>> VIVIFIED_ARMOR =
            ENTITIES.register("vivified_armor", () -> EntityType.Builder.<VivifiedArmorEntity>of(VivifiedArmorEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("test_npc")
            );


    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}