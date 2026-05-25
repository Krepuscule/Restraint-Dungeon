package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record PlayerRestraintMovePayload(String direction) implements CustomPacketPayload {

    public static final Type<PlayerRestraintMovePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_restraint_move"));

    public static final StreamCodec<FriendlyByteBuf, PlayerRestraintMovePayload> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUtf(packet.direction),
            buf -> new PlayerRestraintMovePayload(buf.readUtf())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            RestraintMoveManager.handleServerInputPacket(player, this.direction());
        });
    }
}