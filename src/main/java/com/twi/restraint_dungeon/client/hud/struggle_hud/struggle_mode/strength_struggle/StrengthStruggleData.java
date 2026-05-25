package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.strength_struggle;

import com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.common_utils.ShakeEffect;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class StrengthStruggleData {
    public float progress = 0.0f;
    public long lastUpdateTime;
    public long lastKeyPressTime;
    public boolean lastKeyWasLeft = false;
    public boolean isActive = true;

    // 锁相关
    public ItemStack lockItem = ItemStack.EMPTY;
    public ShakeEffect lockShake = new ShakeEffect();

    public StrengthStruggleData() {
        this.lastUpdateTime = System.currentTimeMillis();
        this.lastKeyPressTime = System.currentTimeMillis();
        refreshLockInfo();
    }

    public void refreshLockInfo() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack strugglingItem = StruggleUtils.getPlayerStrugglingItem(player);
            if (strugglingItem.getItem() instanceof RestraintItem restraintItem) {
                this.lockItem = restraintItem.getLockType(Minecraft.getInstance().player,strugglingItem);
            } else {
                this.lockItem = ItemStack.EMPTY;
            }
        }
    }
}