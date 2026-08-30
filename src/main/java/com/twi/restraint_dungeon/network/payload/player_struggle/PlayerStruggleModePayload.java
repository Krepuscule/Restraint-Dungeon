package com.twi.restraint_dungeon.network.payload.player_struggle;

import com.twi.restraint_dungeon.RestraintDungeon;
import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.canBeStruggle;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.updateStruggleMode;

public record PlayerStruggleModePayload(String modeName) implements CustomPacketPayload {

    public static final Type<PlayerStruggleModePayload> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_struggle_mode"));

    public static final StreamCodec<FriendlyByteBuf, PlayerStruggleModePayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PlayerStruggleModePayload::modeName,
            PlayerStruggleModePayload::new
        );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if(!(player instanceof ServerPlayer serverPlayer)) return;
            if(!modeName.equals(StruggleMode.NONE.name()) && canBeStruggle(player) != null){
                player.displayClientMessage(Objects.requireNonNull(canBeStruggle(player)),true);
                return;
            }
            try {
                StruggleMode mode = StruggleMode.valueOf(this.modeName());
                updateStruggleMode(player, mode);
                if(!modeName.equals(StruggleMode.NONE.name())){
                    PacketDistributor.sendToPlayer(serverPlayer,new ActivateStrugglePayload());
                }

            } catch (IllegalArgumentException e) {
                RestraintDungeon.LOGGER.error("Received invalid struggle mode from player {}: {}",
                        player.getName().getString(), this.modeName());
            }
        });
    }
}