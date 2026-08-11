package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.DialogueTree;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui.GuiState;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.Option;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.TradeOption;
import com.twi.restraint_dungeon.network.payload.npc.conversation.OpenNodeGuiPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class TradeNode extends DialogueNode {
    private Component text;

    public TradeNode(String nodeName, String previousNode, Component text) {
        super(nodeName, previousNode);
        this.text = text;
    }

    private TradeNode(int nodeId, String nodeName, String previousNode, Component text, List<Option> options) {
        super(nodeId, nodeName, previousNode, options);
        this.text = text;
    }

    public void setText(Component text) { this.text = text; }
    public Component getText() { return this.text; }

    @Override
    protected NodeType getType() { return NodeType.TRADE; }

    public void addTradeOption(int actionId, String defaultNextNode,
                               List<ItemStack> costs, List<Boolean> ignoreNBTs, List<ItemStack> results, Component text) {
        TradeOption template = new TradeOption(
                actionId,
                defaultNextNode,
                costs,
                ignoreNBTs,
                results,
                text
        );

        super.addOption(template);
    }

    @Override
    protected void writeAdditional(RegistryFriendlyByteBuf buf) {
        ComponentSerialization.STREAM_CODEC.encode(buf, this.text);
        buf.writeVarInt(this.options.size());
        for (Option option : this.options) {
            option.write(buf);
        }
    }

    public static TradeNode readAdditional(int nodeId, String nodeName, String previousNode, RegistryFriendlyByteBuf buf) {
        Component text = ComponentSerialization.STREAM_CODEC.decode(buf);
        int size = buf.readVarInt();
        List<Option> options = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            options.add(Option.read(buf));
        }
        return new TradeNode(nodeId, nodeName, previousNode, text, options);
    }

    @Override
    public void performDefaultAction(ServerPlayer player, BaseNPCEntity npc, Option clickedOption, GuiState guiState) {
        if (!(clickedOption instanceof TradeOption tradeOption)) return;

        boolean success = tryPerformTrade(player, tradeOption);

        if (success) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.0F);
        } else {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 0.5F, 1.0F);
        }

        DialogueTree tree = npc.getDialogueTree();
        if (tree == null) return;

        DialogueNode refreshedNode = tree.resolveAndGetNode(this.getNodeName(), player, npc);
        if (refreshedNode instanceof TradeNode tradeNode) {
            if (tradeOption.setNodeText(player, npc, success) == null) {
                tradeNode.setText(Component.translatable(success ? "gui." + MODID + ".npc.trade.success" : "gui." + MODID + ".npc.trade.fail"));
            } else {
                tradeNode.setText(tradeOption.setNodeText(player, npc, success));
            }
        }

        PacketDistributor.sendToPlayer(player, new OpenNodeGuiPayload(refreshedNode, npc.getId(), guiState));
    }

    private boolean tryPerformTrade(ServerPlayer player, TradeOption option) {
        if (player.isCreative()) {
            giveResults(player, option);
            return true;
        }

        if (!hasEnoughItems(player, option.getCosts(), option)) {
            return false;
        }

        for (int i = 0; i < option.getCosts().size(); i++) {
            removeItems(player, option.getCosts().get(i), option.isIgnoreNBT(i));
        }

        giveResults(player, option);
        return true;
    }

    private void giveResults(ServerPlayer player, TradeOption option) {
        for (ItemStack result : option.getResults()) {
            ItemStack resultCopy = result.copy();
            player.addItem(resultCopy);
        }
    }

    private boolean hasEnoughItems(Player player, List<ItemStack> costs, TradeOption option) {
        for (int i = 0; i < costs.size(); i++) {
            ItemStack cost = costs.get(i);
            boolean ignoreNBT = option.isIgnoreNBT(i);

            int countFound = 0;
            for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
                ItemStack stackInSlot = player.getInventory().getItem(j);

                boolean match = ignoreNBT ? ItemStack.isSameItem(stackInSlot, cost) : ItemStack.isSameItemSameComponents(stackInSlot, cost);

                if (match) {
                    countFound += stackInSlot.getCount();
                }
            }

            if (countFound < cost.getCount()) {
                return false;
            }
        }
        return true;
    }

    private void removeItems(Player player, ItemStack cost, boolean ignoreNBT) {
        int amountToRemove = cost.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            boolean match = ignoreNBT ? ItemStack.isSameItem(stack, cost) : ItemStack.isSameItemSameComponents(stack, cost);

            if (match) {
                int take = Math.min(stack.getCount(), amountToRemove);
                stack.shrink(take);
                amountToRemove -= take;
            }
            if (amountToRemove <= 0) break;
        }
    }
}