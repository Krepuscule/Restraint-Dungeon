package com.twi.restraint_dungeon.block.addon_block.placed_sword;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class PlacedSwordBlock extends BaseEntityBlock {
    public static final MapCodec<PlacedSwordBlock> CODEC = simpleCodec(PlacedSwordBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_NORTH = Block.box(2, 6, 0, 14, 10, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(2, 6, 0, 14, 10, 14);
    private static final VoxelShape SHAPE_WEST  = Block.box(0, 2, 7, 16, 14, 9);
    private static final VoxelShape SHAPE_EAST  = Block.box(0, 2, 7, 14, 14, 9);

    private static final VoxelShape COLL_NORTH = Block.box(2, 6, 0, 13, 9, 15);
    private static final VoxelShape COLL_SOUTH = Block.box(2, 6, 0, 13, 9, 13);
    private static final VoxelShape COLL_WEST  = Block.box(0, 2, 7, 15, 13, 8);
    private static final VoxelShape COLL_EAST  = Block.box(0, 2, 7, 13, 13, 8);

    public PlacedSwordBlock(Properties properties) {
        super(properties.strength(-1.0F, 3600000.0F).noOcclusion());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public @NotNull VoxelShape getShape(BlockState state,
                                        @NotNull BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> Shapes.block();
        };
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> COLL_NORTH;
            case SOUTH -> COLL_SOUTH;
            case WEST -> COLL_WEST;
            case EAST -> COLL_EAST;
            default -> Shapes.block();
        };
    }

    @Override
    public void entityInside(@NotNull BlockState state, Level level,
                             @NotNull BlockPos pos,
                             @NotNull Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;

        Direction tipDir = state.getValue(FACING);
        AABB entityBox = living.getBoundingBox();
        double blockCenterX = pos.getX() + 0.5;
        double blockCenterZ = pos.getZ() + 0.5;

        boolean atTipSide = switch (tipDir) {
            case NORTH -> entityBox.maxZ < blockCenterZ;
            case SOUTH -> entityBox.minZ > blockCenterZ;
            case WEST  -> entityBox.maxX < blockCenterX;
            case EAST  -> entityBox.minX > blockCenterX;
            default -> false;
        };

        if (!atTipSide) return;

        Vec3 move = living.getDeltaMovement();
        Vec3 tipVec = Vec3.atLowerCornerOf(tipDir.getNormal());
        boolean isPushing = living.horizontalCollision || move.lengthSqr() > 0.001;

        if (isPushing && move.dot(tipVec) < 0.05) {
            doDamageAndKnockback(living, level, pos, tipDir);
        }
    }

    private void doDamageAndKnockback(LivingEntity entity, Level level, BlockPos pos, Direction tipDir) {
        if (entity.hurtTime > 0) return;

        if (level.getBlockEntity(pos) instanceof PlacedSwordBlockEntity be) {
            ItemStack sword = be.getSword();
            if (sword.isEmpty()) return;

            Player owner = be.getOwner(level);
            DamageSource source = owner != null ? level.damageSources().playerAttack(owner) : level.damageSources().generic();

            // 计算基础伤害
            float dmg = 0.5f;
            if (sword.getItem() instanceof SwordItem s) dmg = s.getDamage(sword) + 1.0f;


            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                dmg = EnchantmentHelper.modifyDamage(serverLevel, sword, entity, source, dmg);
            }

            entity.hurt(source, dmg);

            Holder<Enchantment> knockbackEnchant = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.KNOCKBACK);

            int kbLevel = sword.getEnchantmentLevel(knockbackEnchant);

            double strength = 0.25 + (kbLevel * 0.15);

            Vec3 kbVec = Vec3.atLowerCornerOf(tipDir.getNormal()).scale(strength);
            entity.setDeltaMovement(kbVec.x, 0.25, kbVec.z);
            entity.hurtMarked = true;
        }
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state,
                                           @NotNull Direction facing,
                                           @NotNull BlockState facingState,
                                           @NotNull LevelAccessor level,
                                           @NotNull BlockPos currentPos,
                                           @NotNull BlockPos facingPos) {
        Direction swordTip = state.getValue(FACING);
        if (facing == swordTip.getOpposite() && !facingState.isFaceSturdy(level, facingPos, swordTip)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PlacedSwordBlockEntity be) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getSword());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.INVISIBLE; }
    @Nullable
    @Override public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new PlacedSwordBlockEntity(pos, state); }

}