package com.twi.restraint_dungeon.event.mod_event.release;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_release.PlayerReleaseActionPayload;
import com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils;
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
public class PlayerReleaseClientEvent {

    private static final ResourceLocation EMPTY_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/empty_progress_bar.png");
    private static final ResourceLocation GREEN_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/green_progress_bar.png");

    private static final int BAR_WIDTH = 192;
    private static final int BAR_HEIGHT = 16;
    private static final int BAR_Y_OFFSET = 75;

    // 布局微调变量
    private static final int HORIZONTAL_SPACING = 4;
    private static final int VERTICAL_OFFSET = -10;

    private static final int ICON_BOX_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int BADGE_BOX_SIZE = 10;
    private static final int BADGE_SIZE = 8;
    private static final int BORDER_SIZE = 1;
    private static final int ICON_BORDER_COLOR = 0xFF888888;

    private static final int TOOL_HORIZONTAL_OFFSET = 0; // 相对于图标框水平对齐
    private static final int TOOL_VERTICAL_SPACING = 2;  // 工具图标框距离上方图标框的间距
    private static final int TOOL_BORDER_COLOR = 0xFF44FF44; // 绿色边框
    private static final int TOOL_SIZE = 16;

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        if(mc.screen != null) return;

        if(!mc.player.getMainHandItem().isEmpty()) return;
        // TODO:完成释放工具后修改

//        if(!mc.player.getMainHandItem().isEmpty() || mc.player.getMainHandItem().getItem() instanceof ReleaseToolItem) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity target) {

                // TODO:完成NPC系统后修改
                if(!(target instanceof Player)) return;
//                if(!(target instanceof Player) || !(target instanceof BaseNPCEntity)) return;

                PacketDistributor.sendToServer(new PlayerReleaseActionPayload(target.getId(), true));
            }
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            PacketDistributor.sendToServer(new PlayerReleaseActionPayload(-1, false));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (ReleaseUtils.isReleaseActive(mc.player) && ReleaseUtils.isReleaser(mc.player)) {
            UUID targetUUID = ReleaseUtils.getPartnerUUID(mc.player);
            boolean shouldStop = true;

            if (mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity().getUUID().equals(targetUUID)) {
                if (mc.options.keyUse.isDown()) {
                    shouldStop = false;
                }
            }

            if (shouldStop) {
                PacketDistributor.sendToServer(new PlayerReleaseActionPayload(-1, false));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderProgressBar(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        if (!ReleaseUtils.isReleaseActive(mc.player)) return;

        float progress = ReleaseUtils.getReleaseProgress(mc.player);
        if(progress <= 0) return;

        GuiGraphics g = event.getGuiGraphics();
        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();

        // --- 计算基准位置 ---
        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = screenHeight / 2 + BAR_Y_OFFSET;

        int itemIconX = barX - HORIZONTAL_SPACING - ICON_BOX_SIZE;
        int itemIconY = (barY + BAR_HEIGHT / 2) + VERTICAL_OFFSET - (ICON_BOX_SIZE / 2);

        int badgeX = (itemIconX + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);
        int badgeY = (itemIconY + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);

        int bgColor = 0x77000000;
        int padding = 4;

        int toolX = itemIconX + TOOL_HORIZONTAL_OFFSET;
        int toolY = itemIconY + ICON_BOX_SIZE + TOOL_VERTICAL_SPACING;

        // --- 渲染背景填充 ---
        // 进度条背景
        g.fill(barX - padding, barY - padding, barX + BAR_WIDTH + padding, barY + BAR_HEIGHT + padding, bgColor);

        int minX = Math.min(itemIconX, toolX) - padding;
        int maxX = Math.max(Math.max(itemIconX + ICON_BOX_SIZE, badgeX + BADGE_BOX_SIZE), toolX + ICON_BOX_SIZE) + padding;
        int minY = itemIconY - padding;
        int maxY = toolY + ICON_BOX_SIZE + padding;
        g.fill(minX, minY, maxX, maxY, bgColor);

        // --- 渲染物品图标组件 ---
        renderIconBox(g, itemIconX, itemIconY, ICON_BOX_SIZE, BORDER_SIZE,ICON_BORDER_COLOR);
        ItemStack item = ReleaseUtils.getReleasingItem(mc.player);
        if (!item.isEmpty()) {
            if (item.getItem() instanceof RestraintItem ri) {
                g.renderItem(item, itemIconX + (ICON_BOX_SIZE - ICON_SIZE) / 2, itemIconY + (ICON_BOX_SIZE - ICON_SIZE) / 2);
            }
        }


        // --- 渲染部位角标组件 ---
        PlayerRestraintPart part = ReleaseUtils.getReleasingPart(mc.player);
        renderIconBox(g, badgeX, badgeY, BADGE_BOX_SIZE, BORDER_SIZE,ICON_BORDER_COLOR);

        // --- 渲染工具图标组件 ---
        ItemStack toolStack = ReleaseUtils.getReleaseTool(mc.player);
        if (!toolStack.isEmpty()) {
            renderIconBox(g, toolX, toolY, ICON_BOX_SIZE, BORDER_SIZE, TOOL_BORDER_COLOR); // 绿色边框
            g.renderItem(toolStack, toolX + (ICON_BOX_SIZE - TOOL_SIZE) / 2, toolY + (ICON_BOX_SIZE - TOOL_SIZE) / 2);
        }

        ResourceLocation partTex = ResourceLocation.fromNamespaceAndPath(MODID,
                "textures/gui/icon/" + part.toString().toLowerCase() + ".png");

        int pX = badgeX + (BADGE_BOX_SIZE - BADGE_SIZE) / 2;
        int pY = badgeY + (BADGE_BOX_SIZE - BADGE_SIZE) / 2;

        RenderSystem.enableBlend();
        g.blit(partTex, pX, pY, 0, 0, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE);
        RenderSystem.disableBlend();

        // --- 渲染进度条纹理 ---
        g.blit(EMPTY_PROGRESS_BAR, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        int filledWidth = (int) (BAR_WIDTH * (Mth.clamp(progress, 0, 100) / 100f));
        if (filledWidth > 0) {
            g.blit(GREEN_PROGRESS_BAR, barX, barY, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }
    }

    private static void renderIconBox(GuiGraphics graphics, int x, int y, int size, int border, int borderColor) {
        graphics.fill(x - border, y - border, x + size + border, y + size + border, borderColor);
        graphics.fill(x, y, x + size, y + size, 0xFF222222);
    }
}