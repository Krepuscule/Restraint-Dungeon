package com.twi.restraint_dungeon.network.payload.player_struggle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.handleStruggleResult;

public record StruggleOutOfRestraintPayload(String slotType) implements CustomPacketPayload {

    public static final Type<StruggleOutOfRestraintPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "struggle_out_restraint"));


    public static final StreamCodec<FriendlyByteBuf, StruggleOutOfRestraintPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StruggleOutOfRestraintPayload::slotType,
            StruggleOutOfRestraintPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理逻辑
     */
    public static void handle(final StruggleOutOfRestraintPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                handleStruggleResult(player, payload.slotType());
            }
        });
    }
}