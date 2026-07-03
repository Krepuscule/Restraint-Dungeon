package com.twi.restraint_dungeon.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isBeingCarried;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSetOverlayMessage(Component component, boolean animate, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if(isBeingCarried(player) || isRidingRestraintDevice(player)){
            if (component != null) {
                if (component.getContents() instanceof TranslatableContents translatable) {

                    if ("mount.onboard".equals(translatable.getKey())) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}