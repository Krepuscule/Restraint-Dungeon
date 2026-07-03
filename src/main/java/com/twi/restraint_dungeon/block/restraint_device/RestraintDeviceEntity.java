package com.twi.restraint_dungeon.block.restraint_device;


import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.NULL_UUID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRidingEntity;


public abstract class RestraintDeviceEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack lockType = ItemStack.EMPTY;

    // 用于记录上一次的红石强度，避免每个 Tick 都触发方块更新
    private int lastSignalStrength = -1;

    public RestraintDeviceEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @NotNull ItemStack getLockType() {
        return this.lockType;
    }

    public void setLockType(@Nullable ItemStack stack) {
        this.lockType = (stack != null) ? stack.copy() : ItemStack.EMPTY;
        this.setChanged();
    }


    public static void tick(Level level, BlockPos pos, BlockState state, RestraintDeviceEntity blockEntity) {
        if (state.getBlock() instanceof RestraintDevice device) {
            LivingEntity rider = getRidingEntity(level, pos);

            int currentStrength = device.getSignalStrength(state,level, pos, rider);

            if (blockEntity.lastSignalStrength != currentStrength) {
                blockEntity.lastSignalStrength = currentStrength;
                level.updateNeighborsAt(pos, state.getBlock());
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
                blockEntity.setChanged();
            }
        }
    }

    // --- 数据持久化 ---

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag,
                                  HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("LastSignal")) {
            this.lastSignalStrength = tag.getInt("LastSignal");
        }
        if (tag.contains("LockTypeItem", Tag.TAG_COMPOUND)) {
            this.lockType = ItemStack.parse(registries, tag.getCompound("LockTypeItem"))
                    .orElse(ItemStack.EMPTY);
        } else {
            this.lockType = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag,
                                  HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("LastSignal", this.lastSignalStrength);
        if (!this.lockType.isEmpty()) {
            tag.put("LockTypeItem", this.lockType.save(registries));
        }
    }

    // --- GeckoLib 实现 ---

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    /**
     * 返回纹理的文件名（不含路径和后缀）
     */
    public String getTextureName() {
        return null;
    }

    /**
     * 返回模型的文件名（不含路径和后缀）
     */
    public String getModelName() {
        return null;
    }
}