package com.twi.restraint_dungeon.network.payload.npc.conversation;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.DialogueTree;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.GuiState;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.DialogueNode;

import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.Option;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record ClickOptionPayload(int npcId, int optionId, GuiState state) implements CustomPacketPayload {
    public static final Type<ClickOptionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "click_option"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClickOptionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClickOptionPayload::npcId,
            ByteBufCodecs.VAR_INT, ClickOptionPayload::optionId,
            ByteBufCodecs.COMPOUND_TAG.map(GuiState::new, GuiState::data),
            ClickOptionPayload::state,
            ClickOptionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerLevel level = player.serverLevel();
            Entity target = level.getEntity(this.npcId);

            if (target instanceof BaseNPCEntity npc) {
                DialogueTree tree = npc.getDialogueTree();
                DialogueNode currentNode = tree.getNodeById(npc.getCurrentNodeId());
                if (currentNode == null) return;

                Option clickedOption = currentNode.findOptionById(this.optionId);
                if (clickedOption == null) return;

                if (clickedOption.getActionId() == -2) {
                    player.connection.send(new ClientboundContainerClosePacket(0));
                    npc.stopTalking();
                    return;
                }

                if (clickedOption.getActionId() == -1) {
                    if(currentNode.getPreviousNode() != null){
                        DialogueNode prevNode = tree.resolveAndGetNode(currentNode.getPreviousNode(), player, npc);
                        npc.setCurrentNodeId(prevNode.getNodeId());
                        sendOpenGui(player, prevNode, npc, this.state);
                    }
                    return;
                }

                if (clickedOption.getActionId() == 0) {
                    currentNode.performDefaultAction(player, npc, clickedOption,this.state);
                    return;
                }

                currentNode.handleOptionAction(player, npc, clickedOption.getActionId());
                clickedOption.onClick(player, npc);

                if (clickedOption.isClosing()) {
                    player.connection.send(new ClientboundContainerClosePacket(0));
                    npc.stopTalking();
                    return;
                }


                clickedOption.resolveState(player, npc);
                String nextNodeName = clickedOption.getResolvedNextNode();
                if (nextNodeName == null || nextNodeName.isEmpty() || nextNodeName.equals(currentNode.getNodeName())) {
                    refreshGui(player, npc, currentNode, tree, this.state);
                } else {
                    DialogueNode nextNode = tree.resolveAndGetNode(nextNodeName, player, npc);
                    npc.setCurrentNodeId(nextNode.getNodeId());
                    sendOpenGui(player, nextNode, npc, this.state);
                }
            }
        });
    }

    private void refreshGui(ServerPlayer player, BaseNPCEntity npc, DialogueNode current, DialogueTree tree, GuiState state) {
        DialogueNode refreshedNode = tree.resolveAndGetNode(current.getNodeName(), player, npc);
        PacketDistributor.sendToPlayer(player, new OpenNodeGuiPayload(refreshedNode, npc.getId(), state));
    }

    private void sendOpenGui(ServerPlayer player, DialogueNode node, BaseNPCEntity npc, GuiState state) {
        PacketDistributor.sendToPlayer(player, new OpenNodeGuiPayload(node, npc.getId(), state));
    }
}