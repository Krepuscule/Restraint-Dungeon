package com.twi.restraint_dungeon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.network.payload.player_restraint.ServerUpdatePlayerOptionsPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;

@OnlyIn(Dist.CLIENT)
public class PlayerRestraintOptionsMenu extends Screen {
    
    private final Player targetPlayer;
    private static final float MIN_VALUE = -4.0F;
    private static final float MAX_VALUE = 4.0F;

    public PlayerRestraintOptionsMenu(Player player) {
        super(Component.translatable("gui." + MODID + ".title.player_options_config"));
        this.targetPlayer = player;
    }

    @Override
    protected void init() {
        super.init();


        int rightAreaX = this.width / 2 + 30;
        int startY = this.height / 2 - 65;
        int sliderWidth = 160;
        int sliderHeight = 20;
        int spacing = 35;

        float currentBlindfold = getRenderOffset(targetPlayer, PlayerRestraintPart.restraint_blindfold);
        this.addRenderableWidget(new OffsetSlider(
                rightAreaX, startY, sliderWidth, sliderHeight,
                Component.translatable("part." + MODID + ".restraint_blindfold"),
                currentBlindfold,
                val -> updateOffset(PlayerRestraintPart.restraint_blindfold, val)
        ));

        float currentGag = getRenderOffset(targetPlayer, PlayerRestraintPart.restraint_gag);
        this.addRenderableWidget(new OffsetSlider(
                rightAreaX, startY + spacing, sliderWidth, sliderHeight,
                Component.translatable("part." + MODID + ".restraint_gag"),
                currentGag,
                val -> updateOffset(PlayerRestraintPart.restraint_gag, val)
        ));

        int checkboxY = startY + (spacing * 2);

        boolean initiallySelected = canOpenTargetInventory(targetPlayer);

        Checkbox allowInventoryCheckbox = Checkbox.builder(
                        Component.translatable("gui." + MODID + ".label.allow_open_inventory"),
                        this.font
                )
                .pos(rightAreaX, checkboxY)
                .selected(initiallySelected)
                .onValueChange((checkbox, selected) -> {
                    this.updateInventoryPermission(selected);
                })
                .build();

        this.addRenderableWidget(allowInventoryCheckbox);
    }


    private void updateOffset(PlayerRestraintPart part, float newValue) {

        setRenderOffset(targetPlayer, part, newValue);
        if(part == PlayerRestraintPart.restraint_blindfold){
            this.saveAndSync(newValue,
                    getRenderOffset(targetPlayer,PlayerRestraintPart.restraint_gag),
                    canOpenTargetInventory(targetPlayer));
        }else if(part == PlayerRestraintPart.restraint_gag){
            this.saveAndSync(getRenderOffset(targetPlayer,PlayerRestraintPart.restraint_blindfold),
                    newValue,
                    canOpenTargetInventory(targetPlayer));
        }
    }


    private void updateInventoryPermission(boolean canOpen) {

        setCanOpenTargetInventory(targetPlayer, canOpen);
        this.saveAndSync(getRenderOffset(targetPlayer,PlayerRestraintPart.restraint_blindfold),
                getRenderOffset(targetPlayer,PlayerRestraintPart.restraint_gag),
                canOpen);
    }


    private void saveAndSync(float blindfoldOffset, float gagOffset,boolean canOpenInventory) {
        ServerUpdatePlayerOptionsPayload packet = new ServerUpdatePlayerOptionsPayload(blindfoldOffset, gagOffset, canOpenInventory);
        PacketDistributor.sendToServer(packet);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);


        if (this.targetPlayer != null) {
            int modelX = this.width / 4 + 20;
            int modelY = this.height / 2 + 60;

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    modelX - 50, modelY - 130,
                    modelX + 50, modelY + 20,
                    60, 0.0F,
                    (float) mouseX, (float) mouseY,
                    this.targetPlayer
            );
        }

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void renderTransparentBackground(@NotNull GuiGraphics guiGraphics) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    @OnlyIn(Dist.CLIENT)
    private static class OffsetSlider extends AbstractSliderButton {
        private final Component prefix;
        private final java.util.function.Consumer<Float> onChange;

        public OffsetSlider(int x, int y, int width, int height, Component prefix, float initialValue, java.util.function.Consumer<Float> onChange) {
            super(x, y, width, height, Component.empty(), (double) (initialValue - MIN_VALUE) / (MAX_VALUE - MIN_VALUE));
            this.prefix = prefix;
            this.onChange = onChange;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            float actualValue = MIN_VALUE + (float) this.value * (MAX_VALUE - MIN_VALUE);
            actualValue = Math.round(actualValue * 10.0F) / 10.0F;

            String sign = actualValue > 0 ? "+" : "";
            this.setMessage(Component.literal(this.prefix.getString() + ": " + sign + String.format(Locale.ROOT, "%.1f", actualValue)));
        }

        @Override
        protected void applyValue() {
            float actualValue = MIN_VALUE + (float) this.value * (MAX_VALUE - MIN_VALUE);
            actualValue = Math.round(actualValue * 10.0F) / 10.0F;
            this.onChange.accept(actualValue);
        }
    }
}