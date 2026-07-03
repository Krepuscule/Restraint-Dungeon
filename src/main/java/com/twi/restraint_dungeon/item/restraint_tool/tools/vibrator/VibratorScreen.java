package com.twi.restraint_dungeon.item.restraint_tool.tools.vibrator;

import com.twi.restraint_dungeon.network.payload.restraints_packet.restraint_tool.RemoveRestraintToolPayload;
import com.twi.restraint_dungeon.network.payload.restraints_packet.restraint_tool.SyncVibeLevelPayload;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class VibratorScreen extends Screen {

    private static final ResourceLocation CONTROLLER_DEACTIVATE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/vibrator/vibrator_controller_deactivate.png");
    private static final ResourceLocation CONTROLLER_ACTIVATE_0_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/vibrator/vibrator_controller_activate_0.png");
    private static final ResourceLocation CONTROLLER_ACTIVATE_1_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/vibrator/vibrator_controller_activate_1.png");
    private static final ResourceLocation CONTROLLER_ACTIVATE_2_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/vibrator/vibrator_controller_activate_2.png");
    private static final ResourceLocation CONTROLLER_ACTIVATE_3_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/item_gui/vibrator/vibrator_controller_activate_3.png");

    private final int targetEntityId;
    private final int toolIndex;

    private final int imageWidth = 256;
    private final int imageHeight = 235;

    private int currentLevel = 0;
    private boolean isActivated = false;

    private static final double MAX_DISTANCE = 16.0;

    public VibratorScreen(Component title, int targetEntityId, int toolIndex) {
        super(title);
        this.targetEntityId = targetEntityId;
        this.toolIndex = toolIndex;
    }

    @Override
    protected void init() {
        super.init();
    }

    private boolean updateStateFromServer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        Entity target = mc.level.getEntity(this.targetEntityId);
        if (target instanceof LivingEntity livingEntity) {

            ItemStack stack;
            if (this.toolIndex == -1) {
                stack = livingEntity.getMainHandItem();
                if (!(stack.getItem() instanceof VibratorItem)) {
                    stack = livingEntity.getOffhandItem();
                }
            }
            else {
                stack = RestraintToolsUtils.getRestraintToolByIndex(livingEntity, this.toolIndex);
            }

            if (!stack.isEmpty() && stack.getItem() instanceof VibratorItem vibratorItem) {
                this.currentLevel = vibratorItem.getVibeLevel(stack);
                this.isActivated = vibratorItem.isActivated(stack);
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (mc.level == null) return;

        Entity target = mc.level.getEntity(this.targetEntityId);
        if (target == null || !target.isAlive() || target.isRemoved() || !updateStateFromServer()) {
            this.onClose();
            return;
        }

        if (player != null) {
            double distSqr = player.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (distSqr > MAX_DISTANCE * MAX_DISTANCE) {
                this.onClose();
                return;
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        updateStateFromServer();

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        if (this.isActivated) {
            ResourceLocation activeTex = switch (this.currentLevel) {
                case 1 -> CONTROLLER_ACTIVATE_1_TEXTURE;
                case 2 -> CONTROLLER_ACTIVATE_2_TEXTURE;
                case 3 -> CONTROLLER_ACTIVATE_3_TEXTURE;
                default -> CONTROLLER_ACTIVATE_0_TEXTURE;
            };
            graphics.blit(activeTex, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        } else {
            graphics.blit(CONTROLLER_DEACTIVATE_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void renderTransparentBackground(@NotNull GuiGraphics guiGraphics) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int leftPos = (this.width - this.imageWidth) / 2;
            int topPos = (this.height - this.imageHeight) / 2;

            double localX = mouseX - leftPos;
            double localY = mouseY - topPos;

            boolean isXInButtonRange = localX >= 110 && localX <= 146;
            if (isXInButtonRange) {

                if (!updateStateFromServer()) return false;

                // 减号按钮 (-)
                if (localY >= 48 && localY <= 84) {
                    if (this.currentLevel > 0) {
                        this.currentLevel--;
                        sendSyncPacket();
                        playClickSound();
                    }
                    return true;
                }

                // 电源/开关按钮 (O)
                if (localY >= 99 && localY <= 135) {
                    this.isActivated = !this.isActivated;
                    sendSyncPacket();
                    playClickSound();
                    return true;
                }

                // 加号按钮 (+)
                if (localY >= 150 && localY <= 186) {
                    if (this.currentLevel < 3) {
                        this.currentLevel++;
                        sendSyncPacket();
                        playClickSound();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendSyncPacket() {
        PacketDistributor.sendToServer(new SyncVibeLevelPayload(this.targetEntityId, this.toolIndex, this.currentLevel, this.isActivated));
    }

    private void playClickSound() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            SoundEvents.UI_BUTTON_CLICK, 1.0F
                    )
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}