package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.loose_struggle;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.client.hud.struggle_hud.StruggleHUDManager;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.common_utils.ShakeEffect;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_struggle.PlayerStruggleProgressPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getTargetPart;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.*;

public class LooseHUDHandler {

    private static final int ICON_BOX_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int ICON_OFFSET = 6;
    private static final int BADGE_BOX_SIZE = 8;
    private static final int BORDER_SIZE = 1;

    private static ResourceLocation getArrowTex(LooseStruggleData.Direction dir, String suffix) {
        String path = "textures/gui/screen/arrows/arrow_" + dir.name().toLowerCase() + suffix + ".png";
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static void render(GuiGraphics graphics, LooseStruggleData data, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int BAR_W = 128, BAR_H = 16;
        int ARROW_SIZE = 16, ARROW_SPACING = 4;
        int centerX = sw / 2, centerY = sh * 3 / 4;

        int currentLen = data.currentSequence.size();
        int totalArrowsW = currentLen * ARROW_SIZE + (currentLen - 1) * ARROW_SPACING;

        int barX = centerX - BAR_W / 2;
        int barY = centerY + 5;
        int arrowsStartX = centerX - totalArrowsW / 2;
        int arrowsY = centerY - 20;

        int itemIconX = barX - ICON_BOX_SIZE - ICON_OFFSET;
        int itemIconY = barY + (BAR_H - ICON_BOX_SIZE) / 2 + 2;
        int badgeX = (itemIconX + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);
        int badgeY = (itemIconY + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);

        if (data.isActive && data.progress >= 1.0f) {
            completeStruggle(player);
            return;
        }

        // ---  渲染背景 ---
        int minX = Math.min(arrowsStartX, itemIconX) - 4;
        int maxX = Math.max(arrowsStartX + totalArrowsW, Math.max(barX + BAR_W, badgeX + BADGE_BOX_SIZE)) + 4;
        int minY = arrowsY - 4;
        int maxY = Math.max(barY + BAR_H, badgeY + BADGE_BOX_SIZE) + 10;
        graphics.fill(minX, minY, maxX, maxY, 0x77000000);

        // ---  渲染物品与部位图标 ---
        renderIconBox(graphics, itemIconX, itemIconY, ICON_BOX_SIZE);
        ItemStack currentItem = getPlayerStrugglingItem(player);
        if (currentItem.getItem() instanceof RestraintItem restraintItem) {
            ResourceLocation itemTex = restraintItem.getItemIconResourceLocation(player, currentItem);
            graphics.blit(itemTex, itemIconX + 1, itemIconY + 1, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        PlayerRestraintPart part = getTargetPart(player);
        renderIconBox(graphics, badgeX, badgeY, BADGE_BOX_SIZE);
        ResourceLocation partTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/" + part.name().toLowerCase() + ".png");
        graphics.blit(partTex, badgeX, badgeY, 0, 0, 8, 8, 8, 8);

        // --- 5. 渲染箭头序列 ---
        for (int i = 0; i < currentLen; i++) {
            LooseStruggleData.Direction dir = data.currentSequence.get(i);
            int arrowX = arrowsStartX + i * (ARROW_SIZE + ARROW_SPACING);

            graphics.blit(getArrowTex(dir, ""), arrowX, arrowsY, 0, 0, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE);

            if (i == data.currentIndex) {
                graphics.blit(getArrowTex(dir, "_edge"), arrowX, arrowsY, 0, 0, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE);
                if (data.isInputBlocked()) {
                    graphics.blit(getArrowTex(dir, "_red"), arrowX, arrowsY, 0, 0, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE);
                }
            } else if (i < data.currentIndex) {
                graphics.blit(getArrowTex(dir, "_green"), arrowX, arrowsY, 0, 0, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE, ARROW_SIZE);
            }
        }

        // --- 6. 渲染进度条 ---
        graphics.blit(StruggleHUDManager.EMPTY_PROGRESS_BAR_STRUGGLE, barX, barY, 0, 0, BAR_W, BAR_H, BAR_W, BAR_H);
        int progressWidth = (int) (BAR_W * Mth.clamp(data.progress, 0, 1));
        if (progressWidth > 0) {
            graphics.blit(StruggleHUDManager.YELLOW_PROGRESS_BAR_STRUGGLE, barX, barY, 0, 0, progressWidth, BAR_H, BAR_W, BAR_H);
        }

        // --- 7. 锁图标 ---
        if (!data.lockItem.isEmpty()) {
            int lx = centerX - 8, ly = barY + BAR_H;
            if (data.isInputBlocked() && !data.lockShake.isActive()) data.lockShake.startShake(10000);
            else if (!data.isInputBlocked() && data.lockShake.isActive()) data.lockShake.stopShake();

            float[] offset = data.lockShake.getShakeOffset();
            graphics.blit(ShakeEffect.LOCK_ICON, lx + (int)offset[0], ly + (int)offset[1], 0, 0, 16, 16, 16, 16);
        }
    }

    private static void renderIconBox(GuiGraphics g, int x, int y, int size) {
        g.fill(x - BORDER_SIZE, y - BORDER_SIZE, x + size + BORDER_SIZE, y + size + BORDER_SIZE, 0xFF888888);
        g.fill(x, y, x + size, y + size, 0xFF222222);
    }

    public static boolean handleInput(int keyCode, LooseStruggleData data,int scanCode, int modifiers) {
        if (data.isInputBlocked()) return false;

        LooseStruggleData.Direction inputDir = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> LooseStruggleData.Direction.UP;
            case GLFW.GLFW_KEY_DOWN -> LooseStruggleData.Direction.DOWN;
            case GLFW.GLFW_KEY_LEFT -> LooseStruggleData.Direction.LEFT;
            case GLFW.GLFW_KEY_RIGHT -> LooseStruggleData.Direction.RIGHT;
            default -> null;
        };

        if (inputDir != null) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return false;

            boolean correct = data.checkInput(inputDir);
            if (!correct) {
                data.blockInput(2000);
                float reg = data.minRegressionRate + (float) Math.random() * (data.maxRegressionRate - data.minRegressionRate);
                data.progress = Math.max(0, data.progress - reg);
            } else if (data.isSequenceComplete()) {
                float reward = data.minProgress + (float) Math.random() * (data.maxProgress - data.minProgress);
                data.progress = Math.min(1.0f, data.progress + reward);
                data.resetSequence();
            }

            PacketDistributor.sendToServer(new PlayerStruggleProgressPayload(data.progress));
            return true;
        }
        return false;
    }

    private static void completeStruggle(Player player) {

        StruggleHUDManager.deactivate();
        playerOutOfRestraint(player);
    }
}