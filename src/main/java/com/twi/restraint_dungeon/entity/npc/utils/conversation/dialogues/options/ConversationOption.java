package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;

public class ConversationOption extends Option {

    private Component text;

    public ConversationOption(Component text, int actionId, String defaultNextNode) {
        super(actionId, defaultNextNode);
        this.text = text;
    }

    public ConversationOption(Component text, int actionId, String defaultNextNode,
                              boolean canUse, boolean shouldShow, boolean isClosing, String resolvedNextNode) {
        super(actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode);
        this.text = text;
    }

    @Override
    protected OptionType getType() { return OptionType.CONVERSATION; }

    public Component getText() { return this.text; }
    public void setText(Component text) { this.text = text; }

    @Override
    protected void writeAdditional(RegistryFriendlyByteBuf buf) {
        ComponentSerialization.STREAM_CODEC.encode(buf, this.text);
    }

    public static ConversationOption readAdditional(
            int actionId, String defaultNextNode,
            boolean canUse, boolean shouldShow, boolean isClosing, String resolvedNextNode,
            RegistryFriendlyByteBuf buf
    ) {
        Component text = ComponentSerialization.STREAM_CODEC.decode(buf);
        return new ConversationOption(text, actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode);
    }

    @Override
    public void onClick(Player player, BaseNPCEntity npc) {

    }
}