package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.GuiState;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.Option;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public abstract class DialogueNode {

    private int nodeId = -1;
    private final String nodeName;
    private final String previousNode;
    protected final List<Option> options = new ArrayList<>();
    private int nextOptionId = 0;

    protected DialogueNode(String nodeName, String previousNode) {
        this.nodeName = nodeName;
        this.previousNode = previousNode;
    }

    protected DialogueNode(int nodeId, String nodeName, String previousNode, List<? extends Option> options) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.previousNode = previousNode;
        this.options.addAll(options);
        for (Option opt : options) {
            this.nextOptionId = Math.max(this.nextOptionId, opt.getId() + 1);
        }
    }


    public void addOption(Option option) {
        option.setId(this.nextOptionId++);
        this.options.add(option);
    }

    public int getNextOptionId() {
        return nextOptionId++;
    }

    public List<Option> getOptions() { return this.options; }

    public final Option findOptionById(int id) {
        return options.stream().filter(opt -> opt.getId() == id).findFirst().orElse(null);
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(int nodeId) {
        if (this.nodeId != -1) {
            throw new IllegalStateException("Node ID already assigned: " + this.nodeName);
        }
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public String getPreviousNode() {
        return this.previousNode;
    }

    public void resolveNodeState(ServerPlayer player, BaseNPCEntity npc) {
        for (Option option : this.options) {
            option.resolveState(player, npc);
        }
    }

    public enum NodeType {
        CONVERSATION("conversation"),
        TRADE("trade"),
        QUEST("quest");

        private final String id;
        NodeType(String id) { this.id = id; }
        public String getTypeId() { return this.id; }

        public static NodeType getTypeById(String id) {
            for (NodeType type : values()) {
                if (type.getTypeId().equalsIgnoreCase(id)) return type;
            }
            return CONVERSATION;
        }
    }

    protected abstract NodeType getType();

    public void performDefaultAction(ServerPlayer player, BaseNPCEntity npc, Option clickedOption, GuiState guiState) {
    }

    public void handleOptionAction(ServerPlayer player, BaseNPCEntity npc, int actionId) {
        if (actionId == -2 || actionId == -1 || actionId == 0) {
            return;
        }
        this.performGeneralAction(player, npc, actionId);
    }

    protected void performGeneralAction(ServerPlayer player, BaseNPCEntity npc, int actionId) {
    }

    public final void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.getType().getTypeId());
        buf.writeInt(this.nodeId);
        buf.writeUtf(this.nodeName);
        buf.writeUtf(this.previousNode != null ? this.previousNode : "");
        this.writeAdditional(buf);
    }

    protected abstract void writeAdditional(RegistryFriendlyByteBuf buf);

    public static DialogueNode read(RegistryFriendlyByteBuf buf) {
        String typeIdStr = buf.readUtf();
        NodeType type = NodeType.getTypeById(typeIdStr);
        int nodeId = buf.readInt();
        String nodeName = buf.readUtf();
        String previousNode = buf.readUtf();

        return createNodeSnapshotInstance(type, nodeId, nodeName, previousNode, buf);
    }

    private static DialogueNode createNodeSnapshotInstance(
            NodeType type, int nodeId, String nodeName, String previousNode, RegistryFriendlyByteBuf buf
    ) {
        return switch (type) {
            case CONVERSATION -> ConversationNode.readAdditional(nodeId, nodeName, previousNode, buf);
            case TRADE -> TradeNode.readAdditional(nodeId, nodeName, previousNode, buf);
            default -> throw new IllegalArgumentException("Unknown node type: " + type);
        };
    }
}