package com.twi.restraint_dungeon.network.payload.player_struggle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.canBeReleaseBySelf;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.handleStruggleResult;

public record PlayerReleaseSelfPayload(String part) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerReleaseSelfPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "release_self_restraint"));


    public static final StreamCodec<FriendlyByteBuf, PlayerReleaseSelfPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerReleaseSelfPayload::part,
            PlayerReleaseSelfPayload::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理逻辑
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {

                if(canBeReleaseBySelf(player) != null){
                    player.displayClientMessage(Objects.requireNonNull(canBeReleaseBySelf(player)),true);
                    return;
                }

                handleStruggleResult(player, this.part);
            }
        });
    }
}
