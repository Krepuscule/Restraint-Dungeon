package com.twi.restraint_dungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.network.payload.restraints_packet.restraint_tool.RemoveRestraintToolPayload;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindArms;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

public class RestraintToolsConfigMenu extends Screen {

    private final LivingEntity targetEntity;
    private final boolean isSelf;

    private int hoveredSegment = -1;
    private final float innerR = 45f;
    private final float outerR = 105f;

    public RestraintToolsConfigMenu(LivingEntity targetEntity, boolean isSelf) {
        super(Component.translatable("gui." + MODID + ".restraint_tools.menu"));
        this.targetEntity = targetEntity;
        this.isSelf = isSelf;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x77000000);

        float cx = this.width / 2f;
        float cy = this.height / 2f;

        List<ItemStack> currentTools = RestraintToolsUtils.getAllRestraintTools(this.targetEntity);
        int entryCount = currentTools.size();

        Component hoveredName = null;

        this.hoveredSegment = -1;

        if (entryCount > 0) {
            float sectorStep = 360f / entryCount;
            double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));

            if (dist >= innerR && dist <= outerR) {
                if (entryCount == 1) {
                    this.hoveredSegment = 0;
                } else {
                    float mouseAngle = (float) Math.toDegrees(Math.atan2(mouseY - cy, mouseX - cx));
                    float nMouse = (mouseAngle % 360 + 360) % 360;

                    for (int i = 0; i < entryCount; i++) {
                        float startAngle = -90f - (i * sectorStep);
                        float endAngle = -90f - ((i + 1) * sectorStep);

                        float nA1 = (startAngle % 360 + 360) % 360;
                        float nA2 = (endAngle % 360 + 360) % 360;

                        boolean isInSector = (nA1 >= nA2) ? (nMouse <= nA1 && nMouse >= nA2) : (nMouse <= nA1 || nMouse >= nA2);
                        if (isInSector) {
                            this.hoveredSegment = i;
                            break;
                        }
                    }
                }
            }

            for (int i = 0; i < entryCount; i++) {
                float startAngle = -90f - (i * sectorStep);
                float endAngle = -90f - ((i + 1) * sectorStep);
                boolean hovered = (this.hoveredSegment == i);

                int color = hovered ? 0xAA555555 : 0x44222222;
                drawRadialSector(graphics, cx, cy, innerR, outerR, startAngle, endAngle, color);

                if (hovered) {
                    drawRadialOutline(graphics, cx, cy, innerR, outerR, startAngle, endAngle, 0xFFFFFFFF, entryCount);
                }

                double textRad = Math.toRadians((startAngle + endAngle) / 2f);
                float tx = cx + (float) Math.cos(textRad) * (innerR + outerR) / 2f;
                float ty = cy + (float) Math.sin(textRad) * (innerR + outerR) / 2f;

                ItemStack stack = currentTools.get(i);
                if (!stack.isEmpty() && stack.getItem() instanceof RestraintToolItem toolItem) {
                    ResourceLocation iconTex = toolItem.getItemIconResourceLocation(this.targetEntity, stack);
                    RenderSystem.enableBlend();
                    graphics.blit(iconTex, (int) tx - 8, (int) ty - 8, 0, 0, 16, 16, 16, 16);

                    if (hovered) {
                        hoveredName = stack.getHoverName();
                    }
                }
            }
        }
        else {
            drawRadialSector(graphics, cx, cy, innerR, outerR, -90f, -450f, 0x33333333);

            Component emptyHint = Component.translatable("gui." + MODID + ".restraint_tool.empty_tools");
            graphics.drawCenteredString(this.font, emptyHint, (int) cx, (int) cy + (int) outerR + 15, 0xFF888888);
        }

        if (hoveredName != null) {
            graphics.drawCenteredString(this.font, hoveredName, (int) cx, (int) cy - 4, 0xFFFFCC00);
        } else {
            graphics.drawCenteredString(this.font, "◆", (int) cx, (int) cy - 4, 0x99FFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (this.hoveredSegment != -1 && this.hoveredSegment < entryCount) {
            ItemStack hoveredStack = currentTools.get(this.hoveredSegment);
            if (!hoveredStack.isEmpty()) {
                graphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
            }
        }
    }

    @Override public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void renderTransparentBackground(GuiGraphics guiGraphics) {}

    private void drawRadialSector(GuiGraphics graphics, float cx, float cy, float r1, float r2, float a1, float a2, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = graphics.pose().last().pose();

        float a = (color >> 24 & 255) / 255f;
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;

        for (float angle = a1; angle >= a2; angle -= 2f) {
            double rad = Math.toRadians(angle);
            buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r1, cy + (float) Math.sin(rad) * r1, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r2, cy + (float) Math.sin(rad) * r2, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawRadialOutline(GuiGraphics graphics, float cx, float cy, float r1, float r2, float a1, float a2, int color, int entryCount) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        Matrix4f matrix = graphics.pose().last().pose();

        float a = (color >> 24 & 255) / 255f;
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;

        BufferBuilder buffer;

        if (entryCount == 1) {
            buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (float angle = a1; angle >= a2; angle -= 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r2, cy + (float) Math.sin(rad) * r2, 0).setColor(r, g, b, a);
            }
            BufferUploader.drawWithShader(buffer.buildOrThrow());

            buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (float angle = a1; angle >= a2; angle -= 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r1, cy + (float) Math.sin(rad) * r1, 0).setColor(r, g, b, a);
            }
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } else {
            buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            double radStart = Math.toRadians(a1);
            buffer.addVertex(matrix, cx + (float) Math.cos(radStart) * r1, cy + (float) Math.sin(radStart) * r1, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, cx + (float) Math.cos(radStart) * r2, cy + (float) Math.sin(radStart) * r2, 0).setColor(r, g, b, a);

            for (float angle = a1; angle >= a2; angle -= 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r2, cy + (float) Math.sin(rad) * r2, 0).setColor(r, g, b, a);
            }

            double radEnd = Math.toRadians(a2);
            buffer.addVertex(matrix, cx + (float) Math.cos(radEnd) * r2, cy + (float) Math.sin(radEnd) * r2, 0).setColor(r, g, b, a);

            for (float angle = a2; angle <= a1; angle += 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r1, cy + (float) Math.sin(rad) * r1, 0).setColor(r, g, b, a);
            }
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ItemStack> currentTools = RestraintToolsUtils.getAllRestraintTools(this.targetEntity);
        int totalSegments = currentTools.size();

        if (this.hoveredSegment != -1 && this.hoveredSegment < totalSegments) {
            int targetIndex = this.hoveredSegment;

            ItemStack realStack = RestraintToolsUtils.getRestraintToolByIndex(this.targetEntity, targetIndex);
            if (!realStack.isEmpty() && realStack.getItem() instanceof RestraintToolItem toolItem) {

                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                        && (!isBeenBindArms(Minecraft.getInstance().player) && !isBeenBindHands(Minecraft.getInstance().player))) {
                    Object screenObj = toolItem.getConfigurationScreen(this.targetEntity, realStack, targetIndex);
                    if (screenObj instanceof Screen configScreen) {
                        Minecraft.getInstance().setScreen(configScreen);
                        return true;
                    }
                }
                else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                        && (!isBeenBindArms(Minecraft.getInstance().player) && !isBeenBindHands(Minecraft.getInstance().player))) {
                    PacketDistributor.sendToServer(new RemoveRestraintToolPayload(this.targetEntity.getId(), targetIndex));
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}