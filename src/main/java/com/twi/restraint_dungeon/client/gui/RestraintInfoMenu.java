package com.twi.restraint_dungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_restraint.RequestOpenTargetInventoryPayload;
import com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils;
import com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;

public class RestraintInfoMenu extends Screen {
    private final LivingEntity targetEntity;
    private final boolean isSelf;
    private final boolean isNPC;
    private final List<SlotLine> slotLines = new ArrayList<>();

    private static final double MAX_DISTANCE = 16.0;

    protected int leftPos;
    protected int topPos;

    private static final ResourceLocation EMPTY_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/empty_progress_bar.png");
    private static final ResourceLocation PINK_BAR = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/progress_bar/pleasant_progress_bar.png");
    private static final int BAR_WIDTH = 128;
    private static final int BAR_HEIGHT = 16;

    private static final PlayerRestraintPart[] RESTRAINT_PARTS = new PlayerRestraintPart[]{
            PlayerRestraintPart.restraint_blindfold,
            PlayerRestraintPart.restraint_gag,
            PlayerRestraintPart.restraint_collar,
            PlayerRestraintPart.restraint_body_bind,
            PlayerRestraintPart.restraint_connection,
            PlayerRestraintPart.restraint_arms_bind,
            PlayerRestraintPart.restraint_hands_bind,
            PlayerRestraintPart.restraint_legs_bind
    };

    private static final Component[] SLOT_NAMES = new Component[]{
            Component.translatable("part." + MODID + ".restraint_blindfold"),
            Component.translatable("part." + MODID + ".restraint_gag"),
            Component.translatable("part." + MODID + ".restraint_collar"),
            Component.translatable("part." + MODID + ".restraint_body_bind"),
            Component.translatable("part." + MODID + ".restraint_connection"),
            Component.translatable("part." + MODID + ".restraint_arms_bind"),
            Component.translatable("part." + MODID + ".restraint_hands_bind"),
            Component.translatable("part." + MODID + ".restraint_legs_bind")
    };

    private final List<StatusIcon> statusIcons = List.of(
            new StatusIcon(
                    entity -> ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/status_icon/blindfold_status.png"),
                    entity -> Component.translatable("gui.restraint_dungeon.restraint_menu.status.blindfolding").withStyle(ChatFormatting.DARK_RED),
                    RestraintUtils::isBeenBlindfold
            ),
            new StatusIcon(
                    this::getGagStatusTexture,
                    this::getGagStatusTooltips,
                    RestraintUtils::isBeenGag
            ),
            new StatusIcon(
                    entity -> ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/status_icon/arms_bind_status.png"),
                    entity -> Component.translatable("gui.restraint_dungeon.restraint_menu.status.binding_arms").withStyle(ChatFormatting.DARK_RED),
                    RestraintUtils::isBeenBindArms
            ),
            new StatusIcon(
                    entity -> ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/status_icon/hands_bind_status.png"),
                    entity -> Component.translatable("gui.restraint_dungeon.restraint_menu.status.binding_hands").withStyle(ChatFormatting.DARK_RED),
                    RestraintUtils::isBeenBindHands
            ),
            new StatusIcon(
                    entity -> ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/status_icon/legs_bind_status.png"),
                    entity -> Component.translatable("gui.restraint_dungeon.restraint_menu.status.binding_legs").withStyle(ChatFormatting.DARK_RED),
                    RestraintUtils::isBeenBindLegs
            ),
            new StatusIcon(
                    this::getThrillLevelStatusTexture,
                    this::getThrillLevelStatusTooltips,
                    entity -> getThrillLevel(entity) > 0
            ),
            new StatusIcon(
                    entity -> ResourceLocation.fromNamespaceAndPath(MODID,"textures/gui/screen/status_icon/on_device_status.png") ,
                    entity -> Component.translatable("gui." + MODID + ".restraint_menu.status.on_device").withStyle(ChatFormatting.DARK_RED),
                    RestraintDeviceUtils::isRidingRestraintDevice
            ),
            new StatusIcon(
                    entity -> ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/status_icon/cut_status.png"),
                    entity -> Component.translatable("gui." + MODID + ".restraint_menu.status.near_cut_tool").withStyle(ChatFormatting.GREEN),
                    StruggleUtils::isNearCutStrugglingState
            ),
            new StatusIcon(
                    entity -> ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/status_icon/hook_status.png"),
                    entity -> Component.translatable("gui." + MODID + ".restraint_menu.status.near_hook_tool").withStyle(ChatFormatting.GREEN),
                    StruggleUtils::isNearHookStrugglingState
            )


    );

