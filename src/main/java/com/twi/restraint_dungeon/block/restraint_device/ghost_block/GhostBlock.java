package com.twi.restraint_dungeon.block.restraint_device.ghost_block;

import com.mojang.serialization.MapCodec;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GhostBlock extends BaseEntityBlock {
    public GhostBlock(Properties props) {
        super(props);
    }
    public static final MapCodec<GhostBlock> CODEC = simpleCodec(GhostBlock::new);

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level,
                                                        @NotNull BlockPos pos,
                                                        @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof GhostBlockEntity ghostBE) {
            BlockPos mPos = ghostBE.getMasterPos();
            if (mPos != null && level.getBlockState(mPos).getBlock() instanceof RestraintDevice master) {
                return master.useWithoutItem(level.getBlockState(mPos), level, mPos, player, hit.withPosition(mPos));
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state,
                                                 @NotNull Player player) {
        if (level.getBlockEntity(pos) instanceof GhostBlockEntity ghostBE) {
            BlockPos mPos = ghostBE.getMasterPos();
            if (mPos != null && !level.isClientSide) {
                level.destroyBlock(mPos, !player.isCreative());
            }
        }
        return state;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof GhostBlockEntity ghostBE) {
            BlockPos mPos = ghostBE.getMasterPos();

            if (mPos != null && !mPos.equals(BlockPos.ZERO)) {
                BlockState mState = level.getBlockState(mPos);
                if (mState.getBlock() instanceof RestraintDevice master) {
                    VoxelShape masterFullShape = master.getShape(mState, level, mPos, context);

                    double dx = mPos.getX() - pos.getX();
                    double dy = mPos.getY() - pos.getY();
                    double dz = mPos.getZ() - pos.getZ();

                    return masterFullShape.move(dx, dy, dz);
                }
            }
        }
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GhostBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state,
                                                 @NotNull BlockGetter level,
                                                 @NotNull BlockPos pos,
                                                 @NotNull CollisionContext context) {
        return getPreciseRelocatedShape(level, pos, context);
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

//    @Override
//    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state,
//                                                   @NotNull BlockGetter level,
//                                                   @NotNull BlockPos pos) {
//        return getPreciseRelocatedShape(level, pos, CollisionContext.empty());
//    }

    protected VoxelShape getPreciseRelocatedShape(BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level == null || pos == null) return Shapes.empty();

        if (level.getBlockEntity(pos) instanceof GhostBlockEntity ghostBE) {
            BlockPos mPos = ghostBE.getMasterPos();

            if (mPos != null && !mPos.equals(BlockPos.ZERO)) {
                BlockState mState = level.getBlockState(mPos);

                if (mState.getBlock() instanceof RestraintDevice master) {
                    VoxelShape masterShape = master.getShape(mState, level, mPos, context);

                    VoxelShape relocated = masterShape.move(
                            mPos.getX() - pos.getX(),
                            mPos.getY() - pos.getY(),
                            mPos.getZ() - pos.getZ()
                    );
                    return Shapes.join(relocated, Shapes.block(), BooleanOp.AND);
                }
            }
        }
        return Shapes.empty();
    }
}