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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    private final HitResult hitResult;
    private final List<MenuEntry> entries = new ArrayList<>();
    private final float innerR = 45f;
    private final float outerR = 105f;

    public record MenuEntry(Component name, String actionId) {}

    public ActionSelectMenu(Player actionPlayer, @Nullable LivingEntity target,HitResult result) {
        super(Component.literal("Action Menu"));
        this.target = target;
        this.actionPlayer = actionPlayer;
        this.hitResult = result;

        String carryingState = PlayerCarryUtils.getCarryState(actionPlayer);
        boolean isCarryTarget = PlayerCarryUtils.isTargetFlag(actionPlayer);

        // 动态加载所有满足条件的 Action
        for (String id : ActionManager.getRegisteredIds()) {
            BaseAction action = ActionManager.get(id);

            if (action != null && action.shouldShowInMenu(actionPlayer, target,result, carryingState,isCarryTarget)) {
                entries.add(new MenuEntry(action.getDisplayName(), id));
            }
        }

        if (entries.isEmpty()) {
            entries.add(new MenuEntry(Component.translatable("action." + MODID + ".none"), "NONE"));
        }
    }


    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float cx = width / 2f;
        float cy = height / 2f;
        int entryCount = entries.size();
        float sectorStep = 360f / entryCount;

        for (int i = 0; i < entryCount; i++) {
            float startAngle = -90f - (i * sectorStep);
            float endAngle = -90f - ((i + 1) * sectorStep);

            MenuEntry entry = entries.get(i);
            boolean isNone = entry.actionId().equals("NONE");
            boolean hovered;
            if (isNone) {
                hovered = false;
            } else if (entryCount == 1) {
                double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));
                hovered = (dist >= innerR && dist <= outerR);
            } else {
                hovered = isMouseInSector(mouseX, mouseY, cx, cy, startAngle, endAngle);
            }

            int color = isNone ? 0x44000000 : (hovered ? 0xAA444444 : 0x88000000);
            drawRadialSector(graphics, cx, cy, innerR, outerR, startAngle, endAngle, color);

            if (hovered) {
                drawRadialOutline(graphics, cx, cy, innerR, outerR, startAngle, endAngle, 0xFFFFFFFF, entryCount);
            }

            double textRad = Math.toRadians((startAngle + endAngle) / 2f);
            float tx = cx + (float) Math.cos(textRad) * (innerR + outerR) / 2f;
            float ty = cy + (float) Math.sin(textRad) * (innerR + outerR) / 2f;

            int textColor = isNone ? 0x66FFFFFF : (hovered ? 0xFFFFCC00 : 0xFFFFFF);
            graphics.drawCenteredString(font, entry.name(), (int)tx, (int)ty - 4, textColor);
        }
    }



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

        for (float angle = a1; angle >= a2; angle -= 1f) {
            double rad = Math.toRadians(angle);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            buffer.addVertex(matrix, cx + cos * r1, cy + sin * r1, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, cx + cos * r2, cy + sin * r2, 0).setColor(r, g, b, a);
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

        int entityId = -1;
        BlockPos blockPos = null;
        Direction direction = Direction.UP;
        Vec3 hitVec = this.hitResult != null ? this.hitResult.getLocation() : Vec3.ZERO;

        if (this.hitResult instanceof EntityHitResult entityHit) {
            entityId = entityHit.getEntity().getId();
        }
        else if (this.hitResult instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
            blockPos = blockHit.getBlockPos();
            direction = blockHit.getDirection();
        }

        PacketDistributor.sendToServer(new ActionExecutePayload(
                entry.actionId(),
                entityId,
                java.util.Optional.ofNullable(blockPos),
                direction,
                hitVec
        ));

        this.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }
}