    private static final ResourceLocation STACK_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/restraint_stack.png");
    private static final ResourceLocation LOCK_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/restraint_lock.png");

    public RestraintInfoMenu(LivingEntity targetEntity,boolean isSelf,boolean isNPC) {
        super(Component.translatable("gui.restraint_dungeon.title.restraint_info_menu"));
        this.targetEntity = targetEntity;
        this.isSelf = isSelf;
        this.isNPC = isNPC;
    }

    @Override
    protected void init() {
        this.leftPos = this.width / 3;
        this.topPos = this.height / 3 * 2;

        slotLines.clear();
        int slotStartX = this.width / 50;
        int slotStartY = this.height / 8;
        int lineSpacing = this.height / 10;

        for (int i = 0; i < RESTRAINT_PARTS.length; i++) {
            int yPos = slotStartY + i * lineSpacing;
            slotLines.add(new SlotLine(RESTRAINT_PARTS[i], SLOT_NAMES[i], slotStartX, yPos, this.width, this.height));
        }

        List<ItemStack> tools = RestraintToolsUtils.getAllRestraintTools(this.targetEntity);

        int buttonWidth = 80;
        int buttonHeight = 20;
        int buttonX = this.width - buttonWidth - 20;

        int baseButtonY = (int) (this.height * 0.75F) - (buttonHeight / 2);

        if (!tools.isEmpty()) {
            int toolBtnY = baseButtonY - buttonHeight - 5;

            this.addRenderableWidget(
                    Button.builder(
                                    Component.translatable("gui." + MODID + ".button.manage_tools"),
                                    button -> {
                                        Minecraft.getInstance().setScreen(new RestraintToolsConfigMenu(this.targetEntity, this.isSelf));
                                    }
                            )
                            .bounds(buttonX, toolBtnY, buttonWidth, buttonHeight)
                            .build()
            );
        }

        if(!isNPC){
            if(isSelf){
                this.addRenderableWidget(
                        Button.builder(
                                        Component.translatable("gui." + MODID + ".button.player_options"),
                                        button -> {
                                            Minecraft mc = Minecraft.getInstance();
                                            if (mc.player != null) {
                                                mc.setScreen(new PlayerRestraintOptionsMenu(mc.player));
                                            }
                                        }
                                )
                                .bounds(buttonX, baseButtonY, buttonWidth, buttonHeight)
                                .build()
                );
            }
            else{
                if(isBeenFullyBind(targetEntity) && targetEntity.getData(ModAttachments.PLAYER_OPTION).canOpenInventory()){
                    this.addRenderableWidget(
                            Button.builder(
                                            Component.translatable("gui." + MODID + ".button.open_target_inventory"),
                                            button -> {
                                                PacketDistributor.sendToServer(new RequestOpenTargetInventoryPayload(targetEntity.getUUID()));
                                                this.onClose();
                                            }
                                    )
                                    .bounds(buttonX, baseButtonY, buttonWidth, buttonHeight)
                                    .build()
                    );
                }
            }
        }

        super.init();
    }

