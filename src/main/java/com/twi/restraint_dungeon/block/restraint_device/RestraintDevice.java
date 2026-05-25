package com.twi.restraint_dungeon.block.restraint_device;

import com.mojang.serialization.MapCodec;
import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RestraintDevice extends BaseEntityBlock {

    public static final MapCodec<RestraintDevice> CODEC = simpleCodec(props -> new RestraintDevice(props, null));

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected final Map<Direction, VoxelShape> shapeCache = new EnumMap<>(Direction.class);
    protected final Supplier<? extends BlockEntityType<? extends RestraintDeviceEntity>> blockEntityType;

    public RestraintDevice(Properties properties, Supplier<? extends BlockEntityType<? extends RestraintDeviceEntity>> beType) {
        super(properties);
        this.blockEntityType = beType;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        runInitializeShapes();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return this.blockEntityType != null ? this.blockEntityType.get().create(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;

        return createTickerHelper(type, this.blockEntityType.get(), RestraintDeviceEntity::tick
        );
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // --- 形状处理逻辑 ---

    protected void runInitializeShapes() {
        VoxelShape raw = buildRawShape();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            shapeCache.put(dir, rotateShape(Direction.NORTH, dir, raw));
        }
    }

    private VoxelShape buildRawShape() {
        VoxelShape combined = Shapes.empty();
        for (BBCube cube : getBBCubes()) {
            combined = Shapes.or(combined, Block.box(
                    8 + cube.posX, cube.posY, 8 + cube.posZ,
                    8 + cube.posX + cube.width, cube.posY + cube.height, 8 + cube.posZ + cube.depth
            ));
        }
        return combined;
    }

    protected List<BBCube> getBBCubes() {
        return List.of(new BBCube(-8, 0, -8, 16, 8, 16));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shapeCache.getOrDefault(state.getValue(FACING), Shapes.block());
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return this.getShape(state, level, pos, CollisionContext.empty());
    }

    // --- 骑乘参数与逻辑 ---

    protected double getBaseOffsetY() { return 1.0; }
    protected double getBaseOffsetX() { return 0.5; }
    protected double getBaseOffsetZ() { return 0.5; }
    protected double getPushBack() { return 0.0; }
    protected float getSeatYaw(Direction facing) { return facing.toYRot(); }

    @Override
    @NotNull
    public InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            if (mount(level, pos, player)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean mount(Level level, BlockPos pos, Entity target) {
        if (level.isClientSide) return false;

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RestraintDevice block)) return false;

        if (!block.canMount(level, pos, target)) return false;

        Direction facing = state.getValue(FACING);

        double push = block.getPushBack();
        double finalX = pos.getX() + block.getBaseOffsetX() + (facing.getStepX() * push);
        double finalZ = pos.getZ() + block.getBaseOffsetZ() + (facing.getStepZ() * push);
        double finalY = pos.getY() + Math.min(block.getBaseOffsetY(), 0.95);

        float finalYaw = block.getSeatYaw(facing);

        SeatEntity seat = new SeatEntity(level, finalX, finalY, finalZ);
        seat.setYRot(finalYaw);
        level.addFreshEntity(seat);

        if (target.startRiding(seat)) {
            target.setYRot(finalYaw);
            target.setXRot(0);

            if (target instanceof LivingEntity living) {
                living.yBodyRot = finalYaw;
                living.yBodyRotO = finalYaw;
                living.setYHeadRot(finalYaw);
                living.yHeadRotO = finalYaw;
            }

            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        finalYaw, 0,
                        Collections.emptySet()
                );
            }
            return true;
        }
        return false;
    }

    public boolean canMount(Level level, BlockPos pos, Entity target) {
        List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.1));
        return seats.isEmpty();
    }

    public boolean canDismount(Level level, BlockPos pos, Entity rider) { return true; }
    public void onMount(LivingEntity entity, Level level, BlockPos pos) {}
    public void onDismount(LivingEntity entity, Level level, BlockPos pos) {}
    public void onRiderTick(LivingEntity entity, Level level, BlockPos pos) {}

    // --- 幽灵方块管理 ---

    public boolean canPlaceAt(@NotNull BlockState state, LevelReader level, BlockPos pos) {
        VoxelShape shape = getShape(state, level, pos, CollisionContext.empty());
        if (shape.isEmpty()) return true;

        AABB bounds = shape.bounds();
        for (BlockPos targetPos : BlockPos.betweenClosed(
                pos.offset((int) Math.floor(bounds.minX + 0.001), (int) Math.floor(bounds.minY + 0.001), (int) Math.floor(bounds.minZ + 0.001)),
                pos.offset((int) Math.ceil(bounds.maxX - 0.001) - 1, (int) Math.ceil(bounds.maxY - 0.001) - 1, (int) Math.ceil(bounds.maxZ - 0.001) - 1)
        )) {
            if (targetPos.equals(pos)) continue;
            BlockState targetState = level.getBlockState(targetPos);
            if (!targetState.isAir() && !targetState.canBeReplaced()) return false;
        }
        return true;
    }

    protected void placeGhostBlocks(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        VoxelShape shape = getShape(state, level, pos, CollisionContext.empty());
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            for (int x = (int) Math.floor(x1 + 0.001); x < (int) Math.ceil(x2 - 0.001); x++) {
                for (int y = (int) Math.floor(y1 + 0.001); y < (int) Math.ceil(y2 - 0.001); y++) {
                    for (int z = (int) Math.floor(z1 + 0.001); z < (int) Math.ceil(z2 - 0.001); z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            BlockPos target = pos.offset(x, y, z);
                            level.setBlock(target, ModBlocks.GHOST_BLOCK.get().defaultBlockState(), 3);
                            if (level.getBlockEntity(target) instanceof GhostBlockEntity ghostBE) {
                                ghostBE.setMasterPos(pos);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 自适应清理幽灵方块
     */
    protected void removeGhostBlocks(Level level, BlockPos pos, BlockState state) {
        VoxelShape shape = shapeCache.get(state.getValue(FACING));
        if (shape == null) {
            shape = getShape(state, level, pos, CollisionContext.empty());
        }

        if (shape.isEmpty()) return;
        AABB bounds = shape.bounds();

        int minX = (int) Math.floor(bounds.minX + 0.001);
        int maxX = (int) Math.ceil(bounds.maxX - 0.001);
        int minY = (int) Math.floor(bounds.minY + 0.001);
        int maxY = (int) Math.ceil(bounds.maxY - 0.001);
        int minZ = (int) Math.floor(bounds.minZ + 0.001);
        int maxZ = (int) Math.ceil(bounds.maxZ - 0.001);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos targetPos = pos.offset(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.is(ModBlocks.GHOST_BLOCK.get())) {
                        if (level.getBlockEntity(targetPos) instanceof GhostBlockEntity ghostBE) {
                            if (pos.equals(ghostBE.getMasterPos())) {
                                level.removeBlock(targetPos, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        placeGhostBlocks(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.1));
                for (SeatEntity seat : seats) {
                    seat.ejectPassengers();
                    seat.discard();
                    level.updateNeighborsAt(pos, state.getBlock());
                }
                removeGhostBlocks(level, pos, state);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return canPlaceAt(state, level, pos);
    }

    // --- 红石逻辑 ---

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) { return true; }

    @Override
    protected int getSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return state.getAnalogOutputSignal( (Level) level, pos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        LivingEntity rider = null;
        return getSignalStrength(state, level, pos, rider);
    }

    protected int getSignalStrength(BlockState state, Level level, BlockPos pos, @Nullable LivingEntity entity) {
        return 0;
    }

    // --- 内部辅助工具 ---

    protected static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = {shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((x1, y1, z1, x2, y2, z2) -> buffer[1] = Shapes.or(buffer[1],
                    Block.box(16 * (1 - z2), 16 * y1, 16 * x1, 16 * (1 - z1), 16 * y2, 16 * x2)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    public record BBCube(double posX, double posY, double posZ, double width, double height, double depth) {}
}