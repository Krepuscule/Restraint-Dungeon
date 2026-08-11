package com.twi.restraint_dungeon.attachment.capability.npc_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class NPCData {

    private int skinIndex = 0;
    private Component npcName = Component.literal("NPC");
    private boolean isSlim = false;
    private float blindfoldOffset = 0.0F;
    private float gagOffset = 0.0F;

    public NPCData() {}

    public int getSkinIndex() { return skinIndex; }
    public void setSkinIndex(int skinIndex) { this.skinIndex = skinIndex; }

    // 2. 修改 Getter 和 Setter 的类型
    public Component getNPCName() { return npcName; }
    public void setNPCName(Component npcName) { this.npcName = npcName; }

    public boolean isSlim() {
        return isSlim;
    }
    public void setSlim(boolean isSlim){this.isSlim = isSlim;}

    public float getBlindfoldOffset(){return blindfoldOffset;}
    public void setBlindfoldOffset(float blindfoldOffset){this.blindfoldOffset = blindfoldOffset;}
    public float getGagOffset(){return gagOffset;}
    public void setGagOffset(float gagOffset){this.gagOffset = gagOffset;}

    public static final Codec<NPCData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("skin_index").forGetter(c -> c.skinIndex),
            // 3. 使用 ComponentSerialization.CODEC 替代 Codec.STRING
            ComponentSerialization.CODEC.fieldOf("npc_name").forGetter(c -> c.npcName),
            Codec.BOOL.fieldOf("is_slim").forGetter(c -> c.isSlim),
            Codec.FLOAT.fieldOf("blindfold_offset").forGetter(c -> c.blindfoldOffset),
            Codec.FLOAT.fieldOf("gag_offset").forGetter(c -> c.gagOffset)
    ).apply(instance, (skin, name, slim, blindfold_offset, gag_offset) -> {
        NPCData data = new NPCData();
        data.skinIndex = skin;
        data.npcName = name;
        data.isSlim = slim;
        data.blindfoldOffset = blindfold_offset;
        data.gagOffset = gag_offset;
        return data;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, NPCData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}