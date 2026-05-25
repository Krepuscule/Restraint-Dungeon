package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.unlock_struggle;

import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.common_utils.ShakeEffect;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.*;

public class UnlockStruggleData {
    public float pointerPosition = 0.0f;
    public int pointerDirection = 1;
    public float targetZoneStart = 0.3f;
    public float targetZoneEnd = 0.5f;
    public float overallProgress = 0.0f;
    public boolean isActive = false;
    public long lastUpdateTime = 0;

    public float pointerSpeed;
    public float targetZoneRatio;
    public float maxProgressIncrement;
    public float minProgressIncrement;
    public float maxRegressionRate;
    public float minRegressionRate;

    public final float pointerWidthPercentage = 0.06f;

    // 锁相关
    public ItemStack lockItem = ItemStack.EMPTY;
    public ShakeEffect lockShake = new ShakeEffect();
    public boolean showLockedMessage = false;
    public long messageStartTime = 0;
    public static final long MESSAGE_DURATION = 2000;

    public enum FeedbackState { NORMAL, SUCCESS, MISS }
    public FeedbackState currentFeedback = FeedbackState.NORMAL;
    public long lastFeedbackTime = 0;
    public static final long FEEDBACK_DURATION = 2000;

    public UnlockStruggleData() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            refreshAttributes();
            generateTargetZone();
            this.lastUpdateTime = System.currentTimeMillis();
            this.isActive = true;

            ItemStack item = getPlayerStrugglingItem(player);
            if (item.getItem() instanceof RestraintItem restraintItem) {
                this.lockItem = restraintItem.getLockType(Minecraft.getInstance().player,item);
            }
        }
    }

    public void refreshAttributes() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack item = getPlayerStrugglingItem(player);
        if (!(item.getItem() instanceof RestraintItem restraintItem)) return;

        this.minProgressIncrement = unlockStruggle_ProgressIncrement(player, item);
        this.maxProgressIncrement = this.minProgressIncrement * 2.0f;
        this.minRegressionRate = unlockStruggle_Regression(player, item);
        this.maxRegressionRate = this.minRegressionRate * 2.0f;

        this.pointerSpeed = Mth.clamp(unlockStruggle_PointerSpeed(player, item), 0.005f, 0.05f);
        this.targetZoneRatio = Mth.clamp(unlockStruggle_TargetZoneRatio(player, item), 0.05f, 0.2f);
    }

    public void generateTargetZone() {
        float minStart = pointerWidthPercentage / 2;
        float maxStart = 1 - pointerWidthPercentage / 2 - targetZoneRatio;
        targetZoneStart = maxStart > minStart ? minStart + (float) Math.random() * (maxStart - minStart) : minStart;
        targetZoneEnd = targetZoneStart + targetZoneRatio;
    }

    public void updatePointerPosition(long elapsed) {
        pointerPosition += pointerDirection * pointerSpeed * (elapsed / 16.67f);
        float minPos = pointerWidthPercentage / 2;
        float maxPos = 1 - pointerWidthPercentage / 2;

        if (pointerPosition <= minPos) { pointerPosition = minPos; pointerDirection = 1; }
        else if (pointerPosition >= maxPos) { pointerPosition = maxPos; pointerDirection = -1; }
    }

    public void handleSuccess() {
        this.currentFeedback = FeedbackState.SUCCESS;
        this.lastFeedbackTime = System.currentTimeMillis();
        float reward = minProgressIncrement + (float) Math.random() * (maxProgressIncrement - minProgressIncrement);
        overallProgress = Mth.clamp(overallProgress + reward, 0.0f, 1.0f);
        refreshAttributes();
        generateTargetZone();
    }

    public void handleMiss() {
        this.currentFeedback = FeedbackState.MISS;
        this.lastFeedbackTime = System.currentTimeMillis();
        float penalty = minRegressionRate + (float) Math.random() * (maxRegressionRate - minRegressionRate);
        overallProgress = Mth.clamp(overallProgress - penalty, 0.0f, 1.0f);
        refreshAttributes();
        generateTargetZone();
    }

    public void showLockedMessage() {
        this.showLockedMessage = true;
        this.messageStartTime = System.currentTimeMillis();
    }

    public void updateState() {
        long now = System.currentTimeMillis();
        if (showLockedMessage && now - messageStartTime > MESSAGE_DURATION) showLockedMessage = false;
        if (currentFeedback != FeedbackState.NORMAL && now - lastFeedbackTime > FEEDBACK_DURATION) currentFeedback = FeedbackState.NORMAL;
    }
}