package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractingProgressManager;
import net.minecraft.client.Minecraft;
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

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record PlayerInteractProgressPayload(float progress) implements CustomPacketPayload {
    public static final Type<PlayerInteractProgressPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_interact_progress"));
    public static final StreamCodec<FriendlyByteBuf, PlayerInteractProgressPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            PlayerInteractProgressPayload::progress,
            PlayerInteractProgressPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }


    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            handle();
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void handle() {
        Player player = Minecraft.getInstance().player;
        InteractingProgressManager.currentProgress = progress;
    }
}