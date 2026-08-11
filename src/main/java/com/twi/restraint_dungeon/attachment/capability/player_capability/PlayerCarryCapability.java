package com.twi.restraint_dungeon.attachment.capability.player_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public class PlayerCarryCapability {
    private String carryingState;
    private UUID partnerUUID;
    private boolean isTarget;

    // 默认构造函数
    public PlayerCarryCapability() {
        this.carryingState = "NONE";
        this.partnerUUID = null;
        this.isTarget = false;
    }

    // 全参数构造函数
    public PlayerCarryCapability(String state, UUID partner, boolean isTarget) {
        this.carryingState = state;
        this.partnerUUID = partner;
        this.isTarget = isTarget;
    }

    public String getCarryingState() {
        return carryingState;
    }

    public void setCarryingState(String carryingState) {
        this.carryingState = carryingState;
    }

    public UUID getPartnerUUID() {
        return partnerUUID;
    }

    public void setPartnerUUID(UUID partnerUUID) {
        this.partnerUUID = partnerUUID;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean target) {
        isTarget = target;
    }


    public static final Codec<PlayerCarryCapability> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("carryingState").forGetter(PlayerCarryCapability::getCarryingState),
                    UUIDUtil.CODEC.optionalFieldOf("partnerUUID").forGetter(data -> Optional.ofNullable(data.getPartnerUUID())),
                    Codec.BOOL.fieldOf("isTarget").forGetter(PlayerCarryCapability::isTarget) // 添加到 Codec
            ).apply(instance, (state, uuidOpt, target) -> new PlayerCarryCapability(state, uuidOpt.orElse(null), target))
    );

    public static final StreamCodec<FriendlyByteBuf, PlayerCarryCapability> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerCarryCapability::getCarryingState,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.getPartnerUUID()),
            ByteBufCodecs.BOOL, PlayerCarryCapability::isTarget, // 添加到 StreamCodec
            (state, uuidOpt, target) -> new PlayerCarryCapability(state, uuidOpt.orElse(null), target)
    );

    public void copyFrom(PlayerCarryCapability other) {
        this.carryingState = other.getCarryingState();
        this.partnerUUID = other.getPartnerUUID();
        this.isTarget = other.isTarget();
    }
}