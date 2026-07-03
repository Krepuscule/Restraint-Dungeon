package com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.DataComponentsUtils;
import com.twi.restraint_dungeon.network.payload.restraints_packet.restraints.SyncMiRaiTechSuitPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;

@OnlyIn(Dist.CLIENT)
public class MiraiTechRemoteGUI extends Screen {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui.png");
    private static final ResourceLocation ALT_FONT = ResourceLocation.fromNamespaceAndPath("minecraft", "alt");

    private static final ResourceLocation ICON_BOUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_bound.png");
    private static final ResourceLocation ICON_UNBOUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_unbound.png");
    private static final ResourceLocation ICON_LOCK = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_lock.png");
    private static final ResourceLocation ICON_UNLOCK = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_unlock.png");
    private static final ResourceLocation ICON_AROUSED_ON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_aroused_on.png");
    private static final ResourceLocation ICON_AROUSED_OFF = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_aroused_off.png");
    private static final ResourceLocation ICON_DENY_ON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_aroused_deny_on.png");
    private static final ResourceLocation ICON_DENY_OFF = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_techgui_aroused_deny_off.png");

    private final ResourceLocation[] moduleIcons = {
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_tech_glass.png"),
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_tech_mask.png"),
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_tech_sleeves.png"),
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_tech_mitten.png"),
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_tech_suit.png"),
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/mirai_tech_suit_controller/mirai_tech_boot.png")
    };

    public static final String[] PART_MAPPING = {
            PlayerRestraintPart.restraint_blindfold.toString(),
            PlayerRestraintPart.restraint_gag.toString(),
            PlayerRestraintPart.restraint_arms_bind.toString(),
            PlayerRestraintPart.restraint_hands_bind.toString(),
            PlayerRestraintPart.restraint_body_bind.toString(),
            PlayerRestraintPart.restraint_legs_bind.toString()
    };

    private final int imgW = 200, imgH = 280;
    private int left, top;
    private final ItemStack controllerStack;
    private final UUID pairID;

    private LivingEntity target = null;
    private ItemStack targetSuitStack = ItemStack.EMPTY;

    private Component warningMsg = null;
    private int warningTimer = 0;
    private static final int WARNING_DURATION = 100;

    public MiraiTechRemoteGUI(ItemStack controller, UUID pairID) {
        super(Component.literal("Hi-Tech Remote"));
        this.controllerStack = controller;
        this.pairID = pairID;
    }

    @Override
    protected void init() {
        this.left = (this.width - imgW) / 2;
        this.top = (this.height - imgH) / 2;
        this.refreshConnection();
    }

