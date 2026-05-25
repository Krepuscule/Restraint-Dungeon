package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerRestraintMovePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindLegs;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class RestraintMoveClientManager {

    private static int clientCooldownTimer = 0;
    private static float targetBodyRot = 0.0f;

    private static PlayerRestraintMove activeClientMove = null;

    public static float getTargetBodyRot() { return targetBodyRot; }


    @SubscribeEvent
    public static void onClientPlayerTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof LocalPlayer player)) return;

        if (activeClientMove != null && activeClientMove.getCurrentStage() != PlayerRestraintMove.Stage.NONE) {
            activeClientMove.updateTick(player);

            if (activeClientMove.getCurrentStage() == PlayerRestraintMove.Stage.NONE) {
                activeClientMove = null;
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        LocalPlayer player = (LocalPlayer) event.getEntity();
        Input input = event.getInput();

        if (getRestraintPosition(player) != RestraintPosition.STANDING || isBeenBindLegs(player)) {
            input.leftImpulse = 0;
            input.forwardImpulse = 0;
            input.jumping = false;
            input.shiftKeyDown = false;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;

            if (activeClientMove == null) {
                handleClientInputDetection(player);
            }
        }
    }

    private static void handleClientInputDetection(LocalPlayer player) {
        if (clientCooldownTimer > 0) {
            clientCooldownTimer--;
            return;
        }
        if (!player.onGround()) return;

        Minecraft mc = Minecraft.getInstance();
        String dir = "NONE";

        PlayerRestraintMove matchingMove = null;
        for (PlayerRestraintMove move : RestraintMoveManager.getRegisteredMoves().values()) {
            if (move.canMove(player)) {
                matchingMove = move;
                break;
            }
        }
        if (matchingMove == null) return;

        if (mc.options.keyUp.isDown() && matchingMove.canMoveForward(player)) dir = "W";
        else if (mc.options.keyDown.isDown() && matchingMove.canMoveBackward(player)) dir = "S";
        else if (mc.options.keyLeft.isDown() && matchingMove.canTurnLeft(player)) dir = "A";
        else if (mc.options.keyRight.isDown() && matchingMove.canTurnRight(player)) dir = "D";

        if (!dir.equals("NONE")) {
            clientCooldownTimer = matchingMove.getCooldown(player, dir);

            try {
                activeClientMove = matchingMove.getClass().getDeclaredConstructor().newInstance();
                activeClientMove.start(player, dir);
            } catch (Exception e) { e.printStackTrace(); }

            PacketDistributor.sendToServer(new PlayerRestraintMovePayload(dir));
        }
    }


    public static void handleStageSyncFromServer(String moveTypeId, String stage, String direction) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (activeClientMove == null) {
            PlayerRestraintMove baseMove = RestraintMoveManager.getRegisteredMoves().get(moveTypeId);
            if (baseMove != null) {
                try {
                    activeClientMove = baseMove.getClass().getDeclaredConstructor().newInstance();
                    activeClientMove.start(player, direction);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }

        if (activeClientMove != null) {
            if ("MID".equals(stage)) {
                if (activeClientMove.getCurrentStage() == PlayerRestraintMove.Stage.PRE) {
                    activeClientMove.forceAdvance(player);
                }

                if (direction.equals("A") || direction.equals("D")) {
                    float angle = activeClientMove.getTurnAngle();
                    float rotDelta = direction.equals("A") ? -angle : angle;
                    targetBodyRot = Mth.wrapDegrees(targetBodyRot + rotDelta);
                }
            } else if ("END".equals(stage)) {
                if (activeClientMove.getCurrentStage() == PlayerRestraintMove.Stage.MID) {
                    activeClientMove.forceAdvance(player);
                }
            }
        }
    }


    // 双腿拘束下的头部旋转限制逻辑
    private static float visualBodyRot = 0.0f;
    private static final float ROT_INTERPOLATION_SPEED = 0.15f;
    private static boolean wasRestricted = false;
    private static float MAX_HEAD_ROT_RANGE = 60.0f;
    private static boolean LOCK_HEAD_ROT_COMPLETELY = false;

    @SubscribeEvent
    public static void headRotLimit_onRenderTick(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        boolean isRestricted = (getRestraintPosition(player) != RestraintPosition.STANDING || isBeenBindLegs(player));
        if (isRestricted) {
            if (!wasRestricted) { visualBodyRot = targetBodyRot = player.getYRot(); wasRestricted = true; }
            visualBodyRot = Mth.rotLerp(ROT_INTERPOLATION_SPEED, visualBodyRot, targetBodyRot);
            player.yBodyRot = player.yBodyRotO = visualBodyRot;
            float allowedRange = LOCK_HEAD_ROT_COMPLETELY ? 0.0f : MAX_HEAD_ROT_RANGE;
            float diff = Mth.wrapDegrees(player.getYRot() - visualBodyRot);
            if (Math.abs(diff) > allowedRange) {
                float clampedYRot = visualBodyRot + (diff > 0 ? allowedRange : -allowedRange);
                player.setYRot(clampedYRot); player.yRotO = clampedYRot;
            }
        } else { wasRestricted = false; }
    }
}
