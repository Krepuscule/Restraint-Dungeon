package com.twi.restraint_dungeon.event.mod_event.kidnap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_kidnap.PlayerKidnapActionPayload;
import com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class PlayerKidnapClientEvent {
    private static final ResourceLocation EMPTY_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/empty_progress_bar.png");
    private static final ResourceLocation YELLOW_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/yellow_progress_bar.png");
    private static final ResourceLocation RED_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/red_progress_bar.png");

    // 进度条参数
    private static final int BAR_WIDTH = 192;
    private static final int BAR_HEIGHT = 16;
    private static final int BAR_X_OFFSET = 0; // 水平偏移
    private static final int BAR_Y_OFFSET = 50; // 垂直偏移（负值向上移动）

    // 横向间距：图标右边缘距离进度条左边缘的距离
    private static final int HORIZONTAL_SPACING = 4;
    // 纵向偏移：图标中心相对于进度条中心的偏移
    private static final int VERTICAL_OFFSET = -10;

    // 图标固定尺寸
    private static final int ICON_BOX_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int BADGE_BOX_SIZE = 10;
    private static final int BADGE_SIZE = 8;
    private static final int BORDER_SIZE = 1;

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;

        if (!(mc.player.getMainHandItem().getItem() instanceof RestraintItem)) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity target) {

                // TODO:完成NPC系统后修改
                if(!(target instanceof Player)) return;
//                if(!(target instanceof Player) || !(target instanceof BaseNPCEntity)) return;

                PacketDistributor.sendToServer(new PlayerKidnapActionPayload(target.getId(), true));
            }
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            PacketDistributor.sendToServer(new PlayerKidnapActionPayload(-1, false));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (KidnapUtils.isKidnappingActive(mc.player) && KidnapUtils.isKidnapper(mc.player)) {
            UUID targetUUID = KidnapUtils.getPartnerUUID(mc.player);
            boolean stillLooking = false;

            if (mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity().getUUID().equals(targetUUID)) {
                stillLooking = true;
            }

            if (!stillLooking || !mc.options.keyUse.isDown()) {
                PacketDistributor.sendToServer(new PlayerKidnapActionPayload(-1, false));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderProgressBar(RenderGuiEvent.Pre event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!KidnapUtils.isKidnappingActive(mc.player)) return;

        float progress = KidnapUtils.getKidnapProgress(mc.player);
        if (progress <= 0) return;

        GuiGraphics g = event.getGuiGraphics();
        boolean isBinder = KidnapUtils.isKidnapper(mc.player);

        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();

        int barX = (screenWidth - BAR_WIDTH) / 2 + BAR_X_OFFSET;
        int barY = screenHeight / 2 + BAR_Y_OFFSET;

        int itemIconX = barX - HORIZONTAL_SPACING - ICON_BOX_SIZE;
        int itemIconY = (barY + BAR_HEIGHT / 2) + VERTICAL_OFFSET - (ICON_BOX_SIZE / 2);

        int badgeX = (itemIconX + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);
        int badgeY = (itemIconY + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);

        int bgColor = 0x77000000;
        int bgPadding = 4;

        int itemBgMinX = itemIconX - bgPadding;
        int itemBgMaxX = Math.max(itemIconX + ICON_BOX_SIZE, badgeX + BADGE_BOX_SIZE) + bgPadding;
        int itemBgMinY = itemIconY - bgPadding;
        int itemBgMaxY = Math.max(itemIconY + ICON_BOX_SIZE, badgeY + BADGE_BOX_SIZE) + bgPadding;
        g.fill(itemBgMinX, itemBgMinY, itemBgMaxX, itemBgMaxY, bgColor);

        g.fill(barX - bgPadding, barY - bgPadding, barX + BAR_WIDTH + bgPadding, barY + BAR_HEIGHT + bgPadding, bgColor);

        renderIconBox(g, itemIconX, itemIconY, ICON_BOX_SIZE, BORDER_SIZE);
        ItemStack stack = KidnapUtils.getKidnappingItem(mc.player);
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof RestraintItem ri) {
                g.renderItem(stack, itemIconX + (ICON_BOX_SIZE - ICON_SIZE) / 2, itemIconY + (ICON_BOX_SIZE - ICON_SIZE) / 2);
            }
        }

        // --- 4. 渲染部位角标组件 ---
        PlayerRestraintPart part = KidnapUtils.getKidnapPart(mc.player);
        renderIconBox(g, badgeX, badgeY, BADGE_BOX_SIZE, BORDER_SIZE);

        ResourceLocation partTex = ResourceLocation.fromNamespaceAndPath(MODID,
                "textures/gui/icon/" + part.toString().toLowerCase() + ".png");

        int pX = badgeX + (BADGE_BOX_SIZE - BADGE_SIZE) / 2;
        int pY = badgeY + (BADGE_BOX_SIZE - BADGE_SIZE) / 2;

        RenderSystem.enableBlend();
        g.blit(partTex, pX, pY, 0, 0, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE);
        RenderSystem.disableBlend();

        // --- 5. 渲染进度条纹理 ---
        g.blit(EMPTY_PROGRESS_BAR, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        ResourceLocation fillTex = isBinder ? YELLOW_PROGRESS_BAR : RED_PROGRESS_BAR;
        int filledWidth = (int) (BAR_WIDTH * (Mth.clamp(progress, 0, 100) / 100f));
        if (filledWidth > 0) {
            g.blit(fillTex, barX, barY, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }
    }

    /**
     * 辅助方法：渲染带有灰色边框和深色背景的图标框
     */
    private static void renderIconBox(GuiGraphics graphics, int x, int y, int size, int border) {
        // 灰色边框层
        graphics.fill(x - border, y - border, x + size + border, y + size + border, 0xFF888888);
        // 深色内部背景
        graphics.fill(x, y, x + size, y + size, 0xFF222222);
    }
}