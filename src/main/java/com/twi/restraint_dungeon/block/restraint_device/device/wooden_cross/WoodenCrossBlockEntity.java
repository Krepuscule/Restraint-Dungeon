package com.twi.restraint_dungeon.block.restraint_device.device.wooden_cross;

import com.twi.restraint_dungeon.block.ModBlockEntities;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDeviceEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class WoodenCrossBlockEntity extends RestraintDeviceEntity {

    public WoodenCrossBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public String getModelName() {
        return "wooden_cross";
    }

    // 具体的变种实现
    public static class Variant extends WoodenCrossBlockEntity {
        private final String woodType;

        public Variant(BlockPos pos, BlockState state, String woodType) {
            super(ModBlockEntities.CROSS_BE_MAP.get(woodType).get(), pos, state);
            this.woodType = woodType;
        }

        @Override
        public String getTextureName() {
            return this.woodType + "_wooden_cross";
        }
    }
}