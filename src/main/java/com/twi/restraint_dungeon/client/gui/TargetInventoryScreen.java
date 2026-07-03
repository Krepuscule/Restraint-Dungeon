package com.twi.restraint_dungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class TargetInventoryScreen extends AbstractContainerScreen<TargetInventoryMenu> {
    private static final ResourceLocation INVENTORY_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/inventory.png");

    private final Component targetTitle;
    private final Component myTitle;

    private static final int DESIGN_WIDTH = 180;
    private static final int DESIGN_HEIGHT = 234;

    public TargetInventoryScreen(TargetInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = DESIGN_WIDTH;
        this.imageHeight = DESIGN_HEIGHT;

        this.targetTitle = title;
        this.myTitle = Component.translatable("gui." + MODID + ".label.your_inventory").withStyle(ChatFormatting.WHITE);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.targetTitle, 2, -10, 0x404040, false);
        graphics.drawString(this.font, this.myTitle, 2, 4 + 130, 0x404040, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int startX = this.leftPos;
        int startY = this.topPos;

        graphics.blit(INVENTORY_BACKGROUND, startX, startY, 0, 0, DESIGN_WIDTH, DESIGN_HEIGHT, DESIGN_WIDTH, DESIGN_HEIGHT);
    }
}