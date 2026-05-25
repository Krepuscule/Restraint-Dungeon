package com.twi.restraint_dungeon.attachment.capability.common_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class StruggleCapability {

    public enum StruggleMode {
        NONE("hud.restraint_dungeon.struggle_menu.none"),
        STRENGTH("hud.restraint_dungeon.struggle_menu.strength"),
        LOOSE("hud.restraint_dungeon.struggle_menu.loose"),
        UNLOCK("hud.restraint_dungeon.struggle_menu.unlock"),
        RELEASE("hud.restraint_dungeon.struggle_menu.release");

        private final String translationKey;
        StruggleMode(String translationKey) { this.translationKey = translationKey; }
        public String getTranslationKey() { return translationKey; }
    }

    private StruggleMode currentStruggleMode = StruggleMode.NONE;
    private boolean isStruggling = false;
    private float struggleProgress = 0.0f;

    public StruggleCapability() {}

    public StruggleMode getCurrentStruggleMode() { return currentStruggleMode; }
    public void setCurrentStruggleMode(StruggleMode mode) { this.currentStruggleMode = mode; }
    public boolean isStruggling() { return isStruggling; }
    public void setStruggling(boolean struggling) { isStruggling = struggling; }
    public float getStruggleProgress() { return struggleProgress; }
    public void setStruggleProgress(float progress) { this.struggleProgress = progress; }

    public static final Codec<StruggleCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("struggle_mode").forGetter(c -> c.currentStruggleMode.name()),
            Codec.BOOL.fieldOf("is_struggling").forGetter(c -> c.isStruggling),
            Codec.FLOAT.fieldOf("struggle_progress").forGetter(c -> c.struggleProgress)
    ).apply(instance, (mode, struggling, progress) -> {
        StruggleCapability cap = new StruggleCapability();
        cap.currentStruggleMode = StruggleMode.valueOf(mode);
        cap.isStruggling = struggling;
        cap.struggleProgress = progress;
        return cap;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, StruggleCapability> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
