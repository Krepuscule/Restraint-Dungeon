package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;

public record PositionClientRefreshPayload(int entityId) implements CustomPacketPayload {

    public static final Type<PositionClientRefreshPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "s2c_refresh_position"));

    public static final StreamCodec<FriendlyByteBuf, PositionClientRefreshPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PositionClientRefreshPayload::entityId,
            PositionClientRefreshPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().isClientSide()) {
                handleClient();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(this.entityId);
            if (entity != null) {
                entity.refreshDimensions();
            }
        }
    }
}