package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record GuiState(CompoundTag data) {
    public void putInt(String key, int value) { data.putInt(key, value); }
    public void putFloat(String key, float value) { data.putFloat(key, value); }
    public int getInt(String key, int defaultValue) { return data.contains(key) ? data.getInt(key) : defaultValue; }
    public float getFloat(String key, float defaultValue) { return data.contains(key) ? data.getFloat(key) : defaultValue; }
    
    public static GuiState read(RegistryFriendlyByteBuf buf) { return new GuiState(buf.readNbt()); }
    public void write(RegistryFriendlyByteBuf buf) { buf.writeNbt(data); }
}