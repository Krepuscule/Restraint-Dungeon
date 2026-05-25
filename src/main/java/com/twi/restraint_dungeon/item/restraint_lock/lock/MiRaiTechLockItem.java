package com.twi.restraint_dungeon.item.restraint_lock.lock;

import com.twi.restraint_dungeon.item.restraint_lock.RestraintLockItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MiRaiTechLockItem extends RestraintLockItem {
    public MiRaiTechLockItem() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {

    }
}
