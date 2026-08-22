package com.twi.restraint_dungeon.attachment.capability.player_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public class PlayerActionCapability {
    private String currentAction;
    private UUID partnerUUID;
    private boolean isTarget;

    public PlayerActionCapability() {
        this.currentAction = "NONE";
        this.partnerUUID = null;
        this.isTarget = false;
    }

    private PlayerActionCapability(String currentAction, Optional<UUID> partnerUUID, boolean isTarget) {
        this.currentAction = currentAction;
        this.partnerUUID = partnerUUID.orElse(null);
        this.isTarget = isTarget;
    }


    public void reset() {
        this.currentAction = "NONE";
        this.partnerUUID = null;
        this.isTarget = false;
    }


    public String getCurrentAction() { return currentAction; }
    public void setCurrentAction(String currentAction) { this.currentAction = currentAction; }

    public UUID getPartnerUUID() { return partnerUUID; }
    public void setPartnerUUID(UUID partnerUUID) { this.partnerUUID = partnerUUID; }

    public boolean isTarget() { return isTarget; }
    public void setTarget(boolean isTarget) { this.isTarget = isTarget; }


    public static final Codec<PlayerActionCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("current_action", "NONE").forGetter(d -> d.currentAction),
            UUIDUtil.CODEC.optionalFieldOf("partner_uuid").forGetter(d -> Optional.ofNullable(d.partnerUUID)),
            Codec.BOOL.optionalFieldOf("is_target", false).forGetter(d -> d.isTarget)
    ).apply(instance, PlayerActionCapability::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerActionCapability> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
