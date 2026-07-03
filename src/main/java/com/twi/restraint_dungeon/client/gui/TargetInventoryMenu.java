package com.twi.restraint_dungeon.client.gui;

import com.twi.restraint_dungeon.client.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TargetInventoryMenu extends AbstractContainerMenu {
    private final Container targetInventory;
    private final Player operatorPlayer;
    private final Player targetPlayer;

    public TargetInventoryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(36), playerInventory.player, null);
    }

    public TargetInventoryMenu(int containerId, Inventory playerInventory, Container targetInventory, Player operator, Player target) {
        super(ModMenuTypes.TARGET_INVENTORY.get(), containerId);
        this.targetInventory = targetInventory;
        this.operatorPlayer = operator;
        this.targetPlayer = target;

        int slotX = 5;

        int targetStartY = 4;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(targetInventory, 9 + col + row * 9, slotX + col * 19, targetStartY + row * 19));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(targetInventory, i, slotX + i * 19, targetStartY + 3 * 19 + 4));
        }

        int playerStartY = 150;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, 9 + col + row * 9, slotX + col * 19, playerStartY + row * 19));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, slotX + i * 19, playerStartY + 3 * 19 + 4));
        }
    }

    @Override public boolean stillValid(@NotNull Player player) { return true; }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 36) {
                if (!this.moveItemStackTo(itemstack1, 36, 72, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 36, false)) return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        }
        return itemstack;
    }
}