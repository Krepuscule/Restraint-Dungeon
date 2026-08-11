package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TradeOption extends Option {

    private Component text;
    private List<ItemStack> costs;
    private List<ItemStack> results;
    private List<Boolean> ignoreNBTs;

    public TradeOption(int actionId, String defaultNextNode,
                       List<ItemStack> costs, List<Boolean> ignoreNBTs, List<ItemStack> results, Component text) {
        super(actionId, defaultNextNode);
        this.costs = NonNullList.copyOf(costs);
        this.ignoreNBTs = new ArrayList<>(ignoreNBTs);
        this.results = NonNullList.copyOf(results);
        this.text = text;
    }

    public TradeOption(int actionId, String defaultNextNode,
                       boolean canUse, boolean shouldShow, boolean isClosing, String resolvedNextNode,
                       List<ItemStack> costs, List<Boolean> ignoreNBTs, List<ItemStack> results, Component text) {
        super(actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode);
        this.costs = NonNullList.copyOf(costs);
        this.ignoreNBTs = new ArrayList<>(ignoreNBTs);
        this.results = NonNullList.copyOf(results);
        this.text = text;
    }

    @Override
    protected OptionType getType() { return OptionType.TRADE; }

    public Component getText() { return text; }
    public void setText(Component text) { this.text = text; }

    public Component setNodeText(Player player,BaseNPCEntity npc,boolean success){
        return null;
    }

    public List<ItemStack> getCosts() { return this.costs; }
    public List<ItemStack> getResults() { return this.results; }

    public boolean isIgnoreNBT(int index) {
        if (ignoreNBTs == null || index < 0 || index >= ignoreNBTs.size()) return false;
        return ignoreNBTs.get(index);
    }

    public void setCosts(List<ItemStack> costs) { this.costs = NonNullList.copyOf(costs); }
    public void setResults(List<ItemStack> results) { this.results = NonNullList.copyOf(results); }
    public void setIgnoreNBTs(List<Boolean> ignoreNBTs) { this.ignoreNBTs = new ArrayList<>(ignoreNBTs); }

    @Override
    public void onClick(Player player, BaseNPCEntity npc) {
        // 自定义逻辑
    }

    @Override
    protected void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.ignoreNBTs.size());
        for (boolean b : this.ignoreNBTs) buf.writeBoolean(b);

        buf.writeBoolean(this.text != null);
        if (this.text != null) ComponentSerialization.STREAM_CODEC.encode(buf, this.text);

        buf.writeVarInt(this.costs.size());
        for (ItemStack stack : this.costs) ItemStack.STREAM_CODEC.encode(buf, stack);
        buf.writeVarInt(this.results.size());
        for (ItemStack stack : this.results) ItemStack.STREAM_CODEC.encode(buf, stack);
    }

    public static TradeOption readAdditional(
            int actionId, String defaultNextNode,
            boolean canUse, boolean shouldShow, boolean isClosing, String resolvedNextNode,
            RegistryFriendlyByteBuf buf
    ) {
        int nbtSize = buf.readVarInt();
        List<Boolean> ignoreNBTs = new ArrayList<>(nbtSize);
        for (int i = 0; i < nbtSize; i++) ignoreNBTs.add(buf.readBoolean());

        boolean hasText = buf.readBoolean();
        Component text = hasText ? ComponentSerialization.STREAM_CODEC.decode(buf) : null;

        int costSize = buf.readVarInt();
        List<ItemStack> costs = new ArrayList<>(costSize);
        for (int i = 0; i < costSize; i++) costs.add(ItemStack.STREAM_CODEC.decode(buf));

        int resultSize = buf.readVarInt();
        List<ItemStack> results = new ArrayList<>(resultSize);
        for (int i = 0; i < resultSize; i++) results.add(ItemStack.STREAM_CODEC.decode(buf));

        return new TradeOption(actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode, costs, ignoreNBTs, results, text);
    }
}