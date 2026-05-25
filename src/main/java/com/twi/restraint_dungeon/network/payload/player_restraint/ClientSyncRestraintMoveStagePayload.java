package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record ClientSyncRestraintMoveStagePayload(String moveTypeId, String stage, String direction) implements CustomPacketPayload {

    public static final Type<ClientSyncRestraintMoveStagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "restraint_move_stage"));

    public static final StreamCodec<FriendlyByteBuf, ClientSyncRestraintMoveStagePayload> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.moveTypeId);
                buf.writeUtf(packet.stage);
                buf.writeUtf(packet.direction);
            },
            buf -> new ClientSyncRestraintMoveStagePayload(buf.readUtf(), buf.readUtf(), buf.readUtf())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端接收处理器
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player().level().isClientSide()) {
                handleClient(context);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient(IPayloadContext context) {
        RestraintMoveClientManager.handleStageSyncFromServer(this.moveTypeId(), this.stage(), this.direction());
    }
}