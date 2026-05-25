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

public record StruggleOutOfIndexRestraintPayload(String slotType, int slotIndex) implements CustomPacketPayload {

    public static final Type<StruggleOutOfIndexRestraintPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "struggle_out_restraint_by_index"));

    public static final StreamCodec<FriendlyByteBuf, StruggleOutOfIndexRestraintPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StruggleOutOfIndexRestraintPayload::slotType,
            ByteBufCodecs.VAR_INT, StruggleOutOfIndexRestraintPayload::slotIndex,
            StruggleOutOfIndexRestraintPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final StruggleOutOfIndexRestraintPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                handleStruggleResult(player, payload.slotType(), payload.slotIndex());
            }
        });
    }
}