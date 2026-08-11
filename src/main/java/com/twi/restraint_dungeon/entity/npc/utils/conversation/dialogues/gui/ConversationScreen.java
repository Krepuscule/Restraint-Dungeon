package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.ConversationNode;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.ConversationOption;
import com.twi.restraint_dungeon.network.payload.npc.conversation.ClickOptionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ConversationScreen extends DialogueScreen {
    private final ConversationNode node;
    private final LivingEntity npcEntity;

    private float scrollOffset = 0;
    private int optionScrollIndex = 0;
    private final int OPTION_DISPLAY_COUNT = 3;

    private int bgX, bgY, bgWidth, bgHeight;

    public ConversationScreen(ConversationNode node, LivingEntity npcEntity, GuiState state) {
        super(Component.literal("Conversation"), npcEntity);
        this.node = node;
        this.npcEntity = npcEntity;
        if (state != null) {
            this.scrollOffset = state.getFloat("ConversationOffset", 0.0f);
            this.optionScrollIndex = state.getInt("ConversationIndex", 0);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.bgWidth = (int) (this.width * 0.75);
        this.bgHeight = (int) (this.height * 0.75);
        this.bgX = (this.width - bgWidth) / 2;
        this.bgY = (this.height - bgHeight) / 2;
        updateButtons();
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
        gui.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0xCC000000);
        for (int i = 0; i < 3; i++) {
            gui.renderOutline(bgX + i, bgY + i, bgWidth - (i * 2), bgHeight - (i * 2), 0xFF888888);
        }

        if (this.npcEntity != null) {
            int cx = bgX + (int)(bgWidth * 0.25);
            int cy = bgY + (int)(bgHeight * 0.65);
            InventoryScreen.renderEntityInInventoryFollowsMouse(gui, cx - 60, cy - 80, cx + 50, cy + 40, 50, 0.0f, (float)mouseX, (float)mouseY, this.npcEntity);
            gui.drawCenteredString(this.font, this.npcEntity.getName(), cx - 5, cy + 40, 0xFFFFFF);
        }

        int rightBound = bgX + bgWidth - (int)(bgWidth * 0.05);
        int tLeft = bgX + (int)(bgWidth * 0.5);
        int tW = rightBound - tLeft;
        int tTop = bgY + (int)(bgHeight * 0.1);
        int tH = (int)(bgHeight * 0.40);

        int textH = this.font.wordWrapHeight(this.node.getText(), tW);
        int maxScroll = Math.max(0, textH - tH);

        scrollOffset = Mth.clamp(scrollOffset, 0, (float)maxScroll);

        if (textH > tH) {
            int barX = rightBound + 5;
            float ratio = (float)tH / textH;
            int hH = Math.max(20, (int)(tH * ratio));
            int hY = tTop + (int)((scrollOffset / (float)maxScroll) * (tH - hH));

            gui.fill(barX, tTop, barX + 2, tTop + tH, 0x44FFFFFF);
            gui.fill(barX, hY, barX + 2, hY + hH, 0xFFFFFFFF);
        }

        gui.enableScissor(tLeft, tTop, rightBound, tTop + tH);
        gui.drawWordWrap(this.font, this.node.getText(), tLeft, tTop - (int)scrollOffset, tW, 0xFFFFFF);
        gui.disableScissor();

        List<ConversationOption> visible = node.getOptions().stream()
                .filter(opt -> opt instanceof ConversationOption)
                .map(opt -> (ConversationOption) opt)
                .filter(ConversationOption::isShouldShow).toList();

        if (visible.size() > OPTION_DISPLAY_COUNT) {
            int barX = rightBound + 5;
            int barY = bgY + (int)(bgHeight * 0.55);
            int barH = (int)(bgHeight * 0.35);
            float ratio = (float)OPTION_DISPLAY_COUNT / visible.size();
            int hH = Math.max(20, (int)(barH * ratio));
            int hY = barY + (int)((optionScrollIndex / (float)(visible.size() - OPTION_DISPLAY_COUNT)) * (barH - hH));
            gui.fill(barX, barY, barX + 2, barY + barH, 0x44FFFFFF);
            gui.fill(barX, hY, barX + 2, hY + hH, 0xFFFFFFFF);
        }

        super.render(gui, mouseX, mouseY, partial);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int tLeft = bgX + (int)(bgWidth * 0.5);
        int tTop = bgY + (int)(bgHeight * 0.1);
        int tW = (int)(bgWidth * 0.40);
        int tH = (int)(bgHeight * 0.40);

        if (mx >= tLeft && mx <= tLeft + tW && my >= tTop && my <= tTop + tH) {
            int textH = this.font.wordWrapHeight(this.node.getText(), tW);
            scrollOffset = Mth.clamp(scrollOffset - (float)sy * 9, 0, (float)Math.max(0, textH - tH));
            return true;
        }

        if (mx >= bgX + (int)(bgWidth * 0.5) && my >= bgY + (bgHeight * 0.5)) {
            List<ConversationOption> visible = node.getOptions().stream()
                    .filter(opt -> opt instanceof ConversationOption)
                    .map(opt -> (ConversationOption) opt)
                    .filter(ConversationOption::isShouldShow).toList();
            if (visible.size() > OPTION_DISPLAY_COUNT) {
                if (sy > 0 && optionScrollIndex > 0) { optionScrollIndex--; updateButtons(); }
                else if (sy < 0 && optionScrollIndex < visible.size() - OPTION_DISPLAY_COUNT) { optionScrollIndex++; updateButtons(); }
            }
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    private void updateButtons() {
        this.clearWidgets();
        if (!(npcEntity instanceof BaseNPCEntity npc)) return;

        List<ConversationOption> visible = node.getOptions().stream()
                .filter(opt -> opt instanceof ConversationOption)
                .map(opt -> (ConversationOption) opt)
                .filter(ConversationOption::isShouldShow)
                .toList();

        int rightBound = bgX + bgWidth - (int)(bgWidth * 0.05);
        int tLeft = bgX + (int)(bgWidth * 0.5);
        int btnWidth = rightBound - tLeft;

        int btnHeight = (int) (bgHeight * 0.12);
        int spacing = (int) (bgHeight * 0.02);
        int startY = bgY + (int) (bgHeight * 0.55);

        for (int i = 0; i < OPTION_DISPLAY_COUNT; i++) {
            int index = optionScrollIndex + i;
            if (index < visible.size()) {
                ConversationOption opt = visible.get(index);
                Button btn = Button.builder(opt.getText(), b -> {
                    int id = opt.getActionId();
                    if (id != -2 && id != -1 && id != 0) opt.onClick(Minecraft.getInstance().player, npc);
                    PacketDistributor.sendToServer(new ClickOptionPayload(npc.getId(), opt.getId(), this.getCurrentState()));
                }).bounds(tLeft, startY + (i * (btnHeight + spacing)), btnWidth, btnHeight).build();
                btn.active = opt.isCanUse();
                this.addRenderableWidget(btn);
            }
        }
    }

    public GuiState getCurrentState() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("ConversationOffset", this.scrollOffset);
        tag.putInt("ConversationIndex", this.optionScrollIndex);
        return new GuiState(tag);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void renderBackground(@NotNull GuiGraphics g, int mx, int my, float p) {}
    @Override public void renderTransparentBackground(@NotNull GuiGraphics g) {}
}