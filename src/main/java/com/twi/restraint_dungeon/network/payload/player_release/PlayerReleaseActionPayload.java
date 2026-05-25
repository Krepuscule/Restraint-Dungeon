package com.twi.restraint_dungeon.network.payload.player_release;

import com.twi.restraint_dungeon.event.mod_event.release.PlayerReleaseServerEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record PlayerReleaseActionPayload(int targetId, boolean start) implements CustomPacketPayload {
    public static final Type<PlayerReleaseActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "release_action"));
    
    public static final StreamCodec<FriendlyByteBuf, PlayerReleaseActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerReleaseActionPayload::targetId,
            ByteBufCodecs.BOOL, PlayerReleaseActionPayload::start,
            PlayerReleaseActionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                if (this.start) {
                    Entity target = sp.level().getEntity(this.targetId);
                    if (target instanceof LivingEntity living) {
                        PlayerReleaseServerEvent.start(sp, living);
                    }
                } else {
                    PlayerReleaseServerEvent.stop(sp);
                }
            }
        });
    }
}