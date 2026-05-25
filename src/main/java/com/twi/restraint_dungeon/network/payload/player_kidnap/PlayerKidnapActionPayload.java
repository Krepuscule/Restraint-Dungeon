package com.twi.restraint_dungeon.network.payload.player_kidnap;

import com.twi.restraint_dungeon.event.mod_event.kidnap.PlayerKidnapServerEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record PlayerKidnapActionPayload(int entityId, boolean start) implements CustomPacketPayload {
    public static final Type<PlayerKidnapActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_kidnap_action"));

    public static final StreamCodec<FriendlyByteBuf, PlayerKidnapActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerKidnapActionPayload::entityId,
            ByteBufCodecs.BOOL, PlayerKidnapActionPayload::start,
            PlayerKidnapActionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                if (this.start()) {
                    Entity target = serverPlayer.level().getEntity(this.entityId());
                    if (target instanceof LivingEntity living) {
                        PlayerKidnapServerEvent.startKidnapping(serverPlayer, living);
                    }
                } else {
                    PlayerKidnapServerEvent.stopKidnapping(serverPlayer);
                }
            }
        });
    }
}