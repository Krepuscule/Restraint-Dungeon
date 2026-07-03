package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.client.gui.TargetInventoryMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record RequestOpenTargetInventoryPayload(UUID targetPlayerUUID) implements CustomPacketPayload {


    public static final Type<RequestOpenTargetInventoryPayload> TYPE = 
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "request_open_target_inventory"));

    public static final StreamCodec<FriendlyByteBuf, RequestOpenTargetInventoryPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestOpenTargetInventoryPayload::targetPlayerUUID,
            RequestOpenTargetInventoryPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {

                ServerPlayer targetPlayer = player.server.getPlayerList().getPlayer(this.targetPlayerUUID());
                
                if (targetPlayer != null && targetPlayer.isAlive() && player.distanceToSqr(targetPlayer) <= 4.0D) {
                    player.openMenu(new SimpleMenuProvider(
                            (containerId, playerInv, p) -> 
                                    new TargetInventoryMenu(containerId, playerInv, targetPlayer.getInventory(), player, targetPlayer),
                            Component.literal(targetPlayer.getDisplayName().getString())
                                    .append(Component.translatable("gui." + MODID + ".title.searching_inventory"))
                                    .withStyle(ChatFormatting.WHITE)
                    ));
                    targetPlayer.displayClientMessage(
                            Component.translatable("gui." + MODID + ".warn.someone_open_inventory").withStyle(ChatFormatting.DARK_RED)
                            ,true);
                }
            }
        });
    }
}