    private void refreshConnection() {
        this.target = null;
        this.targetSuitStack = ItemStack.EMPTY;
        if (this.minecraft != null && this.minecraft.level != null) {
            for (Entity entity : this.minecraft.level.entitiesForRendering()) {
                if (entity instanceof LivingEntity livingEntity) {
                    for (ItemStack stack : getAllPartRestraint(livingEntity, PlayerRestraintPart.restraint_body_bind)) {
                        if (stack.getItem() instanceof MiraiTechSuitItem suit) {
                            if (pairID.equals(suit.getPairingID(stack))) {
                                this.target = livingEntity;
                                this.targetSuitStack = stack;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void renderTransparentBackground(@NotNull GuiGraphics guiGraphics) {}

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
//        this.renderBackground(graphics, mouseX, mouseY, partial);
        this.refreshConnection();
        graphics.blit(BG, left, top, 0, 0, imgW, imgH, 200, 280);

        float cX = left + (imgW * 0.20f);
        float cY = top + (imgH * 0.1f);

        renderTargetInfo(graphics, left + (imgW * 0.15f), cY);
        renderSGAID(graphics, (int) (left + (imgW * 0.15f)), (int) cY + 12);

        int entryH = 22;
        float listStartY = top + (imgH / 2.0f) - ((6 * entryH) / 2.0f);

        Map<String, Boolean> modules = DataComponentsUtils.getMiraiModules(targetSuitStack);

        for (int i = 0; i < 6; i++) {
            float curY = listStartY + (i * entryH);
            String nbtKey = MiraiTechSuitItem.MODULE_KEYS[i];
            String partMappingKey = PART_MAPPING[i];

            ItemStack partStack = ItemStack.EMPTY;
            boolean isPresent = false;
            if (target != null && !targetSuitStack.isEmpty()) {
                partStack = ((MiraiTechSuitItem) targetSuitStack.getItem()).getMiRaiTechPartItem(target, partMappingKey);
                isPresent = !partStack.isEmpty();
            }

            boolean active = modules.getOrDefault(nbtKey, false);
            boolean locked = modules.getOrDefault(nbtKey + MiraiTechSuitItem.SUFFIX_LOCKED, false);

            MutableComponent statusTxt;
            int themeColor;

            if (!isPresent) {
                statusTxt = Component.translatable("item.restraint_dungeon.gui.mirai_tech_suit.status.offline");
                themeColor = 0xFF5555;
            } else if (active) {
                statusTxt = Component.translatable("item.restraint_dungeon.gui.mirai_tech_suit.status.active");
                themeColor = 0x55FF55;
            } else if (locked) {
                statusTxt = Component.translatable("item.restraint_dungeon.gui.mirai_tech_suit.status.locked");
                themeColor = 0x55FFFF;
            } else {
                statusTxt = Component.translatable("item.restraint_dungeon.gui.mirai_tech_suit.status.online");
                themeColor = 0xAAAAAA;
            }

            graphics.pose().pushPose();
            graphics.pose().translate(cX - 5, curY + 6, 0);
            graphics.pose().scale(0.6f, 0.6f, 1f);
            int txtWidth = font.width(statusTxt);
            graphics.drawString(font, statusTxt, -txtWidth, 0, themeColor, false);
            graphics.pose().popPose();

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, isPresent ? 1.0f : 0.3f);
            graphics.blit(moduleIcons[i], (int) cX, (int) curY, 0, 0, 16, 16, 16, 16);

            if (isPresent) {
                Component displayName = partStack.getHoverName();
                graphics.pose().pushPose();
                graphics.pose().translate(cX + 20, curY + 6, 0);
                graphics.pose().scale(0.7f, 0.7f, 1f);

                int maxNameWidth = (int) (imgW * 0.50f);
                String rawName = displayName.getString();
                String clippedName = font.plainSubstrByWidth(rawName, maxNameWidth);
                if (rawName.length() > clippedName.length()) clippedName += "..";

                graphics.drawString(font, clippedName, 0, 0, themeColor, false);
                graphics.pose().popPose();
            }

            int bX1 = (int) (left + imgW * 0.75f);
            int bX2 = (int) (left + imgW * 0.85f);
            float btnAlpha = isPresent ? 1.0f : 0.2f;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, btnAlpha);
            graphics.blit(active ? ICON_BOUND : ICON_UNBOUND, bX1, (int) curY, 0, 0, 16, 16, 16, 16);
            graphics.blit((locked || active) ? ICON_LOCK : ICON_UNLOCK, bX2, (int) curY, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        renderWarning(graphics);

        if (target != null && !targetSuitStack.isEmpty()) {
            boolean aroused = Boolean.TRUE.equals(DataComponentsUtils.getArousedMode(targetSuitStack));
            boolean deny = Boolean.TRUE.equals(DataComponentsUtils.getDenyMode(targetSuitStack));

            int footerY = top + (int) (imgH * 0.85f);
            int footerX1 = left + (int) (imgW * 0.70f);
            int footerX2 = left + (int) (imgW * 0.82f);

            graphics.blit(aroused ? ICON_AROUSED_ON : ICON_AROUSED_OFF, footerX1, footerY, 0, 0, 16, 16, 16, 16);
            graphics.blit(deny ? ICON_DENY_ON : ICON_DENY_OFF, footerX2, footerY, 0, 0, 16, 16, 16, 16);
        }else{
            int footerY = top + (int) (imgH * 0.85f);
            int footerX1 = left + (int) (imgW * 0.70f);
            int footerX2 = left + (int) (imgW * 0.82f);

            graphics.blit(ICON_AROUSED_OFF, footerX1, footerY, 0, 0, 16, 16, 16, 16);
            graphics.blit(ICON_DENY_OFF, footerX2, footerY, 0, 0, 16, 16, 16, 16);
        }
        super.render(graphics, mouseX, mouseY, partial);
    }

    private void renderTargetInfo(GuiGraphics graphics, float cX, float cY) {
        graphics.pose().pushPose();
        graphics.pose().translate(cX, cY, 0);
        graphics.pose().scale(0.8f, 0.8f, 1f);
        MutableComponent label = Component.translatable("item.restraint_dungeon.gui.mirai_tech_suit.target_label");
        graphics.drawString(font, label, 0, 0, 0x44FF44, false);

        String name = (target != null) ? target.getName().getString() : Component.translatable("item.restraint_dungeon.gui.mirai_tech_suit.cant_find").getString();
        graphics.drawString(font, name, font.width(label) + 5, 0, (target != null) ? 0x44FF44 : 0xFF4444, false);
        graphics.pose().popPose();
    }

    private void renderSGAID(GuiGraphics g, int x, int y) {
        if (pairID == null) return;

        String raw = pairID.toString().substring(0, 8).toUpperCase();
        StringBuilder sga = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (Character.isDigit(c)) sga.append((char) ('G' + (c - '0')));
            else if (c != '-') sga.append(c);
        }

        long t = System.currentTimeMillis() / 50;
        double baseWave = Math.sin(t * 0.1) * 20 + 235;
        int wave = (int) baseWave;
        int color = (100 << 16) | (wave << 8) | 255;

        g.drawString(font, "ID: ", x, y, 0xAAAAAA, false);
        Style magicStyle = Style.EMPTY.withFont(ALT_FONT).withColor(TextColor.fromRgb(color));
        g.drawString(font, Component.literal(sga.toString()).withStyle(magicStyle), x + 20, y, 0xFFFFFF, false);
    }

    private void renderWarning(GuiGraphics graphics) {
        if (warningMsg != null && warningTimer > 0) {
            float alpha = warningTimer > 20 ? 1.0f : (warningTimer / 20.0f);
            int color = (Math.max(4, (int) (alpha * 255)) << 24) | 0xFF4444;
            int textW = font.width(warningMsg);
            graphics.drawString(font, warningMsg, left + (imgW - textW) / 2, top + imgH - 45, color, false);
            warningTimer--;
        } else {
            warningMsg = null;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        this.refreshConnection();
        int entryH = 22;
        float listStartY = top + (imgH / 2.0f) - ((6 * entryH) / 2.0f);

        for (int i = 0; i < 6; i++) {
            float curY = listStartY + (i * entryH);
            boolean isOnline = false;
            if (target != null && !targetSuitStack.isEmpty()) {
                isOnline = !((MiraiTechSuitItem) targetSuitStack.getItem()).getMiRaiTechPartItem(target, PART_MAPPING[i]).isEmpty();
            }

            int bX1 = (int) (left + imgW * 0.75f);
            int bX2 = (int) (left + imgW * 0.85f);
            if (isOnline && my >= curY && my < curY + 16) {
                if (mx >= bX1 && mx < bX1 + 16) {
                    handleButtonClick(i, true);
                    return true;
                } else if (mx >= bX2 && mx < bX2 + 16) {
                    handleButtonClick(i, false);
                    return true;
                }
            }
        }

        int footerY = top + (int) (imgH * 0.85f);
        int footerX1 = left + (int) (imgW * 0.70f);
        int footerX2 = left + (int) (imgW * 0.82f);

        if (target != null && !targetSuitStack.isEmpty() && my >= footerY && my < footerY + 16) {
            if (mx >= footerX1 && mx < footerX1 + 16) {
                handleSpecialButtonClick(MiraiTechSuitItem.KEY_AROUSED);
                return true;
            } else if (mx >= footerX2 && mx < footerX2 + 16) {
                handleSpecialButtonClick(MiraiTechSuitItem.KEY_DENY);
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private void handleButtonClick(int index, boolean isActivationBtn) {
        if (target == null || targetSuitStack.isEmpty()) return;

        Map<String, Boolean> modules = DataComponentsUtils.getMiraiModules(targetSuitStack);
        String nbtKey = MiraiTechSuitItem.MODULE_KEYS[index];
        boolean currentActive = modules.getOrDefault(nbtKey, false);

        if (!isActivationBtn && currentActive) {
            this.warningMsg = Component.translatable("item.restraint_dungeon.gui.mirai_tech_suit.need_deactive_first");
            this.warningTimer = WARNING_DURATION;
            return;
        }

        // 发送同步包
        PacketDistributor.sendToServer(new SyncMiRaiTechSuitPayload(target.getUUID(), index, isActivationBtn));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void handleSpecialButtonClick(String componentKey) {
        if (target == null || targetSuitStack.isEmpty()) return;

        int specialIndex = componentKey.equals(MiraiTechSuitItem.KEY_AROUSED) ? 100 : 101;
        boolean currentState = componentKey.equals(MiraiTechSuitItem.KEY_AROUSED) ?
                Boolean.TRUE.equals(DataComponentsUtils.getArousedMode(targetSuitStack)) :
                Boolean.TRUE.equals(DataComponentsUtils.getDenyMode(targetSuitStack));

        PacketDistributor.sendToServer(new SyncMiRaiTechSuitPayload(target.getUUID(), specialIndex, !currentState));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
    }

    public void updateRemoteState(Player player, UUID uuid, Map<String, Boolean> modules, boolean aroused, boolean deny) {
        if (target != null && target.getUUID().equals(uuid)) {

            DataComponentsUtils.updateMiraiModules(player,targetSuitStack,modules);
            DataComponentsUtils.updateArousedMode(player,targetSuitStack,aroused);
            DataComponentsUtils.updateDenyMode(player,targetSuitStack,deny);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
