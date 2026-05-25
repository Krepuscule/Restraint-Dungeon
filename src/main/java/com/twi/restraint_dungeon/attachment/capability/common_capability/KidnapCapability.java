package com.twi.restraint_dungeon.attachment.capability.common_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class KidnapCapability {

    private boolean isKidnapping = false;
    private boolean isKidnapper = false;
    private float progress = 0.0f;

    @Nullable
    private UUID partnerUUID = null;

    private ItemStack kidnappingItem = ItemStack.EMPTY;
    private PlayerRestraintPart kidnapPart = PlayerRestraintPart.restraint_blindfold;

    public KidnapCapability() {}


    public boolean isKidnapping() { return isKidnapping; }
    public void setKidnapping(boolean active) { this.isKidnapping = active; }

    public boolean isKidnapper() { return isKidnapper; }
    public void setKidnapper(boolean kidnapper) { isKidnapper = kidnapper; }

    public float getProgress() { return progress; }
    public void setProgress(float progress) { this.progress = progress; }

    public Optional<UUID> getPartnerUUID() {
        return Optional.ofNullable(partnerUUID);
    }

    public void setPartnerUUID(@Nullable UUID uuid) {
        this.partnerUUID = uuid;
    }

    public ItemStack getKidnappingItem() { return kidnappingItem; }
    public void setKidnappingItem(ItemStack item) { this.kidnappingItem = item; }

    public PlayerRestraintPart getKidnapPart() { return kidnapPart; }
    public void setKidnapPart(PlayerRestraintPart part) { this.kidnapPart = part; }

    public static final Codec<KidnapCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_active").forGetter(c -> c.isKidnapping),
            Codec.BOOL.fieldOf("is_kidnapper").forGetter(c -> c.isKidnapper),
            Codec.FLOAT.fieldOf("progress").forGetter(c -> c.progress),
            UUIDUtil.CODEC.optionalFieldOf("partner").forGetter(c -> Optional.ofNullable(c.partnerUUID)),
            ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(c -> c.kidnappingItem),
            Codec.STRING.fieldOf("part").forGetter(c -> c.kidnapPart.name())
    ).apply(instance, (active, kidnapper, prog, partner, item, partName) -> {
        KidnapCapability cap = new KidnapCapability();
        cap.isKidnapping = active;
        cap.isKidnapper = kidnapper;
        cap.progress = prog;
        cap.partnerUUID = partner.orElse(null);
        cap.kidnappingItem = item;
        cap.kidnapPart = PlayerRestraintPart.valueOf(partName);
        return cap;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, KidnapCapability> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
