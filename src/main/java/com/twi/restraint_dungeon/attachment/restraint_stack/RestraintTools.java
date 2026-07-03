package com.twi.restraint_dungeon.attachment.restraint_stack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RestraintTools {

    private static final int DEFAULT_TOOLS_LIMIT = 5;

    public static int getDefaultToolsLimit() {
        return DEFAULT_TOOLS_LIMIT;
    }

    private final List<ItemStack> tools = new ArrayList<>();

    public RestraintTools() {

    }


    public List<ItemStack> getToolsList() {
        return this.tools;
    }


    public void addToLast(ItemStack stack) {
        if (this.tools.size() < getDefaultToolsLimit()) {
            this.tools.add(stack);
        }
    }


    public ItemStack removeFromLast() {
        if (!this.tools.isEmpty()) {
            return this.tools.removeLast();
        }
        return ItemStack.EMPTY;
    }


    public void addByIndex(int index, ItemStack stack) {
        if (this.tools.size() >= getDefaultToolsLimit()) return;

        if (index >= this.tools.size()) {
            this.tools.add(stack);
        } else {
            this.tools.add(Math.max(0, index), stack);
        }
    }


    public ItemStack removeByIndex(int index) {
        if (index >= 0 && index < this.tools.size()) {
            return this.tools.remove(index);
        }
        return ItemStack.EMPTY;
    }

    public void setByIndex(int index, ItemStack stack) {
        if (index >= 0 && index < this.tools.size()) {
            this.tools.set(index, stack);
        }
    }


    public ItemStack getByIndex(int index) {
        if (index >= 0 && index < this.tools.size()) {
            return this.tools.get(index);
        }
        return ItemStack.EMPTY;
    }


    public void clear() {
        this.tools.clear();
    }


    public boolean isFull() {
        return this.tools.size() >= getDefaultToolsLimit();
    }


    public static final Codec<RestraintTools> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf()
                    .fieldOf("tools_list").forGetter(c -> c.tools)
    ).apply(instance, incomingList -> {
        RestraintTools restraintTools = new RestraintTools();
        List<ItemStack> validatedList = incomingList.stream()
                .limit(getDefaultToolsLimit())
                .map(ItemStack::copy)
                .collect(Collectors.toCollection(ArrayList::new));
        restraintTools.tools.addAll(validatedList);
        return restraintTools;
    }));


    public static final StreamCodec<RegistryFriendlyByteBuf, RestraintTools> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
