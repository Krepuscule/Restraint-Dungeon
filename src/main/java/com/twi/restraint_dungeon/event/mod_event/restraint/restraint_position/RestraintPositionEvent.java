package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position;

import com.mojang.blaze3d.platform.InputConstants;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerSetTargetPositionPayload;
import com.twi.restraint_dungeon.network.payload.player_restraint.RestraintPositionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.client.keybind.ModKeyBinds.CHANGE_POSITION;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

@EventBusSubscriber(modid = MODID,value = Dist.CLIENT)
public class RestraintPositionEvent {

    public enum RestraintPosition {
        STANDING,
        KNEELING,
        SITTING,
        LYING_UP,
        LYING_DOWN,
        LYING_LEFT,
        LYING_RIGHT,
        CONNECTING,
        CARRIED
    }

    // --- 追踪变量 ---
    private static Vector3f currentRotOffset = new Vector3f(0, 0,0);
    private static Vec3 currentCameraOffset = new Vec3(0.0, 0.0, 0.0);

    private static final double LERP_OFFSET = 0.05f;
    private static final float LERP_ROT = 0.15f;

    // --- 姿势偏移量常量 ---
    private static final Vec3 STANDING_OFFSET = new Vec3(0.0, 0.0, 0.0);
    private static final Vec3 KNEELING_OFFSET = new Vec3(0.0, -0.45, 0.0);
    private static final Vec3 SITTING_OFFSET = new Vec3(0.0, -0.65, 0.0);
    private static final Vec3 LYING_UP_OFFSET = new Vec3(0.0, -1.25, -0.85);
    private static final Vec3 LYING_LEFT_OFFSET = new Vec3(-0.25, -1.2, -0.85);
    private static final Vec3 LYING_RIGHT_OFFSET = new Vec3(0.25, -1.2, -0.85);
    private static final Vec3 LYING_DOWN_OFFSET = new Vec3(0.0, -1.3, -1.0);


    // --- 姿势旋转量常量 ---
    private static final Vector3f STANDING_ROT = new Vector3f(0, 0, 0);
    private static final Vector3f KNEELING_ROT = new Vector3f(0, 0, 0);
    private static final Vector3f SITTING_ROT = new Vector3f(0, 0, 0);
    private static final Vector3f LYING_UP_ROT = new Vector3f(0, 0, 0);
    private static final Vector3f LYING_DOWN_ROT = new Vector3f(90, 0, 180);
    private static final Vector3f LYING_LEFT_ROT = new Vector3f(0, -45, -90);
    private static final Vector3f LYING_RIGHT_ROT = new Vector3f(0, 45, 90);

    // --- 辅助函数 ---

    private static Vec3 getTargetOffset(RestraintPosition position) {
        return switch (position) {
            case STANDING -> STANDING_OFFSET;
            case KNEELING -> KNEELING_OFFSET;
            case SITTING -> SITTING_OFFSET;
            case LYING_UP -> LYING_UP_OFFSET;
            case LYING_LEFT -> LYING_LEFT_OFFSET;
            case LYING_RIGHT -> LYING_RIGHT_OFFSET;
            case LYING_DOWN -> LYING_DOWN_OFFSET;
            case CONNECTING -> getConnectingPositionOffset();
            default -> STANDING_OFFSET;
        };
    }

    private static Vector3f getTargetRotation(RestraintPosition position) {
        return switch (position) {
            case STANDING -> STANDING_ROT;
            case KNEELING -> KNEELING_ROT;
            case SITTING -> SITTING_ROT;
            case LYING_UP -> LYING_UP_ROT;
            case LYING_LEFT -> LYING_LEFT_ROT;
            case LYING_RIGHT -> LYING_RIGHT_ROT;
            case LYING_DOWN -> LYING_DOWN_ROT;
            case CONNECTING -> getConnectingPositionRotation();
            default -> new Vector3f(0, 0,0);
        };
    }

    private static Vec3 getConnectingPositionOffset() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return new Vec3(0.0, 0.0, 0.0);
        ItemStack connectBind = getFirstConnectBind(player);
        if (connectBind.getItem() instanceof RestraintItem restraintItem) {
            Vec3 offset = restraintItem.getConnectBindViewOffset(player, connectBind);
            if (offset != null) return offset;
        }
        return new Vec3(0.0, 0.0, 0.0);
    }

    private static Vector3f getConnectingPositionRotation() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return new Vector3f(0, 0,0);
        ItemStack connectBind = getFirstConnectBind(player);
        if (connectBind.getItem() instanceof RestraintItem restraintItem) {
            Vector3f rot = restraintItem.getConnectBindViewRotation(player, connectBind);
            if (rot != null) return rot;
        }
        return new Vector3f(0, 0,0);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (!InputConstants.isKeyDown(mc.getWindow().getWindow(), CHANGE_POSITION.getKey().getValue())) return;

        int key = event.getKey();
        String dir = getDir(key);
        if (dir.isEmpty()) return;

        HitResult hit = mc.hitResult;
        if (isBusyState(mc.player)) return;

        if (hit instanceof EntityHitResult eHit && eHit.getEntity() instanceof LivingEntity target && target.distanceToSqr(mc.player) <= 2 * 2) {
            // 操作目标
            if (!isBusyState(target) && !isRidingRestraintDevice(target) && isBeenFullyBind(target)) {
                PacketDistributor.sendToServer(new PlayerSetTargetPositionPayload(target.getUUID(), dir));
            }
        } else {
            if (!isBusyState(mc.player) && !isRidingRestraintDevice(mc.player)) {
                RestraintPosition next = RestraintServerHandler.getNextPosition(mc.player, getRestraintPosition(mc.player), dir);
                if (next != null) {
                    PacketDistributor.sendToServer(new RestraintPositionPayload(mc.player.getUUID(), getRestraintPosition(mc.player),next));
                }
            }
        }
    }

    private static String getDir(int key) {
        if (key == GLFW.GLFW_KEY_UP) return "up";
        if (key == GLFW.GLFW_KEY_DOWN) return "down";
        if (key == GLFW.GLFW_KEY_LEFT) return "left";
        if (key == GLFW.GLFW_KEY_RIGHT) return "right";
        return "";
    }

    /* ------------------------------------ 镜头角度及位置处理 ---------------------------------- */

    public static Vec3 getCurrentCameraOffset() { return currentCameraOffset; }
    public static float getCurrentYawOffset() { return currentRotOffset.y; }
    public static float getCurrentPitchOffset() { return currentRotOffset.x;}
    public static float getCurrentRollOffset() { return currentRotOffset.z; }

    @SubscribeEvent
    public static void restraintPosition_ComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        RestraintPosition pos = getRestraintPosition(mc.player);

        if(pos == null || pos == RestraintPosition.CARRIED) return;

        Vec3 targetOffset = getTargetOffset(pos);
        Vector3f targetRot = getTargetRotation(pos);

        currentCameraOffset = currentCameraOffset.lerp(targetOffset, LERP_OFFSET);
        currentRotOffset = currentRotOffset.lerp(targetRot, LERP_ROT);

        if (mc.options.getCameraType().isFirstPerson()) {
            event.setYaw(event.getYaw() + currentRotOffset.y);
            event.setPitch(event.getPitch() + currentRotOffset.x);
        }
    }
}
