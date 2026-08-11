package com.twi.restraint_dungeon.network.payload.npc.conversation;

import com.twi.restraint_dungeon.api.IRestraintApi;
import com.twi.restraint_dungeon.api.RestraintApiImpl;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.DialogueTree;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.GuiState;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.DialogueNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record RequestStartConversationPayload(int npcId) implements CustomPacketPayload {
    public static final Type<RequestStartConversationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "request_start_conversation"));

    public static final StreamCodec<FriendlyByteBuf, RequestStartConversationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestStartConversationPayload::npcId,
            RequestStartConversationPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerLevel level = player.serverLevel();
            Entity target = level.getEntity(this.npcId);

            if (target instanceof BaseNPCEntity npc && npc.canBeConversation(player, npc)) {
                DialogueTree tree = npc.getDialogueTree();
                if (tree != null) {
                    npc.startTalkingWith(player);

                    String firstNodeName = tree.getFirstNodeName(player, npc);
                    DialogueNode node = tree.resolveAndGetNode(firstNodeName, player, npc);

                    npc.setCurrentNodeId(node.getNodeId());

                    PacketDistributor.sendToPlayer(player, new OpenNodeGuiPayload(node, npc.getId(), new GuiState(new CompoundTag())));
                }
            }
        });
    }
}