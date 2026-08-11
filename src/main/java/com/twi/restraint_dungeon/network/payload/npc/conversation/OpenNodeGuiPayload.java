package com.twi.restraint_dungeon.network.payload.npc.conversation;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.ConversationScreen;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.DialogueScreen;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.GuiState;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.TradeScreen;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.ConversationNode;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.DialogueNode;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.TradeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record OpenNodeGuiPayload(DialogueNode node, int entityId, GuiState state) implements CustomPacketPayload {
    public static final Type<OpenNodeGuiPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "open_node_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenNodeGuiPayload> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                packet.node().write(buf);
                buf.writeInt(packet.entityId());
                packet.state().write(buf);
            },
            buf -> {
                DialogueNode node = DialogueNode.read(buf);
                int entityId = buf.readInt();
                GuiState state = GuiState.read(buf);
                return new OpenNodeGuiPayload(node, entityId, state);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            handleClient(context);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient(IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(this.entityId);
            if (entity instanceof BaseNPCEntity npc) {
                if (mc.screen instanceof DialogueScreen oldScreen) {
                    oldScreen.setTransitioning(true, npc);
                }

                if (this.node instanceof ConversationNode conv) {
                    mc.setScreen(new ConversationScreen(conv, npc, this.state));
                } else if (this.node instanceof TradeNode trade) {
                    mc.setScreen(new TradeScreen(trade, npc, this.state));
                }
            }
        }
    }
}