    @Override
    public void tick() {
        super.tick();
        Player player = Minecraft.getInstance().player;

        if (targetEntity == null || !targetEntity.isAlive() || targetEntity.isRemoved()) {
            this.onClose();
            return;
        }

        if (player != null) {
            double distSqr = player.distanceToSqr(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
            if (distSqr > MAX_DISTANCE * MAX_DISTANCE) {
                this.onClose();
                return;
            }
        }

        updateSlotItems();
    }

    private void updateSlotItems() {
        for (SlotLine slotLine : slotLines) {
            slotLine.updateItems(getAllRestraintsByPart(this.targetEntity, slotLine.partEnum));
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        pGuiGraphics.fill(0, 0, this.width, this.height, 0x88000000);

        if (targetEntity != null) {
            int EntityModelX = this.leftPos + 40;
            int EntityModelY = this.topPos;

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    pGuiGraphics,
                    EntityModelX - 50, EntityModelY - 120,
                    EntityModelX + 50, EntityModelY + 30,
                    50, 0.0F,
                    (float)pMouseX, (float)pMouseY,
                    targetEntity
            );

            renderPleasantBar(pGuiGraphics);
            renderEntityStatusIcons(pGuiGraphics, pMouseX, pMouseY);
        }

        renderRestraintStacks(pGuiGraphics, pMouseX, pMouseY);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void renderTransparentBackground(GuiGraphics guiGraphics) {}

    private void renderRestraintStacks(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (SlotLine slotLine : slotLines) {
            slotLine.isStackTooltipActive = false;
        }
        for (SlotLine slotLine : slotLines) {
            boolean isHovered = slotLine.isMouseOverLine(mouseX, mouseY);
            slotLine.render(guiGraphics, mouseX, mouseY, this.font, isHovered);
        }
    }

    private void renderPleasantBar(GuiGraphics guiGraphics) {
        double pleasantValue = getPleasantValue(this.targetEntity);
        double drawValue = Mth.clamp(pleasantValue, 0, 100);
        int x = this.width - BAR_WIDTH + 2;
        int y = this.height - BAR_HEIGHT - 8;
        guiGraphics.blit(EMPTY_BAR, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        int progressWidth = (int) (BAR_WIDTH * (drawValue / 100.0f));
        if (progressWidth > 0) {
            guiGraphics.blit(PINK_BAR, x, y, 0, 0, progressWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }
        String text = String.format("%.1f%%", drawValue);
        guiGraphics.drawString(this.font, text, x + (BAR_WIDTH - this.font.width(text)) / 2, y - 10, 0xFFFFFF);
    }

    private void renderEntityStatusIcons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<StatusIcon> activeStatus = new ArrayList<>();
        for (StatusIcon icon : statusIcons) {
            if (icon.condition.test(targetEntity)) {
                activeStatus.add(icon);
            }
        }
        int iconSize = 12;
        int startX = this.leftPos - 15;
        int startY = this.topPos / 3 + 10;

        for (int i = 0; i < activeStatus.size(); i++) {
            StatusIcon status = activeStatus.get(i);
            int renderY = startY + i * (iconSize + 2);
            ResourceLocation tex = status.textureProvider.apply(targetEntity);
            guiGraphics.blit(tex, startX, renderY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            if (mouseX >= startX && mouseX <= startX + iconSize && mouseY >= renderY && mouseY <= renderY + iconSize) {
                guiGraphics.renderTooltip(this.font, status.tooltipProvider.apply(targetEntity), mouseX, mouseY);
            }
        }
    }

    private ResourceLocation getGagStatusTexture(LivingEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(MODID, isBeenHeavyGag(entity) ? "textures/gui/screen/status_icon/heavy_gag_status.png" : "textures/gui/screen/status_icon/gag_status.png");
    }

    private Component getGagStatusTooltips(LivingEntity entity) {
        String key = isBeenHeavyGag(entity) ? "gui.restraint_dungeon.restraint_menu.status.heavy_gagging" : "gui.restraint_dungeon.restraint_menu.status.gagging";
        return Component.translatable(key).withStyle(ChatFormatting.DARK_RED);
    }

    private ResourceLocation getThrillLevelStatusTexture(LivingEntity entity) {
        int level = Mth.clamp(getThrillLevel(entity), 1, 10);
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/status_icon/thrill_level/thrill_level_" + level + ".png");
    }

    private Component getThrillLevelStatusTooltips(LivingEntity entity) {
        return Component.translatable("gui.restraint_dungeon.restraint_menu.status.thrill_level").append(String.valueOf(getThrillLevel(entity))).withStyle(ChatFormatting.DARK_RED);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_TAB) {
            for (SlotLine slotLine : slotLines) {
                if (slotLine.isStackTooltipActive) {
                    slotLine.handleTabPress();
                    return true;
                }
            }
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override public boolean isPauseScreen() { return false; }

    // --- 内部辅助类 SlotLine ---
    private class SlotLine {
        public final PlayerRestraintPart partEnum;
        private final String slotId;
        private final Component slotName;
        private final int x, y, width, height;

        private List<ItemStack> items = new ArrayList<>();
        private ItemStack lastItem = ItemStack.EMPTY;
        private final ResourceLocation slotTexture;

        public boolean isStackTooltipActive = false;
        private int selectedItemIndex = -1;

        public SlotLine(PlayerRestraintPart part, Component slotName, int x, int y, int sw, int sh) {
            this.partEnum = part;
            this.slotId = part.toString();
            this.slotName = slotName;
            this.x = x;
            this.y = y;
            this.width = sw / 4;
            this.height = sh / 12;
            this.slotTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/" + slotId + ".png");
        }

        public void updateItems(List<ItemStack> newItems) {
            this.items = new ArrayList<>(newItems);
            this.lastItem = items.isEmpty() ? ItemStack.EMPTY : items.getLast();
            if (items.isEmpty()) {
                selectedItemIndex = -1;
            } else if (selectedItemIndex == -1 || selectedItemIndex >= items.size()) {
                selectedItemIndex = items.size() - 1;
            }
        }


        private List<Component> getConnectedInfoComponents(ItemStack stack, int index) {
            List<Component> tooltip = new ArrayList<>();
            List<ConnectingRestraint> connections =
                    RestraintUtils.getConnectingRestraintsInfo(targetEntity, partEnum, index);

            if (!connections.isEmpty()) {
                tooltip.add(Component.translatable("gui.restraint_dungeon.restraint_menu.connecting_items").withStyle(ChatFormatting.GOLD));
                for (ConnectingRestraint conn : connections) {
                    MutableComponent partName = Component.translatable("part.restraint_dungeon." + conn.sourcePart().name());

                    MutableComponent locationInfo = partName.copy();
                    int totalAtSource = getRestraintCount(targetEntity, conn.sourcePart());
                    if (totalAtSource > 1) {
                        locationInfo.append(" #").append(String.valueOf(conn.index() + 1));
                    }

                    tooltip.add(conn.stack().getHoverName().copy().withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY))
                            .append(locationInfo.withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
            return tooltip;
        }

        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font, boolean isHovered) {
            int backgroundColor = isHovered ? 0x40FFFFFF : 0x20000000;
            guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, backgroundColor);
            int borderColor = isHovered ? 0xFFC0C0C0 : 0x40000000;
            guiGraphics.renderOutline(x - 2, y - 2, width + 4, height + 4, borderColor);

            int iconYOffset = (height - 16) / 2;
            float iconBrightness = isHovered ? 1.0f : 0.7f;
            guiGraphics.setColor(iconBrightness, iconBrightness, iconBrightness, 1.0f);
            guiGraphics.blit(slotTexture, x + 20, y + iconYOffset, 0, 0, 16, 16, 16, 16);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (!lastItem.isEmpty()) {
                guiGraphics.renderItem(lastItem, x + 2, y + iconYOffset);
                boolean hasStack = items.size() > 1;
                int rightIconX = x + width - 18;

                if (lastItem.getItem() instanceof RestraintItem ri && !ri.getLockType(Minecraft.getInstance().player,lastItem).isEmpty()) {
                    guiGraphics.blit(LOCK_ICON, rightIconX, y + iconYOffset, 0, 0, 16, 16, 16, 16);
                } else if (hasStack) {
                    guiGraphics.blit(STACK_ICON, rightIconX, y + iconYOffset, 0, 0, 16, 16, 16, 16);
                }

                if (hasStack) {
                    String countText = "x" + items.size();
                    guiGraphics.drawString(font, countText, rightIconX - font.width(countText) - 2, y + iconYOffset + 4, 0xCCCCCC, false);
                }
            }

            if (isHovered) {
                boolean overRightIcon = mouseX >= x + width - 20 && mouseX <= x + width &&
                        mouseY >= y + iconYOffset && mouseY <= y + iconYOffset + 16;

                if (overRightIcon && items.size() > 1) {
                    this.isStackTooltipActive = true;
                    renderStackTooltip(guiGraphics, mouseX, mouseY, font);
                } else if (!lastItem.isEmpty()) {
                    renderItemDetailTooltip(guiGraphics, lastItem, mouseX, mouseY);
                }
            }
        }


        private void renderStackTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
            List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(Component.translatable("gui.restraint_dungeon.restraint_menu.stack").withStyle(ChatFormatting.GOLD));

            for (int i = items.size() - 1; i >= 0; i--) {
                ItemStack item = items.get(i);
                MutableComponent nameLine = item.getHoverName().copy();
                if (i == selectedItemIndex) {
                    nameLine = Component.literal("→ ").withStyle(ChatFormatting.YELLOW).append(nameLine.withStyle(ChatFormatting.UNDERLINE));

                } else {
                    nameLine = nameLine.withStyle(ChatFormatting.GRAY);

                }
                if(item.getItem() instanceof RestraintItem ri && !ri.getLockType(Minecraft.getInstance().player,item).isEmpty()) {
                    nameLine.append(" (")
                            .append(Component.translatable("gui.restraint_dungeon.restraint_menu.locked").withStyle(ChatFormatting.RED))
                            .append(")");
                }

                tooltipLines.add(nameLine);
            }

            tooltipLines.add(Component.empty());

            if (selectedItemIndex >= 0 && selectedItemIndex < items.size() && hasShiftDown()) {
                tooltipLines.add(Component.empty());
                addItemBasicTooltips(items.get(selectedItemIndex), tooltipLines);
            }

            if (!hasShiftDown()) {
                Style s = Style.EMPTY.withColor(0x777777);
                tooltipLines.add(Component.translatable("gui.restraint_dungeon.restraint_menu.stack_tab").withStyle(s));
                tooltipLines.add(Component.translatable("gui.restraint_dungeon.restraint_menu.stack_shift").withStyle(s));
            }
            guiGraphics.renderTooltip(font, tooltipLines, Optional.empty(), mouseX, mouseY);
        }



        private void renderItemDetailTooltip(GuiGraphics guiGraphics, ItemStack stack, int mouseX, int mouseY) {
            List<Component> tooltip = new ArrayList<>();
            addItemBasicTooltips(stack, tooltip);

            List<Component> connInfo = getConnectedInfoComponents(stack, items.size() - 1);
            if (!connInfo.isEmpty()) {
                tooltip.add(Component.empty());
                tooltip.addAll(connInfo);
            }

            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
        }


        private void addItemBasicTooltips(ItemStack stack, List<Component> lines) {
            Item.TooltipContext ctx = Item.TooltipContext.of(Minecraft.getInstance().level.registryAccess());
            lines.addAll(stack.getTooltipLines(ctx, Minecraft.getInstance().player, TooltipFlag.Default.NORMAL));

            if (stack.getItem() instanceof RestraintItem ri && !ri.getLockType(Minecraft.getInstance().player,stack).isEmpty()) {
                lines.add(Component.translatable("gui.restraint_dungeon.restraint_menu.locked").withStyle(ChatFormatting.RED));
            }
        }

        public void handleTabPress() {
            if (!items.isEmpty()) selectedItemIndex = (selectedItemIndex + 1) % items.size();
        }

        public boolean isMouseOverLine(int mouseX, int mouseY) {
            return mouseX >= x - 2 && mouseX <= x + width + 2 && mouseY >= y - 2 && mouseY <= y + height + 2;
        }
    }

    private record StatusIcon(Function<LivingEntity, ResourceLocation> textureProvider, Function<LivingEntity, Component> tooltipProvider, Predicate<LivingEntity> condition) {}
}
