package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.DialogueNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DialogueTree {

    private final Map<String, DialogueNode> nodesByName = new HashMap<>();
    private final Map<Integer, DialogueNode> nodesById = new HashMap<>();

    private final AtomicInteger idCounter = new AtomicInteger(0);


    public void registerNode(DialogueNode node) {
        int id = idCounter.getAndIncrement();
        node.setNodeId(id);
        
        nodesByName.put(node.getNodeName(), node);
        nodesById.put(id, node);
    }

    public String getFirstNodeName(ServerPlayer player, BaseNPCEntity npc) {
        return "main";
    }


    public DialogueNode getNodeByName(String nodeName) {
        return nodesByName.get(nodeName);
    }

    public DialogueNode getNodeById(int nodeId) {
        return nodesById.get(nodeId);
    }


    public DialogueNode resolveAndGetNode(String nodeName, ServerPlayer player, BaseNPCEntity npc) {
        DialogueNode node = nodesByName.get(nodeName);
        if (node != null) {
            node.resolveNodeState(player, npc);
        }
        return node;
    }
}