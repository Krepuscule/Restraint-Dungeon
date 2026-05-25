package com.twi.restraint_dungeon.block.addon_block.placed_sword;

import com.twi.restraint_dungeon.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlacedSwordBlockEntity extends BlockEntity {
    private ItemStack storedSword = ItemStack.EMPTY;
    private UUID ownerUUID = null;

    public PlacedSwordBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLACED_SWORD_BE.get(), pos, state);
    }

    public void setSword(ItemStack stack, @Nullable UUID owner) {
        this.storedSword = stack.copy();
        this.ownerUUID = owner;
        this.setChanged();
    }

    public ItemStack getSword() { return this.storedSword; }

    public @Nullable Player getOwner(Level level) {
        return ownerUUID != null ? level.getPlayerByUUID(ownerUUID) : null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag,
                                  HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.storedSword.isEmpty()) {

            tag.put("SwordItem", this.storedSword.save(registries));
        }
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag,
                                  HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("SwordItem")) {
            this.storedSword = ItemStack.parse(registries, tag.getCompound("SwordItem")).orElse(ItemStack.EMPTY);
        }
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}