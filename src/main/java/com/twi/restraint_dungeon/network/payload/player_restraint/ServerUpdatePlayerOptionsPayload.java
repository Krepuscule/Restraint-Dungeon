package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;

public record ServerUpdatePlayerOptionsPayload(
        float blindfoldOffset,
        float gagOffset,
        boolean inventoryPermission
) implements CustomPacketPayload {

    public static final Type<ServerUpdatePlayerOptionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "c2s_update_options"));

    public static final StreamCodec<FriendlyByteBuf, ServerUpdatePlayerOptionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ServerUpdatePlayerOptionsPayload::blindfoldOffset,
            ByteBufCodecs.FLOAT, ServerUpdatePlayerOptionsPayload::gagOffset,
            ByteBufCodecs.BOOL, ServerUpdatePlayerOptionsPayload::inventoryPermission,
            ServerUpdatePlayerOptionsPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {

                boolean changed = false;
                if(getRenderOffset(serverPlayer,PlayerRestraintPart.restraint_blindfold) != blindfoldOffset){
                    setRenderOffset(serverPlayer,PlayerRestraintPart.restraint_blindfold,blindfoldOffset);
                    changed = true;
                }
                if(getRenderOffset(serverPlayer,PlayerRestraintPart.restraint_gag) != gagOffset){
                    setRenderOffset(serverPlayer,PlayerRestraintPart.restraint_gag,gagOffset);
                    changed = true;
                }
                if(canOpenTargetInventory(serverPlayer) != inventoryPermission){
                    setCanOpenTargetInventory(serverPlayer,inventoryPermission);
                    changed = true;
                }

                if(changed){
                    ClientPlayerOptionsSyncPayload syncPacket = new ClientPlayerOptionsSyncPayload(
                            serverPlayer.getUUID(), blindfoldOffset, gagOffset, this.inventoryPermission
                    );
                    PacketDistributor.sendToAllPlayers(syncPacket);
                }
            }
        });
    }
}