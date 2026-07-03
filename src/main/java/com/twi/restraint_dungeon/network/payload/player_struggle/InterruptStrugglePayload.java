package com.twi.restraint_dungeon.network.payload.player_struggle;

import com.twi.restraint_dungeon.client.hud.struggle_hud.StruggleHUDManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record InterruptStrugglePayload() implements CustomPacketPayload {
    public static final Type<InterruptStrugglePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "kidnap_interrupt"));
    public static final StreamCodec<FriendlyByteBuf, InterruptStrugglePayload> STREAM_CODEC = StreamCodec.unit(new InterruptStrugglePayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }


    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player().level().isClientSide()) {
                handleKidnapInterrupt();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void handleKidnapInterrupt() {
        StruggleHUDManager.cancel();
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("event." + MODID + ".kidnap.struggle_interrupted").withStyle(ChatFormatting.DARK_RED),
                    true
            );
        }
    }
}