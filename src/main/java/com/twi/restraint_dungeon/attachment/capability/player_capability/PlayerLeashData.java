package com.twi.restraint_dungeon.attachment.capability.player_capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Leashable;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;


public class PlayerLeashData {

    private @Nullable UUID leasherUUID;
    private @Nullable BlockPos fencePos;


    public PlayerLeashData() {
        this.leasherUUID = null;
        this.fencePos = null;
    }

    private PlayerLeashData(Optional<UUID> leasherUUID, Optional<BlockPos> fencePos) {
        this.leasherUUID = leasherUUID.orElse(null);
        this.fencePos = fencePos.orElse(null);
    }


    public void reset() {
        this.leasherUUID = null;
        this.fencePos = null;
    }

    public boolean hasLeash() {
        return this.leasherUUID != null || this.fencePos != null;
    }


    public @Nullable UUID getLeasherUUID() { return leasherUUID; }
    public void setLeasherUUID(@Nullable UUID leasherUUID) { this.leasherUUID = leasherUUID; }

    public @Nullable BlockPos getFencePos() { return fencePos; }
    public void setFencePos(@Nullable BlockPos fencePos) { this.fencePos = fencePos; }


    public static final Codec<PlayerLeashData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("leasher_uuid").forGetter(d -> Optional.ofNullable(d.leasherUUID)),
            BlockPos.CODEC.optionalFieldOf("fence_pos").forGetter(d -> Optional.ofNullable(d.fencePos))
    ).apply(instance, PlayerLeashData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerLeashData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}