package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.unlock_struggle;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.client.hud.struggle_hud.StruggleHUDManager;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.common_utils.ShakeEffect;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_struggle.PlayerStruggleProgressPayload;
import com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getTargetPart;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getPlayerStrugglingItem;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.playerOutOfRestraint;

public class UnlockHUDHandler {

    private static final ResourceLocation POINTER_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/pointer.png");
    private static final int ICON_BOX_SIZE = 18, ICON_SIZE = 16, ICON_OFFSET = 6;
    private static final int BADGE_BOX_SIZE = 8, BORDER_SIZE = 1;

    public static void render(GuiGraphics graphics, UnlockStruggleData data, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        int MAIN_W = 128, MAIN_H = 16, OVERALL_W = 64, OVERALL_H = 8, SPACING = 6;
        int centerX = sw / 2, centerY = sh * 3 / 4;

        int totalH = MAIN_H + SPACING + OVERALL_H;
        int mainY = centerY - totalH / 2, overallY = mainY + MAIN_H + SPACING;
        int mainX = centerX - MAIN_W / 2, overallX = centerX - OVERALL_W / 2;

        int itemIconX = overallX - ICON_BOX_SIZE - ICON_OFFSET;
        int itemIconY = overallY + (OVERALL_H - ICON_BOX_SIZE) / 2 + 2;
        int badgeX = (itemIconX + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);
        int badgeY = (itemIconY + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);

        // --- 背景渲染 ---
        int minX = mainX - 4;
        int maxX = Math.max(mainX + MAIN_W, badgeX + BADGE_BOX_SIZE) + 4;
        int minY = mainY - 9, maxY = Math.max(overallY + OVERALL_H, badgeY + BADGE_BOX_SIZE) + 14;
        graphics.fill(minX, minY, maxX, maxY, 0x77000000);

        // --- 逻辑更新 ---
        long now = System.currentTimeMillis();
        if (data.isActive) {
            data.updatePointerPosition(now - data.lastUpdateTime);
            data.updateState();
        }
        data.lastUpdateTime = now;

        // --- 图标与角标 ---
        renderIconBox(graphics, itemIconX, itemIconY, ICON_BOX_SIZE);
        ItemStack item = getPlayerStrugglingItem(player);
        if (item.getItem() instanceof RestraintItem restraintItem) {
            graphics.blit(restraintItem.getItemIconResourceLocation(player, item), itemIconX + 1, itemIconY + 1, 0, 0, 16, 16, 16, 16);
        }
        
        renderIconBox(graphics, badgeX, badgeY, BADGE_BOX_SIZE);
        PlayerRestraintPart part = getTargetPart(player);
        ResourceLocation pTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/" + part.name().toLowerCase() + ".png");
        graphics.blit(pTex, badgeX, badgeY, 0, 0, 8, 8, 8, 8);

        // --- 滑动条与目标区域 ---
        graphics.blit(StruggleHUDManager.EMPTY_PROGRESS_BAR_STRUGGLE, mainX, mainY, 0, 0, MAIN_W, MAIN_H, MAIN_W, MAIN_H);
        int targetW = (int) (MAIN_W * data.targetZoneRatio);
        int targetX = mainX + (int) (MAIN_W * data.targetZoneStart);
        if (targetW > 0) {
            graphics.blit(StruggleHUDManager.GREEN_PROGRESS_BAR_STRUGGLE, targetX, mainY, (int)(MAIN_W * data.targetZoneStart), 0, targetW, MAIN_H, MAIN_W, MAIN_H);
        }

        // 指针
        float effPos = data.pointerPosition * (1 - data.pointerWidthPercentage) + data.pointerWidthPercentage / 2;
        int ptrX = mainX + (int) (MAIN_W * effPos) - 8;
        graphics.blit(POINTER_TEXTURE, ptrX, mainY - 5, 0, 0, 16, 16, 16, 16);

        // --- 总进度条 ---
        graphics.blit(StruggleHUDManager.EMPTY_PROGRESS_BAR, overallX, overallY, 0, 0, OVERALL_W, OVERALL_H, OVERALL_W, OVERALL_H);
        int progW = (int) (OVERALL_W * data.overallProgress);
        if (progW > 0) {
            ResourceLocation fTex = switch (data.currentFeedback) {
                case SUCCESS -> StruggleHUDManager.GREEN_PROGRESS_BAR;
                case MISS -> StruggleHUDManager.RED_PROGRESS_BAR;
                default -> StruggleHUDManager.YELLOW_PROGRESS_BAR;
            };
            graphics.blit(fTex, overallX, overallY, 0, 0, progW, OVERALL_H, OVERALL_W, OVERALL_H);
        }

        // --- 锁图标与消息 ---
        if (!data.lockItem.isEmpty()) {
            float[] shake = data.lockShake.getShakeOffset();
            graphics.blit(ShakeEffect.LOCK_ICON, mainX + MAIN_W / 2 - 8 + (int)shake[0], overallY + OVERALL_H + (int)shake[1], 0, 0, 16, 16, 16, 16);
            if (data.showLockedMessage) {
                graphics.drawCenteredString(mc.font, Component.translatable("hud.restraint_dungeon.already_locked"), centerX, mainY - 25, 0xFF5555);
            }
        }
    }

    private static void renderIconBox(GuiGraphics g, int x, int y, int size) {
        g.fill(x - BORDER_SIZE, y - BORDER_SIZE, x + size + BORDER_SIZE, y + size + BORDER_SIZE, 0xFF888888);
        g.fill(x, y, x + size, y + size, 0xFF222222);
    }

    public static boolean handleInput(int keyCode, UnlockStruggleData data,int scanCode, int modifiers) {
        if (!data.isActive) return false;
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return false;

            if (!data.lockItem.isEmpty()) {
                data.lockShake.startShakeWithCount(2, 200);
                data.showLockedMessage();
                return true;
            }

            float pos = data.pointerPosition * (1 - data.pointerWidthPercentage) + data.pointerWidthPercentage / 2;
            if (pos >= data.targetZoneStart && pos <= data.targetZoneEnd) {
                data.handleSuccess();
                if (data.overallProgress >= 1.0f) completeStruggle(player);
            } else {
                data.handleMiss();
            }

            PacketDistributor.sendToServer(new PlayerStruggleProgressPayload(data.overallProgress));
            return true;
        }
        return false;
    }

    private static void completeStruggle(Player player) {
        StruggleHUDManager.deactivate();
        playerOutOfRestraint(player);
    }
}