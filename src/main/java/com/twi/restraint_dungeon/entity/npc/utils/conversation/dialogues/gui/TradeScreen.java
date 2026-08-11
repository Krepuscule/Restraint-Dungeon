package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.TradeNode;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.TradeOption;
import com.twi.restraint_dungeon.network.payload.npc.conversation.ClickOptionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class TradeScreen extends DialogueScreen {
    private final TradeNode node;
    private final LivingEntity npcEntity;

    private float textScrollOffset = 0;
    private int optionScrollIndex = 0;
    // 修改：将每页容纳数量调整为 5
    private final int MAX_OPTIONS_PER_PAGE = 5;

    private int bgX, bgY, bgWidth, bgHeight;

    public TradeScreen(TradeNode node, LivingEntity npcEntity, GuiState state) {
        super(Component.literal("Trade"), npcEntity);
        this.node = node;
        this.npcEntity = npcEntity;
        if (state != null) {
            this.textScrollOffset = state.getFloat("textOffset", 0.0f);
            this.optionScrollIndex = state.getInt("optionIndex", 0);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.bgWidth = (int) (this.width * 0.85);
        this.bgHeight = (int) (this.height * 0.85);
        this.bgX = (this.width - bgWidth) / 2;
        this.bgY = (this.height - bgHeight) / 2;
        updateTradeButtons();
    }

    private void updateTradeButtons() {
        this.clearWidgets();
        if (!(npcEntity instanceof BaseNPCEntity npc)) return;

        List<TradeOption> options = node.getOptions().stream()
                .filter(opt -> opt instanceof TradeOption)
                .map(opt -> (TradeOption) opt)
                .filter(TradeOption::isShouldShow).toList();

        int rightBound = bgX + bgWidth - (int)(bgWidth * 0.05);
        int btnX = bgX + (int)(bgWidth * 0.5);
        int btnWidth = rightBound - btnX;

        // 布局区域：从 0.35 开始到 0.95 结束
        int startY = bgY + (int) (bgHeight * 0.35);
        int endY = bgY + (int) (bgHeight * 0.95);
        int totalAreaHeight = endY - startY;

        // 修改：减小间距至 4px，让按钮更紧凑
        int spacing = 4;
        // 计算按钮高度：总高度减去所有间距，除以按钮数量
        int btnHeight = (totalAreaHeight - ((MAX_OPTIONS_PER_PAGE - 1) * spacing)) / MAX_OPTIONS_PER_PAGE;

        for (int i = 0; i < MAX_OPTIONS_PER_PAGE; i++) {
            int index = optionScrollIndex + i;
            if (index < options.size()) {
                TradeOption opt = options.get(index);
                // 按钮 Y 坐标根据计算出的高度和较小的间距排布
                int yPos = startY + (i * (btnHeight + spacing));
                this.addRenderableWidget(new TradeButton(btnX, yPos, btnWidth, btnHeight, opt, b -> {
                    int id = opt.getActionId();
                    if (id != -2 && id != -1 && id != 0) opt.onClick(Minecraft.getInstance().player, npc);
                    PacketDistributor.sendToServer(new ClickOptionPayload(this.npcEntity.getId(), opt.getId(), this.getCurrentState()));
                }));
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
        gui.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0xCC000000);
        for (int i = 0; i < 3; i++) {
            gui.renderOutline(bgX + i, bgY + i, bgWidth - (i * 2), bgHeight - (i * 2), 0xFF888888);
        }

        // NPC 渲染
        int cx = bgX + (int)(bgWidth * 0.25);
        int cy = bgY + (int)(bgHeight * 0.5) + 20;
        InventoryScreen.renderEntityInInventoryFollowsMouse(gui, cx - 60, cy - 75, cx + 60, cy + 45, 50, 0.0F, (float)mouseX, (float)mouseY, this.npcEntity);
        gui.drawCenteredString(this.font, this.npcEntity.getName(), cx, cy + 50, 0xFFFFFF);

        // 文本区域
        int rightBound = bgX + bgWidth - (int)(bgWidth * 0.05);
        int tLeft = bgX + (int)(bgWidth * 0.5);
        int tTop = bgY + (int)(bgHeight * 0.1);
        int tW = rightBound - tLeft;
        int tH = (int)(bgHeight * 0.22);

        int textH = this.font.wordWrapHeight(node.getText(), tW);
        if (textH > tH) {
            int barX = rightBound + 5;
            float ratio = (float)tH / textH;
            int handleH = Math.max(20, (int)(tH * ratio));
            int maxScroll = textH - tH;
            float currentOffset = Mth.clamp(textScrollOffset, 0, (float)maxScroll);
            int handleY = tTop + (int)((currentOffset / maxScroll) * (tH - handleH));
            gui.fill(barX, tTop, barX + 2, tTop + tH, 0x44FFFFFF);
            gui.fill(barX, handleY, barX + 2, handleY + handleH, 0xFFFFFFFF);
        }

        gui.enableScissor(tLeft, tTop, rightBound, tTop + tH);
        gui.drawWordWrap(this.font, node.getText(), tLeft, tTop - (int)Math.min(textScrollOffset, Math.max(0, textH - tH)), tW, 0xFFFFFF);
        gui.disableScissor();

        // 按钮部分滚动条：固定高度保持与按钮区域一致
        List<TradeOption> options = node.getOptions().stream().filter(opt -> opt instanceof TradeOption).map(o -> (TradeOption) o).filter(TradeOption::isShouldShow).toList();
        renderScrollBar(gui, rightBound + 5, bgY + (int)(bgHeight * 0.35), (int)(bgHeight * 0.60), optionScrollIndex, options.size(), true);

        super.render(gui, mouseX, mouseY, partial);
    }

    private void renderScrollBar(GuiGraphics gui, int x, int y, int h, float offset, float totalSize, boolean isIndex) {
        float visibleCount = isIndex ? MAX_OPTIONS_PER_PAGE : (float)h;
        if (totalSize <= visibleCount) return;
        float ratio = visibleCount / totalSize;
        int handleH = Math.max(20, (int)(h * ratio));
        handleH = Math.min(handleH, h);
        int maxScroll = (int)(totalSize - visibleCount);
        int handleY = y + (int)((Mth.clamp(offset, 0, maxScroll) / (float)maxScroll) * (h - handleH));
        gui.fill(x, y, x + 2, y + h, 0x44FFFFFF);
        gui.fill(x, handleY, x + 2, handleY + handleH, 0xFFFFFFFF);
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int tLeft = bgX + (int)(bgWidth * 0.5), tTop = bgY + (int)(bgHeight * 0.1), tH = (int)(bgHeight * 0.2);
        if (mx >= tLeft && my >= tTop && my <= tTop + tH) {
            int textH = this.font.wordWrapHeight(node.getText(), (int)(bgWidth * 0.35));
            textScrollOffset = Mth.clamp(textScrollOffset - (float)sy * 15, 0, Math.max(0, textH - tH));
            return true;
        }
        if (mx >= tLeft && my >= bgY + (bgHeight * 0.35)) {
            List<TradeOption> visible = node.getOptions().stream().filter(opt -> opt instanceof TradeOption).map(o -> (TradeOption) o).filter(TradeOption::isShouldShow).toList();
            if (sy > 0 && optionScrollIndex > 0) { optionScrollIndex--; updateTradeButtons(); }
            else if (sy < 0 && optionScrollIndex < Math.max(0, visible.size() - MAX_OPTIONS_PER_PAGE)) { optionScrollIndex++; updateTradeButtons(); }
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    private class TradeButton extends AbstractButton {
        private final TradeOption option;
        private final OnPress onPress;

        public TradeButton(int x, int y, int w, int h, TradeOption opt, OnPress onPress) {
            super(x, y, w, h, Component.empty());
            this.option = opt;
            this.onPress = onPress;
            this.active = opt.isCanUse();
        }

        @Override public void onPress() { if (active) this.onPress.onPress(this); }

        @Override
        public void renderWidget(GuiGraphics gui, int mx, int my, float pt) {
            boolean isHovered = active && mx >= getX() && mx <= getX() + width && my >= getY() && my <= getY() + height;

            int bgColor = active ? 0xFF555555 : 0xFF222222;
            gui.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

            if (active) {
                gui.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF999999);
                gui.fill(getX(), getY(), getX() + 1, getY() + height, 0xFF999999);

                gui.fill(getX() + width - 1, getY(), getX() + width, getY() + height, 0xFF222222);
                gui.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF222222);
            } else {
                gui.renderOutline(getX(), getY(), width, height, 0xFF444444);
            }

            if (isHovered) {
                gui.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
            }

            renderContents(gui, mx, my);
        }

        private void renderContents(GuiGraphics gui, int mx, int my) {
            if (option.getText() != null) {
                int centerX = getX() + width / 2;
                int centerY = getY() + height / 2;
                gui.drawCenteredString(font, option.getText(), centerX, centerY - (font.lineHeight / 2), 0xFFFFFF);
                return;
            }

            int iconY = getY() + (height - 16) / 2;
            ItemStack hoveredStack = null;
            Integer hoveredCostIndex = null;

            int leftStartX = getX() + 8;
            for (int i = 0; i < option.getCosts().size(); i++) {
                ItemStack stack = option.getCosts().get(i);
                gui.renderItem(stack, leftStartX, iconY);
                gui.renderItemDecorations(font, stack, leftStartX, iconY);

                if (mx >= leftStartX && mx <= leftStartX + 16 && my >= iconY && my <= iconY + 16) {
                    hoveredStack = stack;
                    hoveredCostIndex = i;
                }
                leftStartX += 20;
            }

            int centerX = getX() + (width / 2) - 5;
            gui.drawString(font, "→", centerX, getY() + (height - 8) / 2, 0xFFFFFF);

            int rightEndX = getX() + width - 8;
            List<ItemStack> results = option.getResults();
            for (int i = results.size() - 1; i >= 0; i--) {
                ItemStack stack = results.get(i);
                rightEndX -= 16;
                gui.renderItem(stack, rightEndX, iconY);
                gui.renderItemDecorations(font, stack, rightEndX, iconY);

                if (mx >= rightEndX && mx <= rightEndX + 16 && my >= iconY && my <= iconY + 16) {
                    hoveredStack = stack;
                    hoveredCostIndex = null;
                }
                rightEndX -= 4;
            }

            if (hoveredStack != null) {
                List<Component> tooltipComponents = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), hoveredStack));

                if (hoveredCostIndex != null && option.isIgnoreNBT(hoveredCostIndex)) {
                    tooltipComponents.add(Component.empty());
                    tooltipComponents.add(Component.translatable("gui." + MODID + ".npc.trade_tooltips.ignore_nbt")
                            .withStyle(ChatFormatting.GREEN));
                }

                List<FormattedCharSequence> formattedTooltip = tooltipComponents.stream()
                        .map(Component::getVisualOrderText)
                        .toList();

                gui.renderTooltip(font, formattedTooltip, mx, my);
            }
        }

        @Override protected void updateWidgetNarration(@NotNull NarrationElementOutput n) {}
        public interface OnPress { void onPress(TradeButton button); }
    }

    public GuiState getCurrentState() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("textOffset", this.textScrollOffset);
        tag.putInt("optionIndex", this.optionScrollIndex);
        return new GuiState(tag);
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void renderBackground(@NotNull GuiGraphics g, int mx, int my, float p) {}
    @Override public void renderTransparentBackground(@NotNull GuiGraphics g) {}
}