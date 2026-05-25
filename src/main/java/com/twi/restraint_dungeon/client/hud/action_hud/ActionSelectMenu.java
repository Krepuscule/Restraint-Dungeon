package com.twi.restraint_dungeon.client.hud.action_hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.utils.ActionManager;
import com.twi.restraint_dungeon.network.payload.player_action.ActionExecutePayload;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ActionSelectMenu extends Screen {

    private final Player actionPlayer;
    private final @Nullable LivingEntity target;
    private final List<MenuEntry> entries = new ArrayList<>();
    private final float innerR = 45f;
    private final float outerR = 105f;

    public record MenuEntry(Component name, String actionId) {}

    public ActionSelectMenu(Player actionPlayer, @Nullable LivingEntity target) {
        super(Component.literal("Action Menu"));
        this.target = target;
        this.actionPlayer = actionPlayer;

        String carryingState = PlayerCarryUtils.getCarryState(actionPlayer);
        boolean isCarryTarget = PlayerCarryUtils.isTargetFlag(actionPlayer);

        // 动态加载所有满足条件的 Action
        for (String id : ActionManager.getRegisteredIds()) {
            BaseAction action = ActionManager.get(id);

            if (action != null && action.shouldShowInMenu(actionPlayer, target, carryingState,isCarryTarget)) {
                entries.add(new MenuEntry(action.getDisplayName(), id));
            }
        }

        // 如果没有可用动作，显示一个置灰的“无”
        if (entries.isEmpty()) {
            entries.add(new MenuEntry(Component.translatable("action." + MODID + ".none"), "NONE"));
        }
    }

    // --- 渲染逻辑部分 (保持你提供的绘制代码不变) ---

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float cx = width / 2f;
        float cy = height / 2f;
        int entryCount = entries.size();
        float sectorStep = 360f / entryCount;

        for (int i = 0; i < entryCount; i++) {
            float startAngle = -90f - (i * sectorStep);
            float endAngle = -90f - ((i + 1) * sectorStep);

            boolean hovered;
            if (entryCount == 1) {
                double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));
                hovered = (dist >= innerR && dist <= outerR);
            } else {
                hovered = isMouseInSector(mouseX, mouseY, cx, cy, startAngle, endAngle);
            }

            MenuEntry entry = entries.get(i);
            boolean isNone = entry.actionId().equals("NONE");

            int color = isNone ? 0x44000000 : (hovered ? 0xAA444444 : 0x88000000);
            drawRadialSector(graphics, cx, cy, innerR, outerR, startAngle, endAngle, color);

            if (hovered && !isNone) {
                drawRadialOutline(graphics, cx, cy, innerR, outerR, startAngle, endAngle, 0xFFFFFFFF);
            }

            double textRad = Math.toRadians((startAngle + endAngle) / 2f);
            float tx = cx + (float) Math.cos(textRad) * (innerR + outerR) / 2f;
            float ty = cy + (float) Math.sin(textRad) * (innerR + outerR) / 2f;

            int textColor = isNone ? 0x66FFFFFF : (hovered ? 0xFFFFCC00 : 0xFFFFFF);
            graphics.drawCenteredString(font, entry.name(), (int)tx, (int)ty - 4, textColor);
        }
    }

    // [在此处保留你提供的 drawRadialSector, drawRadialOutline, isMouseInSector 方法...]

    private void drawRadialSector(GuiGraphics graphics, float cx, float cy, float r1, float r2, float a1, float a2, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
            float cos = (float)Math.cos(rad);
            float sin = (float)Math.sin(rad);
            buffer.addVertex(matrix, cx + cos * r1, cy + sin * r1, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, cx + cos * r2, cy + sin * r2, 0).setColor(r, g, b, a);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void drawRadialOutline(GuiGraphics graphics, float cx, float cy, float r1, float r2, float a1, float a2, int color) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        Matrix4f matrix = graphics.pose().last().pose();

        float a = (color >> 24 & 255) / 255f, r = (color >> 16 & 255) / 255f, g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (float angle = a1; angle >= a2; angle -= 2f) {
            double rad = Math.toRadians(angle);
            buffer.addVertex(matrix, cx + (float)Math.cos(rad) * r2, cy + (float)Math.sin(rad) * r2, 0).setColor(r, g, b, a);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (float angle = a2; angle <= a1; angle += 2f) {
            double rad = Math.toRadians(angle);
            buffer.addVertex(matrix, cx + (float)Math.cos(rad) * r1, cy + (float)Math.sin(rad) * r1, 0).setColor(r, g, b, a);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private boolean isMouseInSector(double mx, double my, float cx, float cy, float a1, float a2) {
        double dist = Math.sqrt(Math.pow(mx - cx, 2) + Math.pow(my - cy, 2));
        if (dist < innerR || dist > outerR) return false;
        float mouseAngle = (float) Math.toDegrees(Math.atan2(my - cy, mx - cx));
        float nMouse = (mouseAngle % 360 + 360) % 360;
        float nA1 = (a1 % 360 + 360) % 360;
        float nA2 = (a2 % 360 + 360) % 360;
        if (nA1 >= nA2) return nMouse <= nA1 && nMouse >= nA2;
        else return nMouse <= nA1 || nMouse >= nA2;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b == 0) {
            float cx = width / 2f;
            float cy = height / 2f;
            if (entries.size() == 1) {
                double dist = Math.sqrt(Math.pow(mx - cx, 2) + Math.pow(my - cy, 2));
                if (dist >= innerR && dist <= outerR) { executeEntry(entries.get(0)); return true; }
            }
            float sz = 360f / entries.size();
            for (int i = 0; i < entries.size(); i++) {
                float a1 = -90f - (i * sz);
                float a2 = -90f - ((i + 1) * sz);
                if (isMouseInSector(mx, my, cx, cy, a1, a2)) {
                    executeEntry(entries.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    private void executeEntry(MenuEntry entry) {
        if (entry.actionId().equals("NONE")) return;

        // 获取目标的 Runtime ID，如果没有目标则传 -1
        int targetId = (target != null) ? target.getId() : -1;

        // 发送网络包到服务端请求执行 Action
        PacketDistributor.sendToServer(new ActionExecutePayload(entry.actionId(), targetId));

        this.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }
}