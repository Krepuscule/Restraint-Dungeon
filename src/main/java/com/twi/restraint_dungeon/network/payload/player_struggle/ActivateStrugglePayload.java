package com.twi.restraint_dungeon.network.payload.player_struggle;

import com.twi.restraint_dungeon.client.hud.struggle_hud.StruggleHUDManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getPlayerStruggleMode;

public record ActivateStrugglePayload() implements CustomPacketPayload {
    public static final Type<ActivateStrugglePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "activate_struggle"));
    public static final StreamCodec<FriendlyByteBuf, ActivateStrugglePayload> STREAM_CODEC = StreamCodec.unit(new ActivateStrugglePayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }


    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player().level().isClientSide()) {
                handleActivateStruggle();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void handleActivateStruggle() {
        Player player = Minecraft.getInstance().player;
        StruggleHUDManager.activate(getPlayerStruggleMode(player));
    }
}