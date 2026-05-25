package com.twi.restraint_dungeon.action.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientRenderHandler {
    private static final Minecraft client = Minecraft.getInstance();
    public static final Map<UUID, UUID> ACTIVE_LOCKS = new HashMap<>();

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        AbstractClientPlayer renderPlayer = (AbstractClientPlayer) event.getEntity();
        UUID playerId = renderPlayer.getUUID();

        // 如果这个玩家当前正处于动作锁定中
        if (ACTIVE_LOCKS.containsKey(playerId)) {
            UUID partnerId = ACTIVE_LOCKS.get(playerId);
            if (partnerId == null || client.level == null) return;

            AbstractClientPlayer partner = (AbstractClientPlayer) client.level.getPlayerByUUID(partnerId);
            if (partner == null) return;

            // 1. 取消原版带有拉扯抖动的物理模型渲染
            event.setCanceled(true);

            PoseStack poseStack = event.getPoseStack();
            PlayerRenderer renderer = event.getRenderer();
            float partialTicks = event.getPartialTick();

            poseStack.pushPose();

            // 2. 基础回正与对齐
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(0, -1.5, 0);

            // 3. 朝向锁定：无论目标怎么动鼠标，视觉上强制使用对方(或者自己)作为动作发起者时的渲染朝向
            // 因为要求两者朝向一致(背对)，我们直接让两人的渲染基础 Y 轴旋转角保持一致
            // 取两名玩家中“动作发起者”或者当前玩家在渲染帧的插值朝向(getViewYRot)
            float renderYaw = renderPlayer.getViewYRot(partialTicks);
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-renderYaw)));

            // 4. 接管 PlayerAnimator 动画骨骼以实现平滑无缝卡位
            if (renderPlayer instanceof IAnimatedPlayer animHolder) {
                AnimationProcessor animationPlayer = animHolder.playerAnimator_getAnimation();
                animationPlayer.setTickDelta(partialTicks);

                if (animationPlayer.isActive()) {
                    // 读取 .json 动画中 body 骨骼平滑的物理偏移量
                    var posOffset = animationPlayer.get3DTransform("body", TransformType.POSITION, new Vec3f(0, 0, 0));
                    // 加上 0.7 的高度补偿，应用到位移矩阵
                    poseStack.translate(posOffset.getX(), posOffset.getY() + 0.7f, posOffset.getZ());

                    // 锁死并应用动画定义的 Pitch, Yaw, Roll 翻转
                    var rotOffset = animationPlayer.get3DTransform("body", TransformType.ROTATION, new Vec3f(0, 0, 0));
                    poseStack.mulPose(Axis.ZP.rotation(rotOffset.getZ()));
                    poseStack.mulPose(Axis.YP.rotation(rotOffset.getY()));
                    poseStack.mulPose(Axis.XP.rotation(rotOffset.getX()));

                    poseStack.translate(0, -0.7f, 0);
                }
            }
            renderer.getModel().young = renderPlayer.isBaby();
            renderer.getModel().setupAnim(renderPlayer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            
            var renderType = net.minecraft.client.renderer.RenderType.entityCutoutNoCull(renderer.getTextureLocation(renderPlayer));
            renderer.getModel().renderToBuffer(poseStack, event.getMultiBufferSource().getBuffer(renderType), event.getPackedLight(), net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1);

            poseStack.popPose();
        }
    }

    public static void lock(UUID carrier, UUID target) {
        ACTIVE_LOCKS.put(carrier, target);
        ACTIVE_LOCKS.put(target, carrier);
    }

    public static void unlock(UUID carrier, UUID target) {
        ACTIVE_LOCKS.remove(carrier);
        ACTIVE_LOCKS.remove(target);
    }
}