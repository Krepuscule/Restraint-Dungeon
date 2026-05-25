package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager;

import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerInteractProgressPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class InteractingProgressManager {
    public static final Map<UUID, InteractProgressInfo> INTERACT_PROGRESS = new HashMap<>();
    public static final Map<UUID, Long> INTERACT_COOLDOWN = new HashMap<>();
    public static float currentProgress = 0.0f; // 客户端本地缓存
    private static final int INTERACT_COOLDOWN_MS = 500;

    // --- 服务端逻辑 ---

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (!(p instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();
        if (!INTERACT_PROGRESS.containsKey(uuid)) return;

        InteractProgressInfo info = INTERACT_PROGRESS.get(uuid);
        info.timeoutTicks++;

        if (checkInterrupt(player, info)) {
            sync(player, 0);
            INTERACT_PROGRESS.remove(uuid);
            return;
        }

        if (info.timeoutTicks > 5) info.isStopping = true;

        if (info.isStopping) {
            if (info.timeoutTicks >= 15) {
                sync(player, 0);
                INTERACT_PROGRESS.remove(uuid);
            }
        } else {
            info.ticks++;
            float progress = (float) info.ticks / info.targetTicks;
            sync(player, progress);

            if (info.ticks >= info.targetTicks) {
                info.onComplete.accept(player);
                setCooldown(player.getUUID(), INTERACT_COOLDOWN_MS);
                sync(player, 0);
                INTERACT_PROGRESS.remove(uuid);
            }
        }
    }

    private static boolean checkInterrupt(ServerPlayer player, InteractProgressInfo info) {
        if (player.position().distanceToSqr(info.startPos) > 0.25) return true;
        boolean isBackMode = InteractTargetSelector.isBackMode(player);

        if (info.pos != null) {
            if (isBackMode) {
                return player.distanceToSqr(Vec3.atCenterOf(info.pos)) > (info.maxRange * info.maxRange) + 1.0;
            } else {
                Vec3 eyePos = player.getEyePosition();
                Vec3 reachVec = eyePos.add(player.getViewVector(1.0F).scale(info.maxRange + 1.5));
                HitResult hit = player.level().clip(new ClipContext(eyePos, reachVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                return !(hit instanceof BlockHitResult bHit && bHit.getBlockPos().equals(info.pos));
            }
        } else if (info.targetEntity != null) {
            return player.distanceToSqr(info.targetEntity) > (info.maxRange * info.maxRange) + 1.0;
        }
        return true;
    }

    public static void startOrUpdate(Player player, BlockPos pos, Entity entity, InteractionHand hand, int ticks, double range, Consumer<ServerPlayer> onComplete) {
        if (!player.level().isClientSide) {
            if (isInCooldown(player.getUUID())) return;
            InteractProgressInfo info = INTERACT_PROGRESS.get(player.getUUID());
            if (info != null && !info.isStopping) {
                info.timeoutTicks = 0;
            } else {
                INTERACT_PROGRESS.put(player.getUUID(), new InteractProgressInfo(pos, entity, player.position(), hand, ticks, range, onComplete));
            }
        } else {
            player.swing(hand);
        }
    }

    public static void setCooldown(UUID uuid, long millis) {
        INTERACT_COOLDOWN.put(uuid, System.currentTimeMillis() + millis);
    }

    public static boolean isInCooldown(UUID uuid) {
        return INTERACT_COOLDOWN.getOrDefault(uuid, 0L) > System.currentTimeMillis();
    }

    private static void sync(ServerPlayer player, float progress) {
        PacketDistributor.sendToPlayer(player,new PlayerInteractProgressPayload(progress));
    }
}



