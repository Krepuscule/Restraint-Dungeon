package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.setRenderOffset;

public record ServerUpdateRenderOffsetPayload(String partName, float value) implements CustomPacketPayload {

    public static final Type<ServerUpdateRenderOffsetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "c2s_update_offset"));

    public static final StreamCodec<FriendlyByteBuf, ServerUpdateRenderOffsetPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerUpdateRenderOffsetPayload::partName,
            ByteBufCodecs.FLOAT, ServerUpdateRenderOffsetPayload::value,
            ServerUpdateRenderOffsetPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 🖥️ 服务端核心接收处理器：在 record 内部直接维护
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {

                PlayerRestraintPart part = PlayerRestraintPart.valueOf(this.partName);

                boolean successfullyChanged = setRenderOffset(serverPlayer, part, this.value);

                if (successfullyChanged) {
                    ClientRenderOffsetSyncPayload syncPacket = new ClientRenderOffsetSyncPayload(
                            serverPlayer.getUUID(), this.partName, this.value
                    );
                    PacketDistributor.sendToAllPlayers(syncPacket);
                }
            }
        });
    }
}