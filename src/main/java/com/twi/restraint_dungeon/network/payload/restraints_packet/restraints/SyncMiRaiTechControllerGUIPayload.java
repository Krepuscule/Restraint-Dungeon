package com.twi.restraint_dungeon.network.payload.restraints_packet.restraints;

import com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech.MiraiTechRemoteGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;


public record SyncMiRaiTechControllerGUIPayload(UUID targetUUID, Map<String, Boolean> modules, boolean aroused, boolean deny) implements CustomPacketPayload {
    public static final Type<SyncMiRaiTechControllerGUIPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_mirai_gui"));

    public static final StreamCodec<FriendlyByteBuf, SyncMiRaiTechControllerGUIPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {

                UUIDUtil.STREAM_CODEC.encode(buf, payload.targetUUID());

                buf.writeVarInt(payload.modules().size());

                payload.modules().forEach((k, v) -> {
                    buf.writeUtf(k);
                    buf.writeBoolean(v);
                });

                buf.writeBoolean(payload.aroused());
                buf.writeBoolean(payload.deny());
            },
            buf -> {
                UUID uuid = UUIDUtil.STREAM_CODEC.decode(buf);

                int size = buf.readVarInt();
                Map<String, Boolean> modules = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    modules.put(buf.readUtf(), buf.readBoolean());
                }

                boolean aroused = buf.readBoolean();
                boolean deny = buf.readBoolean();

                return new SyncMiRaiTechControllerGUIPayload(uuid, modules, aroused, deny);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().isClientSide) {
                handleGUISync();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void handleGUISync(){
        if (Minecraft.getInstance().screen instanceof MiraiTechRemoteGUI gui) {
            gui.updateRemoteState(Minecraft.getInstance().player,this.targetUUID(), this.modules(), this.aroused(), this.deny());
        }
    }
}