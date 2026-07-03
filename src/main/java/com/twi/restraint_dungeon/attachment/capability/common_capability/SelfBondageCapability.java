package com.twi.restraint_dungeon.attachment.capability.common_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SelfBondageCapability {
    private boolean isActive = false;
    private float progress = 0.0f;

    public SelfBondageCapability() {}

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public float getProgress() { return progress; }
    public void setProgress(float progress) { this.progress = progress; }

    public static final Codec<SelfBondageCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_active").forGetter(c -> c.isActive),
            Codec.FLOAT.fieldOf("progress").forGetter(c -> c.progress)
    ).apply(instance, (active, prog) -> {
        SelfBondageCapability cap = new SelfBondageCapability();
        cap.isActive = active;
        cap.progress = prog;
        return cap;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelfBondageCapability> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}