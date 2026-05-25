package com.twi.restraint_dungeon.network.payload.player_struggle;

import com.twi.restraint_dungeon.event.custom_event.PlayerStrugglingEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getEntityTargetPart;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getPlayerStruggleMode;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.updateIsStruggling;

public record PlayerIsStrugglingPayload(boolean isStruggling, boolean isSuccess) implements CustomPacketPayload {
    public static final Type<PlayerIsStrugglingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_is_struggling"));

    public static final StreamCodec<FriendlyByteBuf, PlayerIsStrugglingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlayerIsStrugglingPayload::isStruggling,
            ByteBufCodecs.BOOL, PlayerIsStrugglingPayload::isSuccess,
            PlayerIsStrugglingPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            updateIsStruggling(player, this.isStruggling);
            
            if(isStruggling){
                NeoForge.EVENT_BUS.post(new PlayerStrugglingEvent.Begin(
                        player,
                        getEntityTargetPart(player),
                        getPlayerStruggleMode(player),
                        getRestraintPosition(player),
                        isSuccess
                ));
            }else{
                NeoForge.EVENT_BUS.post(new PlayerStrugglingEvent.End(
                        player,
                        getEntityTargetPart(player),
                        getPlayerStruggleMode(player),
                        getRestraintPosition(player),
                        isSuccess
                ));
            }
        });
    }
}