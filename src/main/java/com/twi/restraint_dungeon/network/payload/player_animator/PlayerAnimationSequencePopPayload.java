package com.twi.restraint_dungeon.network.payload.player_animator;

import com.twi.restraint_dungeon.animation.utils.AnimationLayer;
import com.twi.restraint_dungeon.animation.utils.ClientAnimationSequencePlayer;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerAnimationData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record PlayerAnimationSequencePopPayload(
        UUID playerUUID,
        AnimationLayer layer
) implements CustomPacketPayload {



    public static final Type<PlayerAnimationSequencePopPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "animation_sequence_pop_sync"));

    public static final StreamCodec<FriendlyByteBuf, PlayerAnimationSequencePopPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerAnimationSequencePopPayload::playerUUID,
            ByteBufCodecs.idMapper(id -> AnimationLayer.values()[id], AnimationLayer::ordinal), PlayerAnimationSequencePopPayload::layer,
            PlayerAnimationSequencePopPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            PlayerAnimationData attachment = player.getData(ModAttachments.PLAYER_ANIMATION.get());
            attachment.popAnimation(layer);
            player.setData(ModAttachments.PLAYER_ANIMATION,attachment);

        });
    }
}