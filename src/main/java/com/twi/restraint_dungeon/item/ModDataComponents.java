package com.twi.restraint_dungeon.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    // 基础属性
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_RESISTANCE =
            register("max_resistance", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> THRILL_VALUE =
            register("thrill_value", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> STRENGTH_INDEX =
            register("strength_index", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> LOOSE_INDEX =
            register("loose_index", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> LOCK_INDEX =
            register("lock_index", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> LOCK_ITEM =
            register("lock_item", builder -> builder
                    .persistent(CustomData.CODEC)
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG.map(CustomData::of, CustomData::copyTag))
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> EQUIP_TIME =
            register("equip_time", builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> RESTRAINT_PAIRING_ID =
            register("restraint_pairing_id", builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, Boolean>>> MIRAI_MODULES =
            register("mirai_modules", builder -> builder.persistent(Codec.unboundedMap(Codec.STRING, Codec.BOOL))
                    .networkSynchronized(ByteBufCodecs.map(java.util.LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.BOOL)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AROUSED_MODE =
            register("aroused_mode", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DENY_MODE =
            register("deny_mode", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ACTIVATE_TIME =
            register("activate_time", builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> DENY_ACTIVATE_TIME =
            register("deny_activate_time", builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));


    // 小玩具属性
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> VIBE_ACTIVATE =
            register("vibe_activate", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> VIBE_LEVEL =
            register("vibe_level", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    // 锁具/钥匙配对属性
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> LOCK_PAIRING_ID =
            register("lock_pairing_id", builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));

    /**
     * 统一注册辅助方法
     */
    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }
}