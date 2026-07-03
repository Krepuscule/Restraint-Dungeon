package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;

public record ClientPlayerOptionsSyncPayload(
        UUID targetUUID,
        float blindfoldOffset,
        float gagOffset,
        boolean inventoryPermission
) implements CustomPacketPayload {

    public static final Type<ClientPlayerOptionsSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "s2c_sync_offset"));

    public static final StreamCodec<FriendlyByteBuf, ClientPlayerOptionsSyncPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientPlayerOptionsSyncPayload::targetUUID,
            ByteBufCodecs.FLOAT, ClientPlayerOptionsSyncPayload::blindfoldOffset,
            ByteBufCodecs.FLOAT, ClientPlayerOptionsSyncPayload::gagOffset,
            ByteBufCodecs.BOOL, ClientPlayerOptionsSyncPayload::inventoryPermission,
            ClientPlayerOptionsSyncPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player().level().isClientSide()) {
                handleClient(context);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient(IPayloadContext context) {
        Player targetPlayer = context.player().level().getPlayerByUUID(this.targetUUID);
        if (targetPlayer != null) {
            if(getRenderOffset(targetPlayer,PlayerRestraintPart.restraint_blindfold) != blindfoldOffset){
                setRenderOffset(targetPlayer,PlayerRestraintPart.restraint_blindfold,blindfoldOffset);
            }
            if(getRenderOffset(targetPlayer,PlayerRestraintPart.restraint_gag) != gagOffset){
                setRenderOffset(targetPlayer,PlayerRestraintPart.restraint_gag,gagOffset);
            }
            if(canOpenTargetInventory(targetPlayer) != inventoryPermission){
                setCanOpenTargetInventory(targetPlayer,inventoryPermission);
            }

        }
    }
}