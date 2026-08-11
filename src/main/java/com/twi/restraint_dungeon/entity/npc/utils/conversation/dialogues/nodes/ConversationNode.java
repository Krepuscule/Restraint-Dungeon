package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.DialogueTree;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.GuiState;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.Option;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.ConversationOption;
import com.twi.restraint_dungeon.network.payload.npc.conversation.OpenNodeGuiPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ConversationNode extends DialogueNode {
    private Component text;

    public ConversationNode(String nodeName, String previousNode, Component text) {
        super(nodeName, previousNode);
        this.text = text;
    }

    private ConversationNode(int nodeId, String nodeName, String previousNode, Component text, List<Option> options) {
        super(nodeId, nodeName, previousNode, options);
        this.text = text;
    }

    public void setText(Component text) { this.text = text; }
    public Component getText() { return this.text; }

    @Override
    protected NodeType getType() { return NodeType.CONVERSATION; }


    public void addConversationOption(Component text, int actionId, String defaultNextNode) {
        ConversationOption newOption = new ConversationOption(
                text,
                actionId,
                defaultNextNode
        );

        super.addOption(newOption);
    }

    @Override
    protected void writeAdditional(RegistryFriendlyByteBuf buf) {
        ComponentSerialization.STREAM_CODEC.encode(buf, this.text);
        buf.writeVarInt(this.options.size());
        for (Option option : this.options) {
            option.write(buf);
        }
    }

    public static ConversationNode readAdditional(int nodeId, String nodeName, String previousNode, RegistryFriendlyByteBuf buf) {
        Component text = ComponentSerialization.STREAM_CODEC.decode(buf);
        int size = buf.readVarInt();
        List<Option> options = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            options.add(Option.read(buf));
        }
        return new ConversationNode(nodeId, nodeName, previousNode, text, options);
    }

    @Override
    public void performDefaultAction(ServerPlayer player, BaseNPCEntity npc, Option clickedOption,GuiState guiState) {
        DialogueTree tree = npc.getDialogueTree();
        if (tree == null) return;

        String targetNodeName = clickedOption.getDefaultNextNode();
        if (targetNodeName == null || targetNodeName.isEmpty()) return;

        DialogueNode nextNode = tree.resolveAndGetNode(targetNodeName, player, npc);
        if (nextNode == null) return;

        npc.setCurrentNodeId(nextNode.getNodeId());

        GuiState newState = new GuiState(new CompoundTag());

        PacketDistributor.sendToPlayer(player, new OpenNodeGuiPayload(nextNode, npc.getId(), newState));
    }

}