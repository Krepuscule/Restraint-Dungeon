package com.twi.restraint_dungeon.event.mod_event.kidnap;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_struggle.InterruptStrugglePayload;
import com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getEntityTargetPart;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;

@EventBusSubscriber(modid = MODID)
public class PlayerKidnapServerEvent {
    private static final Map<UUID, Long> START_TIME_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer actionPlayer)) return;

        if (!KidnapUtils.isKidnappingActive(actionPlayer) || !KidnapUtils.isKidnapper(actionPlayer)) return;

        UUID targetUUID = KidnapUtils.getPartnerUUID(actionPlayer);
        Entity target = actionPlayer.serverLevel().getEntity(targetUUID);
        ItemStack stack = actionPlayer.getMainHandItem();
        PlayerRestraintPart part = KidnapUtils.getKidnapPart(actionPlayer);

        if (!(target instanceof LivingEntity livingTarget)) {
            stopKidnapping(actionPlayer);
            return;
        }

        // 判定条件检查
        KidnapUtils.BindResult result = KidnapUtils.targetCanBeBound(stack, livingTarget, actionPlayer,part);
        if (!result.canBind()) {
            actionPlayer.displayClientMessage(result.messageKey(), true);
            stopKidnapping(actionPlayer);
            return;
        }

        // 进度计算
        if (stack.getItem() instanceof RestraintItem item) {
            long startTime = START_TIME_MAP.getOrDefault(actionPlayer.getUUID(), actionPlayer.level().getGameTime());
            long elapsed = actionPlayer.level().getGameTime() - startTime;
            float totalTicks = item.getKidnapTime(actionPlayer,livingTarget,stack,part) / 50.0f;
            float progress = Math.min(100f, (elapsed / totalTicks) * 100f);


            KidnapUtils.updateProgress(actionPlayer, progress);
            if (livingTarget instanceof Player targetPlayer) {
                KidnapUtils.updateProgress(targetPlayer, progress);
            }

            if (progress >= 100f) {
                KidnapUtils.executeBind(actionPlayer, livingTarget, stack);
                stopKidnapping(actionPlayer);
            }
        }
    }

    public static void startKidnapping(ServerPlayer actionPlayer, LivingEntity target) {
        ItemStack stack = actionPlayer.getMainHandItem();
        PlayerRestraintPart part = getEntityTargetPart(actionPlayer);
        KidnapUtils.BindResult result = KidnapUtils.targetCanBeBound(stack, target, actionPlayer,part);
        if (!result.canBind()) {
            actionPlayer.displayClientMessage(result.messageKey(), true);
            return;
        }

        // 打断挣扎逻辑
        if (target instanceof ServerPlayer targetPlayer) {
             if (getIsStruggling(targetPlayer)){
                 PacketDistributor.sendToPlayer(targetPlayer, new InterruptStrugglePayload());
             }
        }

        START_TIME_MAP.put(actionPlayer.getUUID(), actionPlayer.level().getGameTime());

        KidnapUtils.updateKidnapData(actionPlayer, true, 0, target.getUUID(), stack,part);

        KidnapUtils.updateKidnapData(target, false, 0, actionPlayer.getUUID(), stack,part);

    }

    public static void stopKidnapping(ServerPlayer actionPlayer) {
        UUID targetUUID = KidnapUtils.getPartnerUUID(actionPlayer);
        Entity target = actionPlayer.serverLevel().getEntity(targetUUID);

        START_TIME_MAP.remove(actionPlayer.getUUID());
        KidnapUtils.clearKidnapData(actionPlayer);

        if (target instanceof Player targetPlayer) {
            KidnapUtils.clearKidnapData(targetPlayer);
        }
    }
}