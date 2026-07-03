package com.twi.restraint_dungeon.attachment.capability.player_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PlayerRestraintOptions {
    private float blindfoldOffset;
    private float gagOffset;

    private boolean canOpenInventory;


    public PlayerRestraintOptions() {
        this.blindfoldOffset = 0.0F;
        this.gagOffset = 0.0F;
        this.canOpenInventory = true;
    }


    public PlayerRestraintOptions(float blindfoldOffset, float gagOffset,boolean canOpenInventory) {
        this.blindfoldOffset = blindfoldOffset;
        this.gagOffset = gagOffset;
        this.canOpenInventory = canOpenInventory;
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

    public boolean canOpenInventory(){return canOpenInventory;}

    public void setCanOpenInventory(boolean canOpenInventory){this.canOpenInventory = canOpenInventory;}


    public static final Codec<PlayerRestraintOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("blindfoldOffset").forGetter(PlayerRestraintOptions::getBlindfoldOffset),
                    Codec.FLOAT.fieldOf("gagOffset").forGetter(PlayerRestraintOptions::getGagOffset),
                    Codec.BOOL.fieldOf("canOpenInventory").forGetter(PlayerRestraintOptions::canOpenInventory)
            ).apply(instance, PlayerRestraintOptions::new)
    );

    public static final StreamCodec<FriendlyByteBuf, PlayerRestraintOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PlayerRestraintOptions::getBlindfoldOffset,
            ByteBufCodecs.FLOAT, PlayerRestraintOptions::getGagOffset,
            ByteBufCodecs.BOOL,PlayerRestraintOptions::canOpenInventory,
            PlayerRestraintOptions::new
    );
}