package com.twi.restraint_dungeon.network.payload.player_animator;

import com.twi.restraint_dungeon.animation.utils.AnimationLayer;
import com.twi.restraint_dungeon.animation.utils.ClientAnimationSequencePlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record PlayerAnimationSequencePayload(
        UUID playerUUID,
        List<String> animationNames,
        AnimationLayer layer
) implements CustomPacketPayload {

    public PlayerAnimationSequencePayload {
        animationNames = List.copyOf(animationNames);
    }

    public static final Type<PlayerAnimationSequencePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "animation_sync"));

    public static final StreamCodec<FriendlyByteBuf, PlayerAnimationSequencePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerAnimationSequencePayload::playerUUID,
            ByteBufCodecs.stringUtf8(256).apply(ByteBufCodecs.list()), PlayerAnimationSequencePayload::animationNames,
            ByteBufCodecs.idMapper(id -> AnimationLayer.values()[id], AnimationLayer::ordinal), PlayerAnimationSequencePayload::layer,
            PlayerAnimationSequencePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player().level().isClientSide()) {
                ClientAnimationSequencePlayer.play(
                        this.playerUUID(),
                        this.animationNames(),
                        this.layer()
                );
            }
        });
    }
}