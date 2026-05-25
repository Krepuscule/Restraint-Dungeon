package com.twi.restraint_dungeon.network.payload.player_struggle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.updateStruggleProgress;

public record PlayerStruggleProgressPayload(float progress) implements CustomPacketPayload {

    public static final Type<PlayerStruggleProgressPayload> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_struggle_progress"));

    public static final StreamCodec<FriendlyByteBuf, PlayerStruggleProgressPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            PlayerStruggleProgressPayload::progress,
            PlayerStruggleProgressPayload::new
        );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理逻辑
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            updateStruggleProgress(player, this.progress);

        });
    }
}