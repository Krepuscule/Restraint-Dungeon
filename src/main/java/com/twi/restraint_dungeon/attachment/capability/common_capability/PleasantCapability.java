package com.twi.restraint_dungeon.attachment.capability.common_capability;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PleasantCapability {
    private int thrillLevel = 0;
    private double pleasantValue = 0.0;
    private float physicalStrength = 100.0f;

    public PleasantCapability() {}

    public int getThrillLevel() { return thrillLevel; }
    public void setThrillLevel(int val) { this.thrillLevel = val; }
    public double getPleasantValue() { return pleasantValue; }
    public void setPleasantValue(double val) { this.pleasantValue = val; }
    public float getPhysicalStrength() { return physicalStrength; }
    public void setPhysicalStrength(float val) { this.physicalStrength = val; }

    public static final Codec<PleasantCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("thrill_level").forGetter(c -> c.thrillLevel),
            Codec.DOUBLE.fieldOf("pleasant_value").forGetter(c -> c.pleasantValue),
            Codec.FLOAT.fieldOf("physical_strength").forGetter(c -> c.physicalStrength)
    ).apply(instance, (thrill, pleasant, strength) -> {
        PleasantCapability cap = new PleasantCapability();
        cap.thrillLevel = thrill;
        cap.pleasantValue = pleasant;
        cap.physicalStrength = strength;
        return cap;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, PleasantCapability> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
