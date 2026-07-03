package com.twi.restraint_dungeon.attachment.capability.NPCCapability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class NPCData {


    private int skinIndex = 0;
    private String npcName = "NPC";
    private boolean isSlim = false;

    public NPCData() {}


    public int getSkinIndex() { return skinIndex; }
    public void setSkinIndex(int skinIndex) { this.skinIndex = skinIndex; }

    public String getNPCName() { return npcName; }
    public void setNPCName(String npcName) { this.npcName = npcName; }

    public boolean isSlim() {
        return isSlim;
    }
    public void setSlim(boolean isSlim){this.isSlim = isSlim;}

    public static final Codec<NPCData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("skin_index").forGetter(c -> c.skinIndex),
            Codec.STRING.fieldOf("npc_name").forGetter(c -> c.npcName),
            Codec.BOOL.fieldOf("is_slim").forGetter(c -> c.isSlim) // 绑定序列化
    ).apply(instance, (skin, name, slim) -> {
        NPCData data = new NPCData();
        data.skinIndex = skin;
        data.npcName = name;
        data.isSlim = slim;
        return data;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, NPCData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}