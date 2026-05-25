package com.twi.restraint_dungeon.attachment.capability.player_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class RestraintRenderOffsets {
    private float blindfoldOffset;
    private float gagOffset;


    public RestraintRenderOffsets() {
        this.blindfoldOffset = 0.0F;
        this.gagOffset = 0.0F;
    }


    public RestraintRenderOffsets(float blindfoldOffset, float gagOffset) {
        this.blindfoldOffset = blindfoldOffset;
        this.gagOffset = gagOffset;
    }

    public float getBlindfoldOffset() {
        return blindfoldOffset;
    }

    public void setBlindfoldOffset(float blindfoldOffset) {
        this.blindfoldOffset = blindfoldOffset;
    }

    public float getGagOffset() {
        return gagOffset;
    }

    public void setGagOffset(float gagOffset) {
        this.gagOffset = gagOffset;
    }


    public static final Codec<RestraintRenderOffsets> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("blindfoldOffset").forGetter(RestraintRenderOffsets::getBlindfoldOffset),
                    Codec.FLOAT.fieldOf("gagOffset").forGetter(RestraintRenderOffsets::getGagOffset)
            ).apply(instance, RestraintRenderOffsets::new)
    );

    public static final StreamCodec<FriendlyByteBuf, RestraintRenderOffsets> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, RestraintRenderOffsets::getBlindfoldOffset,
            ByteBufCodecs.FLOAT, RestraintRenderOffsets::getGagOffset,
            RestraintRenderOffsets::new
    );
}