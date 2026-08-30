package com.twi.restraint_dungeon.action.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.event.custom_event.PlayerActionEvent;
import com.twi.restraint_dungeon.mixin.client.LivingEntityRendererAccessor;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.getCurrentActionId;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.getActionPartnerUUID;

@EventBusSubscriber(modid = MODID)
public class ActionManager {
    private static final Map<String, BaseAction> REGISTRY = new HashMap<>();
    private static final List<ActionTask> ACTIVE_TASKS = new CopyOnWriteArrayList<>();

    /**
     * 手动注册 Action
     */
    public static void register(BaseAction action) {
        REGISTRY.put(action.getActionId(), action);
    }

    public static BaseAction get(String id) {
        return REGISTRY.get(id);
    }

    /**
     * 执行动作入口
     */
    public static void execute(ServerPlayer actionPlayer, String actionId, HitResult hitResult) {

        BaseAction action = get(actionId);
        if (action == null) return;

        NeoForge.EVENT_BUS.post(new PlayerActionEvent.Before(actionPlayer, action, hitResult));

        if ("STOP".equals(actionId)) {
            ActionTask currentTask = getActiveTask(actionPlayer);
            if (currentTask != null) {
                currentTask.forceAbortNow(actionPlayer.server);
                ACTIVE_TASKS.remove(currentTask);
            }
            return;
        }

        if (PlayerActionUtils.isDoingAction(actionPlayer)) {
            ActionTask currentTask = getActiveTask(actionPlayer);
            if (currentTask != null && currentTask.getAction().isInfinite()) {
                currentTask.forceAbortNow(actionPlayer.server);
                ACTIVE_TASKS.remove(currentTask);
            } else {
                return;
            }
        }

        Component failureReason = action.canUse(actionPlayer, hitResult);
        if (failureReason != null) {
            actionPlayer.displayClientMessage(failureReason, true);
            return;
        }


        LivingEntity target = null;

        if (action instanceof CarryingAction carryingAction && carryingAction.requiresCarrying()) {
            Entity passenger = actionPlayer.getPassengers().isEmpty() ? null : actionPlayer.getPassengers().get(0);
            if (passenger instanceof LivingEntity living) {
                target = living;
            }
        }
        else if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            target = living;
        }


        if (target != null) {

            PlayerActionUtils.linkAction(actionPlayer, target, actionId);
        } else {

            PlayerActionUtils.boundSingleAction(actionPlayer, actionId);
        }

        ActionTask task = new ActionTask(actionPlayer, target, action, hitResult);
        ACTIVE_TASKS.add(task);

        action.onStart(actionPlayer, target, hitResult);
        NeoForge.EVENT_BUS.post(new PlayerActionEvent.Start(actionPlayer, action, hitResult));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        ACTIVE_TASKS.removeIf(task -> task.tick(server));
    }

    @Nullable
    public static ActionTask getActiveTask(ServerPlayer player) {
        for (ActionTask task : ACTIVE_TASKS) {
            if (task.getCarrierUUID().equals(player.getUUID())) {
                return task;
            }
        }
        return null;
    }

    /**
     * 强制停止某个实体的所有动作任务
     */
    public static void forceStop(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        ACTIVE_TASKS.forEach(task -> {
            if (task.getCarrierUUID().equals(uuid) || task.getTargetUUID().equals(uuid)) {
                task.abort();
            }
        });
    }

    public static Set<String> getRegisteredIds() {
        return REGISTRY.keySet();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!(player instanceof AbstractClientPlayer renderPlayer)) return;

        String currentAction = getCurrentActionId(renderPlayer);
        BaseAction action = get(currentAction);
        if (currentAction == null || currentAction.equals("NONE")) {
            return;
        }

        UUID partnerUUID = getActionPartnerUUID(renderPlayer);

        if (partnerUUID == null || partnerUUID.equals(Util.NIL_UUID)) {
            return;
        }

        if(action instanceof CarryingAction){
            return;
        }

        if (Minecraft.getInstance().level == null) return;

        AbstractClientPlayer partnerPlayer = (AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(partnerUUID);
        if (partnerPlayer == null) return;

        PlayerRenderer renderer = event.getRenderer();
        float partialTicks = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();

        float targetYaw = calculatePlayerYaw(renderPlayer, partnerPlayer, action);

        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0, -1.5, 0);

        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(targetYaw)));

        double serverDistance = 1.0f;
        double visualDistance = action.getAnimationDistance();
        double shiftAmount = (serverDistance - visualDistance) / 2.0;

        poseStack.translate(0, 0, shiftAmount);

        var animationPlayer = ((IAnimatedPlayer) renderPlayer).playerAnimator_getAnimation();
        animationPlayer.setTickDelta(partialTicks);
        if (animationPlayer.isActive()) {
            Vec3f vec3d = animationPlayer.get3DTransform("body", TransformType.POSITION, Vec3f.ZERO);
            poseStack.translate(-vec3d.getX(), -vec3d.getY() + 0.7, vec3d.getZ());

            Vec3f vec3f = animationPlayer.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO);
            poseStack.mulPose(Axis.ZP.rotation(vec3f.getZ()));
            poseStack.mulPose(Axis.YP.rotation(-vec3f.getY()));
            poseStack.mulPose(Axis.XP.rotation(-vec3f.getX()));

            poseStack.translate(0, -0.7d, 0);
        }

        poseStack.scale(1.0F, 1.0F, 1.0F);

        renderPlayerModel(renderer, renderPlayer, poseStack, buffer, packedLight, partialTicks);

        poseStack.popPose();

        event.setCanceled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderPlayerModel(PlayerRenderer renderer, AbstractClientPlayer entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTicks) {
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        model.young = entity.isBaby();

        model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        RenderType renderType = RenderType.entityCutoutNoCull(renderer.getTextureLocation(entity));
        model.renderToBuffer(poseStack, buffer.getBuffer(renderType), packedLight, OverlayTexture.NO_OVERLAY, -1);

        List<RenderLayer<?, ?>> layers = ((LivingEntityRendererAccessor) renderer).getLayers();
        for (RenderLayer<?, ?> layer : layers) {
            safeRenderLayer(layer, poseStack, buffer, packedLight, entity, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void safeRenderLayer(RenderLayer layer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer entity, float partialTicks) {
        layer.render(
                poseStack,
                buffer,
                packedLight,
                entity,
                0.0F, 0.0F,
                partialTicks,
                0.0F,
                0.0F, 0.0F
        );
    }

    @OnlyIn(Dist.CLIENT)
    private static float calculatePlayerYaw(AbstractClientPlayer renderPlayer, AbstractClientPlayer partnerPlayer, BaseAction action) {
        AbstractClientPlayer actor, target;

        boolean isRenderPlayerActor = renderPlayer.getUUID().compareTo(partnerPlayer.getUUID()) <= 0;
        if (isRenderPlayerActor) {
            actor = renderPlayer;
            target = partnerPlayer;
        } else {
            actor = partnerPlayer;
            target = renderPlayer;
        }

        double deltaX = target.getX() - actor.getX();
        double deltaZ = target.getZ() - actor.getZ();
        float baseYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;

        boolean shouldFaceActor = isFacingActor(target, action);

        if (isRenderPlayerActor) {
            return baseYaw;
        } else {
            return shouldFaceActor ? baseYaw + 180.0F : baseYaw;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean isFacingActor(AbstractClientPlayer player, BaseAction action) {
        return action.shouldAnimFaceActor();
    }
}