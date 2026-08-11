package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position;

import com.mojang.blaze3d.platform.InputConstants;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerSetTargetPositionPayload;
import com.twi.restraint_dungeon.network.payload.player_restraint.RestraintPositionPayload;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.client.keybind.ModKeyBinds.CHANGE_POSITION;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.getCarrier;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isBeingCarried;
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
        CARRIED,
        RIDING
    }

    // --- 追踪变量 ---
    private static Vector3f currentRotOffset = new Vector3f(0.0f, 0.0f, 0.0f);
    private static Vector3f currentCameraOffset = new Vector3f(0.0f, 0.0f, 0.0f);

    private static final float LERP_OFFSET = 0.1f;
    private static final float LERP_ROT = 0.2f;

    // --- 姿势偏移量常量 ---
    private static final Vector3f STANDING_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Vector3f KNEELING_OFFSET = new Vector3f(0.0f, -0.45f, 0.0f);
    private static final Vector3f SITTING_OFFSET = new Vector3f(0.0f, -0.65f, 0.0f);
    private static final Vector3f LYING_UP_OFFSET = new Vector3f(0.0f, -1.25f, -0.85f);
    private static final Vector3f LYING_LEFT_OFFSET = new Vector3f(-0.25f, -1.35f, -0.9f);
    private static final Vector3f LYING_RIGHT_OFFSET = new Vector3f(0.25f, -1.35f, -0.9f);
    private static final Vector3f LYING_DOWN_OFFSET = new Vector3f(0.0f, -1.3f, -1.0f);


    // --- 姿势旋转量常量 ---
    private static final Vector3f STANDING_ROT = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Vector3f KNEELING_ROT = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Vector3f SITTING_ROT = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Vector3f LYING_UP_ROT = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Vector3f LYING_DOWN_ROT = new Vector3f(0.0f, 180.0f, 0.0f);
    private static final Vector3f LYING_LEFT_ROT = new Vector3f(0.0f, -45.0f, -90.0f);
    private static final Vector3f LYING_RIGHT_ROT = new Vector3f(0.0f, 45.0f, 90.0f);

    // --- 辅助函数 ---

    private static Vector3f getTargetOffset(RestraintPosition position) {
        return switch (position) {
            case STANDING -> STANDING_OFFSET;
            case KNEELING -> KNEELING_OFFSET;
            case SITTING -> SITTING_OFFSET;
            case LYING_UP -> LYING_UP_OFFSET;
            case LYING_LEFT -> LYING_LEFT_OFFSET;
            case LYING_RIGHT -> LYING_RIGHT_OFFSET;
            case LYING_DOWN -> LYING_DOWN_OFFSET;
            case CONNECTING -> getConnectingPositionOffset();
            case CARRIED -> getCarriedPositionOffset();
            case RIDING -> getRidingRestraintBlockOffset();
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
            case CARRIED -> getCarriedPositionRotation();
            case RIDING -> getRidingRestraintBlcokRotation();
            default -> new Vector3f(0, 0,0);
        };
    }

    private static Vector3f getConnectingPositionOffset() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return new Vector3f(0.0f, 0.0f, 0.0f);
        ItemStack connectBind = getFirstConnectBind(player);
        if (connectBind.getItem() instanceof RestraintItem restraintItem) {
            Vector3f offset = restraintItem.getConnectBindViewOffset(player, connectBind);
            if (offset != null) return offset;
        }
        return new Vector3f(0.0f, 0.0f, 0.0f);
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

    private static Vector3f getCarriedPositionOffset(){
        Player player = Minecraft.getInstance().player;
        if (player == null || !isBeingCarried(player)) return new Vector3f(0, 0,0);

        CarryType type = PlayerCarryUtils.getCurrentCarryType(player);
        Player carrier = getCarrier(player);
        if (type != null && carrier != null) {
            return type.getPassengerFirstPersonCameraOffset(carrier, player);
        }

        return new Vector3f(0, 0,0);
    }

    private static Vector3f getCarriedPositionRotation(){
        Player player = Minecraft.getInstance().player;
        if (player == null || !isBeingCarried(player)) return new Vector3f(0, 0,0);

        CarryType type = PlayerCarryUtils.getCurrentCarryType(player);
        Player carrier = getCarrier(player);
        if (type != null && carrier != null) {
            return type.getPassengerFirstPersonCameraRotation(carrier, player);
        }

        return new Vector3f(0, 0,0);
    }

    private static Vector3f getRidingRestraintBlockOffset(){
        Player player = Minecraft.getInstance().player;
        if (player == null || !isRidingRestraintDevice(player)) return new Vector3f(0, 0,0);

        Block block = getRestraintDevice(player);
        if(block instanceof RestraintDevice device){
            return device.getRiderFirstCameraOffset();
        }

        return new Vector3f(0, 0,0);
    }

    private static Vector3f getRidingRestraintBlcokRotation(){
        Player player = Minecraft.getInstance().player;
        if (player == null || !isRidingRestraintDevice(player)) return new Vector3f(0, 0,0);

        Block block = getRestraintDevice(player);
        if(block instanceof RestraintDevice device){
            return device.getRiderFirstCameraRotation();
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
            if (!isBusyState(target) && !isRidingRestraintDevice(target) && isBeenFullyBind(target)
                    && (target instanceof Player || target instanceof BaseNPCEntity)) {
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

    public static Vector3f getCurrentCameraOffset() { return currentCameraOffset; }
    public static float getCurrentYawOffset() { return currentRotOffset.y; }
    public static float getCurrentPitchOffset() { return currentRotOffset.x;}
    public static float getCurrentRollOffset() { return currentRotOffset.z; }

    @SubscribeEvent
    public static void onRenderFramePre(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused()) return;

        RestraintPosition pos = getRestraintPosition(mc.player);
        if (pos == null) return;

        Vector3f targetOffset = getTargetOffset(pos);
        Vector3f targetRot = getTargetRotation(pos);

        currentCameraOffset = currentCameraOffset.lerp(targetOffset, LERP_OFFSET);
        currentRotOffset = currentRotOffset.lerp(targetRot, LERP_ROT);
    }
}
