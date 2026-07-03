package com.twi.restraint_dungeon.event.mod_event.self_bondage;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.network.payload.player_self_bondage.SelfBondageActionPayload;
import com.twi.restraint_dungeon.utils.mod_utils.self_bondage.SelfBondageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class SelfBondageClientEvent {
    
    private static final ResourceLocation EMPTY_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/empty_progress_bar.png");
    private static final ResourceLocation YELLOW_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/yellow_progress_bar.png");


    private static final int BAR_WIDTH = 192;
    private static final int BAR_HEIGHT = 16;


    public static PlayerRestraintPart clientSelectedPart = null;
    public static ItemStack clientSelectedStack = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (SelfBondageUtils.isSelfBondaging(mc.player)) {
            ItemStack currentStack = mc.player.getMainHandItem();

            boolean itemChanged = clientSelectedStack.isEmpty() || !ItemStack.matches(clientSelectedStack, currentStack);


            Input playerInput = mc.player.input;
            boolean hasMovementInput = playerInput.left || playerInput.right || playerInput.forwardImpulse != 0.0 || playerInput.leftImpulse != 0.0
                    || playerInput.jumping || playerInput.shiftKeyDown;

            if (itemChanged || hasMovementInput) {
                triggerClientInterrupt();
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (SelfBondageUtils.isSelfBondaging(mc.player)) {

            triggerClientInterrupt();
        }
    }

    private static void triggerClientInterrupt() {
        PacketDistributor.sendToServer(new SelfBondageActionPayload(false, PlayerRestraintPart.restraint_blindfold.toString()));
        clearClientCache();
    }

    public static void clearClientCache() {
        clientSelectedPart = null;
        clientSelectedStack = ItemStack.EMPTY;
    }

    private static final int ICON_BOX_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int BADGE_BOX_SIZE = 10;
    private static final int BADGE_SIZE = 8;
    private static final int BORDER_SIZE = 1;
    private static final int HORIZONTAL_SPACING = 4;

    @SubscribeEvent
    public static void onRenderProgressBar(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!SelfBondageUtils.isSelfBondaging(mc.player)) return;
        float progress = SelfBondageUtils.getSelfBondageProgress(mc.player);
        if (progress <= 0) return;

        GuiGraphics g = event.getGuiGraphics();
        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();

        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = screenHeight / 2 + 50;

        int itemIconX = barX - HORIZONTAL_SPACING - ICON_BOX_SIZE;
        int itemIconY = (barY + BAR_HEIGHT / 2) - (ICON_BOX_SIZE / 2);

        int badgeX = (itemIconX + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);
        int badgeY = (itemIconY + ICON_BOX_SIZE) - (BADGE_BOX_SIZE / 2);

        int bgColor = 0x77000000;
        int bgPadding = 4;

        ItemStack stack = mc.player.getMainHandItem();
        boolean showItem = !stack.isEmpty() && (stack.getItem() instanceof RestraintItem || stack.getItem() instanceof RestraintToolItem);

        boolean isTool = !stack.isEmpty() && stack.getItem() instanceof RestraintToolItem;

        if (showItem) {
            int itemBgMinX = itemIconX - bgPadding;
            int itemBgMaxX = (isTool ? itemIconX + ICON_BOX_SIZE : Math.max(itemIconX + ICON_BOX_SIZE, badgeX + BADGE_BOX_SIZE)) + bgPadding;
            int itemBgMinY = itemIconY - bgPadding;
            int itemBgMaxY = (isTool ? itemIconY + ICON_BOX_SIZE : Math.max(itemIconY + ICON_BOX_SIZE, badgeY + BADGE_BOX_SIZE)) + bgPadding;

            g.fill(itemBgMinX, itemBgMinY, itemBgMaxX, itemBgMaxY, bgColor);
        }


        g.fill(barX - bgPadding, barY - bgPadding, barX + BAR_WIDTH + bgPadding, barY + BAR_HEIGHT + bgPadding, bgColor);

        if (showItem) {
            g.fill(itemIconX, itemIconY, itemIconX + ICON_BOX_SIZE, itemIconY + ICON_BOX_SIZE, 0xFF888888);
            g.fill(itemIconX + BORDER_SIZE, itemIconY + BORDER_SIZE, itemIconX + ICON_BOX_SIZE - BORDER_SIZE, itemIconY + ICON_BOX_SIZE - BORDER_SIZE, 0xFF222222);


            g.renderItem(stack, itemIconX + (ICON_BOX_SIZE - ICON_SIZE) / 2, itemIconY + (ICON_BOX_SIZE - ICON_SIZE) / 2);
        }


        if (clientSelectedPart != null && !isTool) {

            g.fill(badgeX, badgeY, badgeX + BADGE_BOX_SIZE, badgeY + BADGE_BOX_SIZE, 0xFF888888);
            g.fill(badgeX + BORDER_SIZE, badgeY + BORDER_SIZE, badgeX + BADGE_BOX_SIZE - BORDER_SIZE, badgeY + BADGE_BOX_SIZE - BORDER_SIZE, 0xFF222222);

            ResourceLocation partTex = ResourceLocation.fromNamespaceAndPath(MODID,
                    "textures/gui/icon/" + clientSelectedPart.toString().toLowerCase() + ".png");

            int pX = badgeX + (BADGE_BOX_SIZE - BADGE_SIZE) / 2;
            int pY = badgeY + (BADGE_BOX_SIZE - BADGE_SIZE) / 2;

            RenderSystem.enableBlend();
            g.blit(partTex, pX, pY, 0, 0, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE);
            RenderSystem.disableBlend();
        }

        Component tipText = Component.translatable("hud." + MODID + ".self_bondage_menu.self_binding").withStyle(ChatFormatting.GOLD);
        g.drawCenteredString(mc.font, tipText, screenWidth / 2, barY - 12, 0xFFFFFFFF);

        g.blit(EMPTY_BAR, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        int filledWidth = (int) (BAR_WIDTH * (Mth.clamp(progress, 0, 100) / 100f));
        if (filledWidth > 0) {
            g.blit(YELLOW_BAR, barX, barY, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }
    }
}