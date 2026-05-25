package com.twi.restraint_dungeon.client.hud.restraint_part_select_hud;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.client.hud.struggle_hud.StruggleHUDManager;
import com.twi.restraint_dungeon.client.keybind.ModKeyBinds;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerRestraintPartPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.client.keybind.ModKeyBinds.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class RestraintPartSelectHUD {

    private static boolean wasKeyPressed = false;
    private static boolean isSelecting = false;

    // 快速切换状态
    private static boolean quickSelectActive = false;
    private static long quickSelectStartTime = 0;
    private static final long QUICK_SELECT_FADE_DELAY = 2000;
    private static final long QUICK_SELECT_FADE_DURATION = 1000;
    private static float quickSelectAlpha = 1.0f;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null) {
            resetAllHUDStates();
            return;
        }

        // 优先级：如果正在挣扎，则不响应部位切换逻辑
        if (StruggleHUDManager.isActive()) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            boolean canRespond = canRespondToQuickSelect(minecraft);
            if (event.getKey() == CHANGE_PART_UP.getKey().getValue() && canRespond) {
                triggerQuickSelect(player, true);
                return;
            }
            if (event.getKey() == CHANGE_PART_DOWN.getKey().getValue() && canRespond) {
                triggerQuickSelect(player, false);
                return;
            }
        }

        // 处理 HUD 选择键 (X键: 按住显示)
        if (event.getKey() == CHANGE_PART_HUD.getKey().getValue()) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                if (!wasKeyPressed) {
                    isSelecting = true;
                    wasKeyPressed = true;
                    quickSelectActive = false;
                }
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                if (wasKeyPressed) {
                    setTargetPart(player, getTargetPart(player));
                    PacketDistributor.sendToServer(new PlayerRestraintPartPayload(getTargetPart(player).name()));
                    isSelecting = false;
                    wasKeyPressed = false;
                }
            }
        } else if (event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() == GLFW.GLFW_PRESS && wasKeyPressed) {
            isSelecting = false;
            wasKeyPressed = false;
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        // 仅在按住X键选择时拦截滚轮切换部位
        if (!isSelecting || Minecraft.getInstance().screen != null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerRestraintPart current = getTargetPart(player);
        PlayerRestraintPart newPart = event.getScrollDeltaY() > 0 ? previous(current) : current.next();

        updateAndSyncPart(player, newPart);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) return;

        // 如果正在挣扎，不渲染部位选择提示
        if (StruggleHUDManager.isActive()) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        if (quickSelectActive && quickSelectAlpha > 0.01f) {
            renderBaseHUD(guiGraphics, minecraft, quickSelectAlpha, false);
        }

        if (isSelecting) {
            renderBaseHUD(guiGraphics, minecraft, 1.0f, true);
        }
    }

    private static void renderBaseHUD(GuiGraphics guiGraphics, Minecraft minecraft, float alpha, boolean showPrompt) {
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        Font font = minecraft.font;
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        PlayerRestraintPart selected = getTargetPart(player);
        Component partName = Component.translatable(selected.getTranslationKey());

        // 根据手持物品决定颜色（如果是拘束具则显示深红，表示准备装配；否则深绿）
        ChatFormatting color = player.getMainHandItem().getItem() instanceof RestraintItem ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GREEN;

        MutableComponent text = Component.translatable("hud.restraint_dungeon.restraint_part_hud_selected")
                .append(partName).withStyle(ChatFormatting.BOLD).withStyle(color);

        if (showPrompt) {
            text.append(Component.literal(" ↑↓ ").withStyle(ChatFormatting.GOLD));
        }

        drawFloatingText(guiGraphics, font, text, 10, screenHeight - 20, alpha);
    }

    private static void updateAndSyncPart(LocalPlayer player, PlayerRestraintPart newPart) {
        setTargetPart(player, newPart);
        PacketDistributor.sendToServer(new PlayerRestraintPartPayload(newPart.name()));
    }

    private static void triggerQuickSelect(LocalPlayer player, boolean up) {
        quickSelectActive = true;
        quickSelectStartTime = System.currentTimeMillis();
        quickSelectAlpha = 1.0f;

        PlayerRestraintPart current = getTargetPart(player);
        PlayerRestraintPart newPart = up ? previous(current) : current.next();
        updateAndSyncPart(player, newPart);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 处理快速切换的淡出逻辑
        if (quickSelectActive) {
            long elapsed = System.currentTimeMillis() - quickSelectStartTime;
            if (elapsed > QUICK_SELECT_FADE_DELAY) {
                float fade = (float) (elapsed - QUICK_SELECT_FADE_DELAY) / QUICK_SELECT_FADE_DURATION;
                quickSelectAlpha = Math.max(0, 1.0f - fade);
                if (quickSelectAlpha <= 0) quickSelectActive = false;
            }
        }
    }

    private static void resetAllHUDStates() {
        isSelecting = false;
        wasKeyPressed = false;
        quickSelectActive = false;
    }

    private static boolean canRespondToQuickSelect(Minecraft minecraft) {
        if (isSelecting || StruggleHUDManager.isActive() || minecraft.screen != null) return false;
        return !ModKeyBinds.CHANGE_POSITION.isDown();
    }

    private static PlayerRestraintPart previous(PlayerRestraintPart part) {
        PlayerRestraintPart[] values = PlayerRestraintPart.values();
        return values[(part.ordinal() - 1 + values.length) % values.length];
    }

    private static void drawFloatingText(GuiGraphics guiGraphics, Font font, Component text, int x, int y, float alpha) {
        int textWidth = font.width(text);
        int alphaInt = (int) (alpha * 255);
        int bgColor = ((int) (alpha * 0x80) << 24);
        int textColor = (alphaInt << 24) | 0xFFFFFF;

        guiGraphics.fill(x - 4, y - 4, x + textWidth + 4, y + 9 + 4, bgColor);
        guiGraphics.drawString(font, text, x, y, textColor, false);
    }
}