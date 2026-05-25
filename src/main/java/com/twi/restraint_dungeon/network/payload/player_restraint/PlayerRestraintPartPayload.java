package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.setTargetPart;

public record PlayerRestraintPartPayload(String partName) implements CustomPacketPayload {

    public static final Type<PlayerRestraintPartPayload> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_restraint_part"));

    public static final StreamCodec<FriendlyByteBuf, PlayerRestraintPartPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PlayerRestraintPartPayload::partName,
            PlayerRestraintPartPayload::new
        );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PlayerRestraintPartPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            setTargetPart(player,
                    PlayerRestraintPart.valueOf(payload.partName()));
        });
    }
}