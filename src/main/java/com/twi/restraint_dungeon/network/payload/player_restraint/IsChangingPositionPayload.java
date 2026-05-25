package com.twi.restraint_dungeon.network.payload.player_restraint;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.setChangingPosition;

public record IsChangingPositionPayload(UUID entityUUID, boolean isChanging) implements CustomPacketPayload {

    public static final Type<IsChangingPositionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_is_changing_position"));

    public static final StreamCodec<FriendlyByteBuf, IsChangingPositionPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, IsChangingPositionPayload::entityUUID,
            ByteBufCodecs.BOOL, IsChangingPositionPayload::isChanging,
            IsChangingPositionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            Entity targetEntity = null;

            if (level instanceof ServerLevel serverLevel) {
                targetEntity = serverLevel.getEntity(this.entityUUID);
            }

            if (targetEntity instanceof LivingEntity living) {
                setChangingPosition(living, this.isChanging);
            }
        });
    }
}