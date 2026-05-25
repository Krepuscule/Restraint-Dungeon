package com.twi.restraint_dungeon.client.command;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MODID);


    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<RestraintPartArgument>> RESTRAINT_PART =
            ARGUMENT_TYPES.register("restraint_part", () -> 
                    ArgumentTypeInfos.registerByClass(RestraintPartArgument.class, SingletonArgumentInfo.contextFree(RestraintPartArgument::restraintPart)));

}