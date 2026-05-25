package com.twi.restraint_dungeon.block.restraint_device.device.doll_stand;

import com.twi.restraint_dungeon.block.ModBlockEntities;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDeviceEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DollStandBlockEntity extends RestraintDeviceEntity {

    public DollStandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public String getModelName() {
        return "doll_stand";
    }

    public static class Variant extends DollStandBlockEntity {
        private final String materialType;

        public Variant(BlockPos pos, BlockState state, String materialType) {
            super(ModBlockEntities.DOLL_STAND_BE_MAP.get(materialType).get(), pos, state);
            this.materialType = materialType;
        }

        @Override
        public String getTextureName() {
            return this.materialType + "_doll_stand";
        }
    }
}