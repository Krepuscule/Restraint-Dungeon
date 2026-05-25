package com.twi.restraint_dungeon.block.restraint_device.ghost_block;

import com.twi.restraint_dungeon.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GhostBlockEntity extends BlockEntity {
    private BlockPos masterPos = BlockPos.ZERO;

    public GhostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GHOST_BE.get(), pos, state);
    }


    @Override
    protected void loadAdditional(@NotNull CompoundTag tag,
                                  HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MasterPos")) {
            this.masterPos = BlockPos.of(tag.getLong("MasterPos"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag,
                                  HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.masterPos != null) {
            tag.putLong("MasterPos", this.masterPos.asLong());
        }
    }

    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getMasterPos() { return masterPos; }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}