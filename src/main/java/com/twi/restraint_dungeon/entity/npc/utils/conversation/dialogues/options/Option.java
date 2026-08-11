package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Option {
    protected int id;
    protected final int actionId;
    protected final String defaultNextNode;

    protected boolean canUse = true;
    protected boolean shouldShow = true;
    protected boolean isClosing = false;
    protected String resolvedNextNode;

    public enum OptionType {
        CONVERSATION("conversation"), TRADE("trade"), QUEST("quest");
        private final String type;
        OptionType(String type) { this.type = type; }
        public String getType() { return this.type; }

        public static OptionType getTypeById(String id) {
            for (OptionType type : values()) {
                if (type.getType().equalsIgnoreCase(id)) return type;
            }
            return CONVERSATION;
        }
    }

    protected Option(int actionId, String defaultNextNode) {
        this.actionId = actionId;
        this.defaultNextNode = defaultNextNode;
        this.resolvedNextNode = defaultNextNode;
    }

    protected Option(int actionId, String defaultNextNode, boolean canUse, boolean shouldShow, boolean isClosing, String resolvedNextNode) {
        this.actionId = actionId;
        this.defaultNextNode = defaultNextNode;
        this.canUse = canUse;
        this.shouldShow = shouldShow;
        this.isClosing = isClosing;
        this.resolvedNextNode = resolvedNextNode;
    }

    public void setId(int id) { this.id = id; }
    public int getId() { return id; }

    public final void resolveState(ServerPlayer player, BaseNPCEntity npc) {
        this.canUse = this.setCanUse(player, npc);
        this.shouldShow = this.setShouldShow(player, npc);
        this.isClosing = this.setIsClosing(player, npc);
        this.resolvedNextNode = this.setNextNode(player, npc);
    }

    protected boolean setCanUse(ServerPlayer player, BaseNPCEntity npc) { return true; }
    protected boolean setShouldShow(ServerPlayer player, BaseNPCEntity npc) { return true; }
    protected boolean setIsClosing(ServerPlayer player, BaseNPCEntity npc) { return false; }
    protected String setNextNode(ServerPlayer player, BaseNPCEntity npc) { return this.defaultNextNode; }

    public void onClick(Player player, BaseNPCEntity npc) {}

    public int getActionId() { return this.actionId; }
    public String getDefaultNextNode() { return this.defaultNextNode; }
    public String getResolvedNextNode() { return this.resolvedNextNode; }
    public boolean isCanUse() { return this.canUse; }
    public boolean isShouldShow() { return this.shouldShow; }
    public boolean isClosing() { return this.isClosing; }

    protected void writeAdditional(RegistryFriendlyByteBuf buf) {}
    protected OptionType getType() { return OptionType.CONVERSATION; }

    public final void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.getType().getType());
        buf.writeInt(this.id);
        buf.writeInt(this.actionId);
        buf.writeUtf(this.defaultNextNode);
        buf.writeBoolean(this.canUse);
        buf.writeBoolean(this.shouldShow);
        buf.writeBoolean(this.isClosing);
        buf.writeUtf(this.resolvedNextNode);
        this.writeAdditional(buf);
    }

    public static Option read(RegistryFriendlyByteBuf buf) {
        String typeIdStr = buf.readUtf();
        OptionType type = OptionType.getTypeById(typeIdStr);
        int id = buf.readInt();
        int actionId = buf.readInt();
        String defaultNextNode = buf.readUtf();
        boolean canUse = buf.readBoolean();
        boolean shouldShow = buf.readBoolean();
        boolean isClosing = buf.readBoolean();
        String resolvedNextNode = buf.readUtf();

        Option option = createSnapshotInstance(type, actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode, buf);
        option.setId(id);
        return option;
    }

    private static Option createSnapshotInstance(
            OptionType type, int actionId, String defaultNextNode,
            boolean canUse, boolean shouldShow, boolean isClosing, String resolvedNextNode, RegistryFriendlyByteBuf buf
    ) {
        return switch (type) {
            case CONVERSATION -> ConversationOption.readAdditional(actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode, buf);
            case TRADE -> TradeOption.readAdditional(actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode, buf);
            default -> new Option(actionId, defaultNextNode, canUse, shouldShow, isClosing, resolvedNextNode) {
                @Override protected OptionType getType() { return type; }
            };
        };
    }
}