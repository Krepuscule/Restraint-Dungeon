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

public class ReleaseCapability {

    private boolean isReleasing = false;
    private boolean isReleaser = false;
    private float progress = 0.0f;

    private ItemStack releasingItem = ItemStack.EMPTY;
    private PlayerRestraintPart releasingPart = PlayerRestraintPart.restraint_blindfold;
    private ItemStack releaseTool = ItemStack.EMPTY;

    @Nullable
    private UUID partnerUUID = null;

    public ReleaseCapability() {}

    public boolean isReleasing() { return isReleasing; }
    public void setReleasing(boolean active) { this.isReleasing = active; }

    public boolean isReleaser() { return isReleaser; }
    public void setReleaser(boolean releaser) { isReleaser = releaser; }

    public float getProgress() { return progress; }
    public void setProgress(float progress) { this.progress = progress; }

    public Optional<UUID> getPartnerUUID() {
        return Optional.ofNullable(partnerUUID);
    }

    public void setPartnerUUID(@Nullable UUID uuid) {
        this.partnerUUID = uuid;
    }

    public ItemStack getReleasingItem() { return releasingItem; }
    public void setReleasingItem(ItemStack stack) { this.releasingItem = stack; }

    public PlayerRestraintPart getReleasingPart() { return releasingPart; }
    public void setReleasingPart(PlayerRestraintPart part) { this.releasingPart = part; }

    public ItemStack getReleaseTool() { return releaseTool; }
    public void setReleaseTool(ItemStack tool) { this.releaseTool = tool; }

    public static final Codec<ReleaseCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_releasing").forGetter(c -> c.isReleasing),
            Codec.BOOL.fieldOf("is_releaser").forGetter(c -> c.isReleaser),
            Codec.FLOAT.fieldOf("progress").forGetter(c -> c.progress),
            UUIDUtil.CODEC.optionalFieldOf("partner").forGetter(c -> Optional.ofNullable(c.partnerUUID)),
            ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(c -> c.releasingItem),
            Codec.STRING.fieldOf("part").forGetter(c -> c.releasingPart.name()),
            ItemStack.OPTIONAL_CODEC.fieldOf("tool").forGetter(c -> c.releaseTool)
    ).apply(instance, (active, releaser, prog, partner, item, partName, tool) -> {
        ReleaseCapability cap = new ReleaseCapability();
        cap.isReleasing = active;
        cap.isReleaser = releaser;
        cap.progress = prog;
        cap.partnerUUID = partner.orElse(null);
        cap.releasingItem = item;
        cap.releasingPart = PlayerRestraintPart.valueOf(partName);
        cap.releaseTool = tool;
        return cap;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseCapability> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}