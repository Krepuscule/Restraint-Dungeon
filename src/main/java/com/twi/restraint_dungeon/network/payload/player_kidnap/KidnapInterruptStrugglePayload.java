package com.twi.restraint_dungeon.network.payload.player_kidnap;

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

public record KidnapInterruptStrugglePayload() implements CustomPacketPayload {
    public static final Type<KidnapInterruptStrugglePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "kidnap_interrupt"));
    public static final StreamCodec<FriendlyByteBuf, KidnapInterruptStrugglePayload> STREAM_CODEC = StreamCodec.unit(new KidnapInterruptStrugglePayload());

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
                    Component.translatable("event.restraint_dungeon.kidnap.struggle_interrupted").withStyle(ChatFormatting.DARK_RED),
                    true
            );
        }
    }
}