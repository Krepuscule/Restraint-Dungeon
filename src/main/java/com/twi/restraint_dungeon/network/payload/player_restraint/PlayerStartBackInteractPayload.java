package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractTargetSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;


public record PlayerStartBackInteractPayload(BlockPos pos, InteractionHand hand) implements CustomPacketPayload {

    public static final Type<PlayerStartBackInteractPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "start_back_interact"));

    public static final StreamCodec<FriendlyByteBuf, PlayerStartBackInteractPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PlayerStartBackInteractPayload::pos,
            ByteBufCodecs.INT.map(i -> InteractionHand.values()[i], InteractionHand::ordinal), PlayerStartBackInteractPayload::hand,
            PlayerStartBackInteractPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                InteractTargetSelector.serverAttemptBlockStart(serverPlayer, this.pos, this.hand);
            }
        });
    }
}
