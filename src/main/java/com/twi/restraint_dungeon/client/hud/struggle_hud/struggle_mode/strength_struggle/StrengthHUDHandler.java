package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.strength_struggle;

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

public class StrengthHUDHandler {

    private static final int ICON_BOX_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int ICON_OFFSET = 6;
    private static final int BADGE_BOX_SIZE = 8;
    private static final int BADGE_SIZE = 8;
    private static final int BORDER_SIZE = 1;

    public static void render(GuiGraphics graphics, StrengthStruggleData data, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 坐标计算
        int BAR_W = 128, BAR_H = 16;
        int centerX = screenWidth / 2;
        int centerY = screenHeight * 3 / 4;
        int barX = centerX - BAR_W / 2;
        int barY = centerY;

        int itemIconX = barX - ICON_BOX_SIZE - ICON_OFFSET;
        int itemIconY = barY + (BAR_H - ICON_BOX_SIZE) / 2 + 2;

        int badgeX = (itemIconX + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);
        int badgeY = (itemIconY + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);

        ItemStack currentItem = getPlayerStrugglingItem(player);
        if (!(currentItem.getItem() instanceof RestraintItem restraintItem)) return;


        float decayRate = strengthStruggle_DecayRate(player, currentItem);
        data.refreshLockInfo();

        long currentTime = System.currentTimeMillis();
        float elapsedFrames = (currentTime - data.lastUpdateTime) / 50.0f; // 基于 20tps 计算
        data.lastUpdateTime = currentTime;
        
        boolean isStopped = (currentTime - data.lastKeyPressTime) > 500;

        if (data.isActive) {
            // 只有在没满的时候才下降
            if (data.progress < 1.0f) {
                data.progress -= decayRate * elapsedFrames;
                data.progress = Math.max(0.0f, data.progress);
            } else {
                completeStruggle(player);
                return;
            }
        }

        // --- 渲染背景 ---
        renderBackground(graphics, barX, barY, BAR_W, BAR_H, itemIconX, itemIconY, badgeX, badgeY);

        // --- 渲染物品图标与角标 ---
        renderIconBox(graphics, itemIconX, itemIconY, ICON_BOX_SIZE);
        ResourceLocation itemTex = restraintItem.getItemIconResourceLocation(player, currentItem);
        graphics.blit(itemTex, itemIconX + 1, itemIconY + 1, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

        PlayerRestraintPart part = getTargetPart(player);
        renderIconBox(graphics, badgeX, badgeY, BADGE_BOX_SIZE);
        ResourceLocation partTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/" + part.name().toLowerCase() + ".png");
        graphics.blit(partTex, badgeX, badgeY, 0, 0, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE);

        // --- 渲染进度条 ---
        graphics.blit(StruggleHUDManager.EMPTY_PROGRESS_BAR_STRUGGLE, barX, barY, 0, 0, BAR_W, BAR_H, BAR_W, BAR_H);
        int progressWidth = (int) (BAR_W * data.progress);
        if (progressWidth > 0) {
            ResourceLocation barTex = isStopped ? StruggleHUDManager.RED_PROGRESS_BAR_STRUGGLE : StruggleHUDManager.GREEN_PROGRESS_BAR_STRUGGLE;
            graphics.blit(barTex, barX, barY, 0, 0, progressWidth, BAR_H, BAR_W, BAR_H);

            if (isStopped && (currentTime / 250) % 2 == 0) {
                graphics.fill(barX - 1, barY - 1, barX + progressWidth + 1, barY + BAR_H + 1, 0x44FF0000);
            }
        }

        // --- 渲染锁图标 ---
        if (!data.lockItem.isEmpty()) {
            int lockX = barX + BAR_W / 2 - 8;
            int lockY = barY - 16;
            
            if (isStopped && !data.lockShake.isActive()) {
                data.lockShake.startShake(10000);
            } else if (!isStopped && data.lockShake.isActive()) {
                data.lockShake.stopShake();
            }

            float[] offset = data.lockShake.getShakeOffset();
            graphics.blit(ShakeEffect.LOCK_ICON, lockX + (int)offset[0], lockY + (int)offset[1], 0, 0, 16, 16, 16, 16);
        }
    }

    private static void renderBackground(GuiGraphics g, int x, int y, int w, int h, int ix, int iy, int bx, int by) {
        int minX = ix - 4;
        int maxX = Math.max(x + w, bx + BADGE_BOX_SIZE) + 4;
        int minY = y - 14;
        int maxY = Math.max(y + h, by + BADGE_BOX_SIZE) + 4;
        g.fill(minX, minY, maxX, maxY, 0x77000000);
    }

    private static void renderIconBox(GuiGraphics g, int x, int y, int size) {
        g.fill(x - BORDER_SIZE, y - BORDER_SIZE, x + size + BORDER_SIZE, y + size + BORDER_SIZE, 0xFF888888);
        g.fill(x, y, x + size, y + size, 0xFF222222);
    }

    public static boolean handleInput(int keyCode, StrengthStruggleData data,int scanCode, int modifiers) {
        if (!data.isActive) return false;
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            data.lastKeyPressTime = System.currentTimeMillis();

            ItemStack currentItem = getPlayerStrugglingItem(player);
            if (!(currentItem.getItem() instanceof RestraintItem)) return false;

            float increment = strengthStruggle_Increment(player, currentItem);

            boolean canProgress = (keyCode == GLFW.GLFW_KEY_LEFT && !data.lastKeyWasLeft) || 
                                 (keyCode == GLFW.GLFW_KEY_RIGHT && data.lastKeyWasLeft);

            if (canProgress) {
                data.progress += increment;
                data.lastKeyWasLeft = (keyCode == GLFW.GLFW_KEY_LEFT);
                data.progress = Mth.clamp(data.progress, 0.0f, 1.1f); // 允许微弱溢出触发完成逻辑

                PacketDistributor.sendToServer(new PlayerStruggleProgressPayload(data.progress));
            }
            return true;
        }
        return false;
    }

    private static void completeStruggle(Player player) {
        StruggleHUDManager.deactivate();
        playerOutOfRestraint(player);
    }
}