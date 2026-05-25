package com.twi.restraint_dungeon.attachment.attributes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, MODID);

    public static final DeferredHolder<Attribute, Attribute> RESTRAINT_STRENGTH = ATTRIBUTES.register("restraint_strength",
            () -> new RangedAttribute(
                    "attribute.restraint_dungeon.restraint_strength",
                    1.0D, 0.0D, 100.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> STRUGGLE_STRENGTH = ATTRIBUTES.register("struggle_strength",
            () -> new RangedAttribute(
                    "attribute.restraint_dungeon.struggle_strength",
                    1.0D, 0.0D, 100.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> STRUGGLE_SPEED = ATTRIBUTES.register("struggle_speed",
            () -> new RangedAttribute(
                    "attribute.restraint_dungeon.struggle_speed",
                    1.0D, 0.0D, 100.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> STRUGGLE_RANGE = ATTRIBUTES.register("struggle_range",
            () -> new RangedAttribute(
                    "attribute.restraint_dungeon.struggle_range",
                    1.0D, 0.0D, 100.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> RESTRAINT_MOVE_ATTRIBUTES = ATTRIBUTES.register("restraint_move_attributes",
            () -> new RangedAttribute(
                    "attribute.restraint_dungeon.restraint_move_attributes",
                    1.0D, 0.0D, 100.0D
            ).setSyncable(true)
    );
}