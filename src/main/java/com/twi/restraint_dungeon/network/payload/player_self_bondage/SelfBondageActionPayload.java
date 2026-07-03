package com.twi.restraint_dungeon.network.payload.player_self_bondage;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.event.mod_event.self_bondage.SelfBondageServerEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record SelfBondageActionPayload(boolean start, String part) implements CustomPacketPayload {
    public static final Type<SelfBondageActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_self_bondage_action"));

    public static final StreamCodec<FriendlyByteBuf, SelfBondageActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SelfBondageActionPayload::start,
            ByteBufCodecs.STRING_UTF8, SelfBondageActionPayload::part,
            SelfBondageActionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (this.start()) {
                    SelfBondageServerEvent.startSelfBondage(serverPlayer, PlayerRestraintPart.valueOf(this.part));
                } else {
                    SelfBondageServerEvent.stopSelfBondage(serverPlayer);
                }
            }
        });
    }
}