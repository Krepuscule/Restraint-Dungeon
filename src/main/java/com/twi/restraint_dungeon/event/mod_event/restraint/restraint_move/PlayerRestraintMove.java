package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move;

import com.twi.restraint_dungeon.event.custom_event.PlayerRestraintMoveEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

public abstract class PlayerRestraintMove {

    public enum Stage { NONE, PRE, MID, END, COOLDOWN }

    protected Stage currentStage = Stage.NONE;
    protected String currentDirection = "NONE";
    protected int timer = 0;
    protected float targetYaw = 0.0f;

    public abstract String getModId();
    public abstract String getMoveTypeId();
    public abstract boolean canMove(Player player);

    public abstract boolean canMoveForward(Player player);
    public abstract boolean canMoveBackward(Player player);
    public abstract boolean canTurnLeft(Player player);
    public abstract boolean canTurnRight(Player player);

    public abstract int getDuration(String direction, Stage stage);
    public abstract int getCooldown(Player player, String direction);
    public abstract float getTurnAngle();

    /**
     * 用于定义MID阶段是等待网络包触发还是根据动画到时间触发（若为false则为根据固定动画时间触发）
     */
    public boolean hasCustomClientEndCondition() { return false; }

    public Stage getCurrentStage() { return currentStage; }
    public String getCurrentDirection() { return currentDirection; }
    public float getTargetYaw() { return targetYaw; }
    public void setTargetYaw(float yaw) { this.targetYaw = yaw; }

    public void start(LivingEntity entity, String direction) {
        this.currentDirection = direction;
        this.targetYaw = entity.getYRot();
        this.currentStage = Stage.PRE;
        this.timer = getDuration(direction, Stage.PRE);

        if (entity instanceof Player player) {
            PlayerRestraintMoveEvent.Pre preEvent = new PlayerRestraintMoveEvent.Pre(player, this, direction);
            NeoForge.EVENT_BUS.post(preEvent);

            if (preEvent.isCanceled()) {
                this.reset();
            }
        }
    }

    public void updateTick(LivingEntity entity) {

        if (timer > 0) timer--;
        if (entity.level().isClientSide) {
            onTick(entity);
        }

        if (timer <= 0) {

            if (currentStage == Stage.MID && hasCustomClientEndCondition() && !entity.level().isClientSide) {
                return;
            }
            advanceStage(entity);

        }

    }

    protected abstract void onTick(LivingEntity entity);

    public void forceAdvance(LivingEntity entity) {
        advanceStage(entity);
    }

    protected void advanceStage(LivingEntity entity) {
        switch (currentStage) {
            case PRE -> {
                currentStage = Stage.MID;
                timer = getDuration(currentDirection, Stage.MID);

                if (currentDirection.equals("A") || currentDirection.equals("D")) {
                    float angle = getTurnAngle();
                    targetYaw += currentDirection.equals("A") ? -angle : angle;
                    targetYaw = Mth.wrapDegrees(targetYaw);
                }

                if (entity instanceof Player player) {
                    NeoForge.EVENT_BUS.post(new PlayerRestraintMoveEvent.Mid(player, this, currentDirection));
                }
            }
            case MID -> {
                currentStage = Stage.END;
                timer = getDuration(currentDirection, Stage.END);
                if (entity instanceof Player player) {
                    NeoForge.EVENT_BUS.post(new PlayerRestraintMoveEvent.End(player, this, currentDirection));
                }
            }
            case END -> {
                if (entity instanceof Player player) {
                    currentStage = Stage.COOLDOWN;
                    timer = getCooldown(player, currentDirection);
                } else {
                    reset();
                }
            }
            case COOLDOWN -> reset();
            default -> reset();
        }
    }

    public void reset() {
        this.currentStage = Stage.NONE;
        this.currentDirection = "NONE";
        this.timer = 0;
    }
}