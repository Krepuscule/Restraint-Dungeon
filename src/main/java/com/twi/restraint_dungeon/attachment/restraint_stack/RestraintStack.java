package com.twi.restraint_dungeon.attachment.restraint_stack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RestraintStack {

    private static final int DEFAULT_LIMIT = 8;
    private static final int DEFAULT_COLLAR_LIMIT = 1;

    /**
     * 各个部位拘束具的上限值
     */
    public static int getPartRestraintLimit(PlayerRestraintPart part) {
        return switch (part) {
            case restraint_blindfold -> DEFAULT_LIMIT;
            case restraint_gag -> DEFAULT_LIMIT;
            case restraint_collar -> DEFAULT_COLLAR_LIMIT;
            case restraint_body_bind -> DEFAULT_LIMIT;
            case restraint_arms_bind -> DEFAULT_LIMIT;
            case restraint_hands_bind -> DEFAULT_LIMIT;
            case restraint_legs_bind -> DEFAULT_LIMIT;
            case restraint_connection -> 1;
            default -> DEFAULT_LIMIT;
        };
    }

    private final Map<PlayerRestraintPart, List<ItemStack>> storage = new EnumMap<>(PlayerRestraintPart.class);

    public RestraintStack() {
        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            storage.put(part, new ArrayList<>());
        }
    }

    public List<ItemStack> getPartList(PlayerRestraintPart part) {
        return storage.get(part);
    }


    public void addToLast(PlayerRestraintPart part, ItemStack stack) {
        List<ItemStack> list = storage.get(part);
        if (list.size() < getPartRestraintLimit(part)) {
            list.add(stack);
        }
    }

    public ItemStack removeFromLast(PlayerRestraintPart part) {
        List<ItemStack> list = storage.get(part);
        if (!list.isEmpty()) {
            return list.removeLast();
        }
        return ItemStack.EMPTY;
    }


    public void addByIndex(PlayerRestraintPart part, int index, ItemStack stack) {
        List<ItemStack> list = storage.get(part);
        if (list.size() >= getPartRestraintLimit(part)) return;

        if (index >= list.size()) {
            list.add(stack);
        } else {
            list.add(Math.max(0, index), stack);
        }
    }

    public ItemStack removeByIndex(PlayerRestraintPart part, int index) {
        List<ItemStack> list = storage.get(part);
        if (index >= 0 && index < list.size()) {
            return list.remove(index);
        }
        return ItemStack.EMPTY;
    }

    public void setByIndex(PlayerRestraintPart part, int index, ItemStack stack) {
        List<ItemStack> list = storage.get(part);
        if (index >= 0 && index < list.size()) {
            list.set(index, stack);
        }
    }

    public ItemStack getByIndex(PlayerRestraintPart part, int index) {
        List<ItemStack> list = storage.get(part);
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return ItemStack.EMPTY;
    }

    public void clear(PlayerRestraintPart part) {
        storage.get(part).clear();
    }


    public static final Codec<RestraintStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ItemStack.OPTIONAL_CODEC.listOf())
                    .fieldOf("restraint_stack").forGetter(c -> {
                        Map<String, List<ItemStack>> map = new HashMap<>();
                        c.storage.forEach((k, v) -> map.put(k.name(), v));
                        return map;
                    })
    ).apply(instance, map -> {
        RestraintStack stack = new RestraintStack();
        map.forEach((k, v) -> {
            try {
                PlayerRestraintPart part = PlayerRestraintPart.valueOf(k);
                int limit = getPartRestraintLimit(part);
                List<ItemStack> validatedList = v.stream()
                        .limit(limit)
                        .map(ItemStack::copy)
                        .collect(Collectors.toCollection(ArrayList::new));
                stack.storage.put(part, validatedList);
            } catch (Exception ignored) {}
        });
        return stack;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, RestraintStack> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
