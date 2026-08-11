package com.twi.restraint_dungeon.network.payload.npc.conversation;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record StopTalkingPayload(int npcId) implements CustomPacketPayload {
    public static final Type<StopTalkingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "stop_talking"));

    public static final StreamCodec<FriendlyByteBuf, StopTalkingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StopTalkingPayload::npcId,
            StopTalkingPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Entity entity = player.serverLevel().getEntity(this.npcId);
            if (entity instanceof BaseNPCEntity npc) {
                if (npc.getTalkingPlayerUUID().equals(player.getUUID())) {
                    npc.stopTalking();
                }
            }
        });
    }
}