package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintServerHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.isChangingPosition;

public record RestraintPositionPayload(UUID playerUUID, RestraintPosition prev,RestraintPosition next) implements CustomPacketPayload {

    public static final Type<RestraintPositionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_restraint_position"));

    public static final StreamCodec<FriendlyByteBuf, RestraintPositionPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RestraintPositionPayload::playerUUID,
            ByteBufCodecs.stringUtf8(32), p -> p.prev().name(),
            ByteBufCodecs.stringUtf8(32), p -> p.next().name(),
            (uuid, prev,next) -> new RestraintPositionPayload(uuid, RestraintPosition.valueOf(prev),RestraintPosition.valueOf(next))
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!isChangingPosition(player)) {
                RestraintServerHandler.startTransition(player, this.prev,this.next);
            }
        });
    }
}