package com.twi.restraint_dungeon.mixin;

import com.twi.restraint_dungeon.utils.mod_utils.restraint.GagUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenGag;

@Mixin(PlayerList.class)
public abstract class MixinSendChatMessage {

    @Inject(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onBroadcastChatMessage(PlayerChatMessage pMessage, ServerPlayer pSender, ChatType.Bound pBoundChatType, CallbackInfo ci) {

        if (isBeenGag(pSender)) {
            ci.cancel(); // 取消原版的默认全服广播


            double range = GagUtils.getChatLimitedRange(pSender);
            double rangeSq = range * range;

            OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(pMessage);


            MinecraftServer server = pSender.getServer();
            if (server == null) return;

            for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
                // 距离与维度判断
                boolean isSameDimension = recipient.level().dimension().equals(pSender.level().dimension());
                boolean isWithinRange = recipient.distanceToSqr(pSender) <= rangeSq;

                if (recipient == pSender || (isSameDimension && isWithinRange)) {
                    recipient.sendChatMessage(outgoingchatmessage,false, pBoundChatType);
                }
            }
        }
    }
}
