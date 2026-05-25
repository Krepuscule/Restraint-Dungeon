package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record RestraintMoveSyncBodyYawPayload(float newYaw) implements CustomPacketPayload {

    public static final Type<RestraintMoveSyncBodyYawPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MODID, "sync_restraint_move_body_yaw")
    );

    public static final StreamCodec<FriendlyByteBuf, RestraintMoveSyncBodyYawPayload> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat),
            RestraintMoveSyncBodyYawPayload::newYaw,
            RestraintMoveSyncBodyYawPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            RestraintMoveManager.handleClientSyncYawRequest(context.player(), this.newYaw());
        });
    }
}