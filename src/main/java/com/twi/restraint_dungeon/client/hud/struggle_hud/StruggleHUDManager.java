package com.twi.restraint_dungeon.client.hud.struggle_hud;

import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.loose_struggle.LooseHUDHandler;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.loose_struggle.LooseStruggleData;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.strength_struggle.StrengthHUDHandler;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.strength_struggle.StrengthStruggleData;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.unlock_struggle.UnlockHUDHandler;
import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.unlock_struggle.UnlockStruggleData;
import com.twi.restraint_dungeon.network.payload.player_struggle.PlayerIsStrugglingPayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.PlayerStruggleModePayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.PlayerStruggleProgressPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.*;

public class StruggleHUDManager {

    public static final ResourceLocation EMPTY_PROGRESS_BAR =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/empty_progress_bar.png");
    public static final ResourceLocation GREEN_PROGRESS_BAR =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/green_progress_bar.png");
    public static final ResourceLocation YELLOW_PROGRESS_BAR =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/yellow_progress_bar.png");
    public static final ResourceLocation RED_PROGRESS_BAR =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/red_progress_bar.png");
    public static final ResourceLocation EMPTY_PROGRESS_BAR_STRUGGLE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/empty_progress_bar_struggle.png");
    public static final ResourceLocation GREEN_PROGRESS_BAR_STRUGGLE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/green_progress_bar_struggle.png");
    public static final ResourceLocation RED_PROGRESS_BAR_STRUGGLE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/red_progress_bar_struggle.png");
    public static final ResourceLocation YELLOW_PROGRESS_BAR_STRUGGLE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/yellow_progress_bar_struggle.png");

    private static StruggleMode currentMode = null;
    private static boolean isActive = false;
    private static Object currentStruggleData = null;

    /**
     * 激活挣扎 HUD 和 QTE 数据
     */
    public static void activate(StruggleMode mode) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        currentMode = mode;
        isActive = true;

        updateIsStruggling(player, true);
        // 发送同步包：(正在挣扎=true, 是否成功结束=false)
        PacketDistributor.sendToServer(new PlayerIsStrugglingPayload(true, false));

        switch (mode) {
            case STRENGTH -> currentStruggleData = new StrengthStruggleData();
            case LOOSE -> currentStruggleData = new LooseStruggleData();
            case UNLOCK -> currentStruggleData = new UnlockStruggleData();
            default -> {}
        }
    }

    /**
     * 正常结束挣扎（如 QTE 成功完成）
     */
    public static void deactivate() {
        stop(true);
    }

    /**
     * 强制取消挣扎（如 Esc 退出或受击打断）
     */
    public static void cancel() {
        stop(false);
    }

    private static void stop(boolean success) {
        Player player = Minecraft.getInstance().player;
        isActive = false;
        currentMode = null;
        currentStruggleData = null;

        if (player != null) {
            updateStruggleMode(player, StruggleMode.NONE);
            PacketDistributor.sendToServer(new PlayerStruggleModePayload(StruggleMode.NONE.name()));

            updateIsStruggling(player, false);
            PacketDistributor.sendToServer(new PlayerIsStrugglingPayload(false, success));

            updateStruggleProgress(player, 0);
            PacketDistributor.sendToServer(new PlayerStruggleProgressPayload(0f));
        }
    }

    public static boolean isActive() {
        return isActive;
    }


    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        if (!isActive || currentMode == null) return;

        switch (currentMode) {
            case STRENGTH -> StrengthHUDHandler.render(guiGraphics, (StrengthStruggleData) currentStruggleData,partialTicks);
            case LOOSE -> LooseHUDHandler.render(guiGraphics, (LooseStruggleData) currentStruggleData,partialTicks);
            case UNLOCK -> UnlockHUDHandler.render(guiGraphics, (UnlockStruggleData) currentStruggleData,partialTicks);
            default -> {}
        }
    }

    /**
     * 处理 QTE 按键输入
     */
    public static boolean handleKeyInput(int keyCode, int scanCode, int modifiers) {
        if (!isActive || currentMode == null) return false;

        // 统一处理 ESC 键取消
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            cancel();
            return true;
        }

        return switch (currentMode) {
            case STRENGTH -> StrengthHUDHandler.handleInput(keyCode,(StrengthStruggleData) currentStruggleData,scanCode,modifiers);
            case LOOSE -> LooseHUDHandler.handleInput(keyCode,(LooseStruggleData) currentStruggleData,scanCode,modifiers);
            case UNLOCK -> UnlockHUDHandler.handleInput(keyCode,(UnlockStruggleData) currentStruggleData,scanCode,modifiers);
            default -> false;
        };
    }
}