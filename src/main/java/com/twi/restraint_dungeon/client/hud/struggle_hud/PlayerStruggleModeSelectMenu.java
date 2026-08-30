package com.twi.restraint_dungeon.client.hud.struggle_hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import com.twi.restraint_dungeon.client.keybind.ModKeyBinds;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerRestraintPartPayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.PlayerReleaseSelfPayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.PlayerStruggleModePayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.StruggleOutOfRestraintPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isBeingCarried;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isCarrier;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.isKidnappingActive;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.isReleaseActive;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.self_bondage.SelfBondageUtils.isSelfBondaging;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.*;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;

public class PlayerStruggleModeSelectMenu extends Screen {

    private final List<MenuEntry> entries = new ArrayList<>();
    private final float innerR = 45f;
    private final float outerR = 105f;
    private final Player player;

    public record MenuEntry(Component name, StruggleMode struggleMode) {}

    public PlayerStruggleModeSelectMenu() {
        super(Component.translatable("hud." + MODID + ".struggle_mode_menu"));
        this.player = Minecraft.getInstance().player;


        entries.add(new MenuEntry(Component.translatable("hud." + MODID + ".struggle_menu.strength"), StruggleMode.STRENGTH));
        entries.add(new MenuEntry(Component.translatable("hud." + MODID + ".struggle_menu.loose"), StruggleMode.LOOSE));
        entries.add(new MenuEntry(Component.translatable("hud." + MODID + ".struggle_menu.unlock"), StruggleMode.UNLOCK));


        if (player != null && !isBeenBindArms(player) && !isBeenBindHands(player)) {
            entries.add(new MenuEntry(Component.translatable("hud.restraint_dungeon.struggle_menu.release"), StruggleMode.RELEASE));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        
        float cx = width / 2f;
        float cy = height / 2f;

        renderCenterPartInfo(graphics, cx, cy);

        int entryCount = entries.size();
        float sectorStep = 360f / entryCount;

        for (int i = 0; i < entryCount; i++) {
            // 起始角度偏移 -90度（顶部开始），逆时针分布
            float startAngle = -90f - (i * sectorStep);
            float endAngle = -90f - ((i + 1) * sectorStep);

            boolean hovered;
            if (entryCount == 1) {
                double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));
                hovered = dist >= innerR && dist <= outerR;
            } else {
                hovered = isMouseInSector(mouseX, mouseY, cx, cy, startAngle, endAngle);
            }
            MenuEntry entry = entries.get(i);

            int color = hovered ? 0xAA444444 : 0x55000000;
            drawRadialSector(graphics, cx, cy, innerR, outerR, startAngle, endAngle, color);

            if (hovered) {
                drawRadialOutline(graphics, cx, cy, innerR, outerR, startAngle, endAngle, 0xFFFFFFFF, entryCount);
            }

            double textRad = Math.toRadians((startAngle + endAngle) / 2f);
            float tx = cx + (float) Math.cos(textRad) * (innerR + outerR) / 2f;
            float ty = cy + (float) Math.sin(textRad) * (innerR + outerR) / 2f;

            int textColor = hovered ? 0xFFFFCC00 : 0xFFFFFF;
            graphics.drawCenteredString(font, entry.name(), (int)tx, (int)ty - 4, textColor);
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void renderTransparentBackground(@NotNull GuiGraphics guiGraphics) {}

    private void renderCenterPartInfo(GuiGraphics graphics, float cx, float cy) {
        if (player == null) return;
        PlayerRestraintPart selected = getTargetPart(player);
        Component partName = Component.translatable(selected.getTranslationKey());
        Component prefix = Component.translatable("hud.restraint_dungeon.restraint_part_hud_selected");
        Component fullText = Component.empty().append(prefix).append(partName).withStyle(ChatFormatting.GOLD);
        graphics.drawCenteredString(font, fullText, (int)cx, (int)cy - 4, 0xFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            changeSelectedPart(true);
        } else if (scrollY < 0) {
            changeSelectedPart(false);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        if (keyCode == ModKeyBinds.CHANGE_PART_UP.getKey().getValue()) {
            changeSelectedPart(true);
            return true;
        } else if (keyCode == ModKeyBinds.CHANGE_PART_DOWN.getKey().getValue()) {
            changeSelectedPart(false);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b == 0) {
            float cx = width / 2f;
            float cy = height / 2f;
            int entryCount = entries.size();

            if (entryCount == 1) {
                double dist = Math.sqrt(Math.pow(mx - cx, 2) + Math.pow(my - cy, 2));
                if (dist >= innerR && dist <= outerR) {
                    executeEntry(entries.getFirst());
                    return true;
                }
            }
            float sz = 360f / entries.size();
            for (int i = 0; i < entries.size(); i++) {
                if (isMouseInSector(mx, my, cx, cy, -90f - (i * sz), -90f - ((i + 1) * sz))) {
                    executeEntry(entries.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    private void executeEntry(MenuEntry entry) {
        if (player == null) return;
        
        PlayerRestraintPart currentPart = getTargetPart(player);
        ItemStack strugglingItem = getPlayerStrugglingItem(player);
        int itemIndex = getPlayerStrugglingItemIndex(player);

        if (entry.struggleMode == StruggleMode.RELEASE) {
            handleSelfReleaseMode(player, strugglingItem, currentPart, itemIndex);
        } else {
            handleStruggleMode(entry,player,strugglingItem, currentPart, itemIndex);
        }
    }

    private void handleSelfReleaseMode(Player player,ItemStack stack, PlayerRestraintPart part, int index) {

        PacketDistributor.sendToServer(new PlayerReleaseSelfPayload(String.valueOf(part)));
        this.onClose();
    }

    private void handleStruggleMode(MenuEntry entry,Player player, ItemStack stack, PlayerRestraintPart part, int index) {

       PacketDistributor.sendToServer(new PlayerStruggleModePayload(entry.struggleMode().name()));

       this.onClose();
    }

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

        for (float angle = a1; angle >= a2; angle -= 1f) {
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
        float a = (color >> 24 & 255) / 255f, r = (color >> 16 & 255) / 255f, g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;

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

    private boolean isMouseInSector(double mx, double my, float cx, float cy, float a1, float a2) {
        double dist = Math.sqrt(Math.pow(mx - cx, 2) + Math.pow(my - cy, 2));
        if (dist < innerR || dist > outerR) return false;
        float mouseAngle = (float) Math.toDegrees(Math.atan2(my - cy, mx - cx));
        float nMouse = (mouseAngle % 360 + 360) % 360;
        float nA1 = (a1 % 360 + 360) % 360;
        float nA2 = (a2 % 360 + 360) % 360;
        return nA1 >= nA2 ? (nMouse <= nA1 && nMouse >= nA2) : (nMouse <= nA1 || nMouse >= nA2);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private void changeSelectedPart(boolean up) {
        if (player == null) return;
        PlayerRestraintPart current = getTargetPart(player);
        PlayerRestraintPart next = up ? current.previous() : current.next();
        setTargetPart(player, next);
        PacketDistributor.sendToServer(new PlayerRestraintPartPayload(next.name()));
    }
}