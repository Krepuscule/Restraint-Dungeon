package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.loose_struggle;

import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.common_utils.ShakeEffect;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.*;

public class LooseStruggleData {
    public float progress = 0.0f;

    public float minProgress;
    public float maxProgress;
    public float minRegressionRate;
    public float maxRegressionRate;
    public int arrowLength;

    public boolean isActive = true;
    public List<Direction> currentSequence;
    public int currentIndex = 0;
    public long blockUntil = 0;

    // 锁相关
    public ItemStack lockItem = ItemStack.EMPTY;
    public ShakeEffect lockShake = new ShakeEffect();

    public LooseStruggleData() {
        refreshAttributes();
        this.currentSequence = generateRandomSequence(this.arrowLength);

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack strugglingItem = getPlayerStrugglingItem(player);
            if (strugglingItem.getItem() instanceof RestraintItem restraintItem) {
                this.lockItem = restraintItem.getLockType(Minecraft.getInstance().player,strugglingItem);
            }
        }
    }

    public void refreshAttributes() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack item = getPlayerStrugglingItem(player);
        if (!(item.getItem() instanceof RestraintItem restraintItem)) return;

        this.minProgress = looseStruggle_minProgress(player, item);
        this.maxProgress = this.minProgress * 1.5f;
        this.minRegressionRate = looseStruggle_minRegressProgress(player, item);
        this.maxRegressionRate = this.minRegressionRate * 2.0f;

        int length = looseStruggle_ArrowLength(player, item);
        this.arrowLength = Mth.clamp(length, 5, 12);
    }

    public boolean isInputBlocked() {
        return System.currentTimeMillis() < blockUntil;
    }

    public void blockInput(long duration) {
        blockUntil = System.currentTimeMillis() + duration;
    }

    public boolean checkInput(Direction inputDir) {
        if (currentIndex >= currentSequence.size()) return false;
        boolean correct = currentSequence.get(currentIndex) == inputDir;
        if (correct) currentIndex++;
        return correct;
    }

    public boolean isSequenceComplete() {
        return currentIndex >= currentSequence.size();
    }

    public void resetSequence() {
        this.currentIndex = 0;
        refreshAttributes();
        this.currentSequence = generateRandomSequence(this.arrowLength);
    }

    public static List<Direction> generateRandomSequence(int length) {
        Random random = new Random();
        List<Direction> sequence = new ArrayList<>();
        Direction[] dirs = Direction.values();
        for (int i = 0; i < length; i++) {
            sequence.add(dirs[random.nextInt(dirs.length)]);
        }
        return sequence;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}