package com.twi.restraint_dungeon.block.restraint_device.device.cage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class CageBlock extends RestraintDevice {
    public static final MapCodec<CageBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    propertiesCodec(),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("block_entity_type")
                            .forGetter(b -> b.blockEntityType.get()),
                    Codec.STRING.fieldOf("material_type").forGetter(b -> b.materialType)
            ).apply(inst, (props, type, mat) -> new CageBlock(props, () -> (BlockEntityType<? extends CageBlockEntity>) type, mat))
    );

    protected final String materialType;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    protected final Map<Direction, VoxelShape> openShapeCache = new EnumMap<>(Direction.class);
    private boolean shapesInitialized = false;

    public CageBlock(Properties properties, Supplier<? extends BlockEntityType<? extends CageBlockEntity>> beType, String materialType) {
        super(properties, beType);
        this.materialType = materialType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    protected @NotNull MapCodec<? extends CageBlock> codec() {
        return CODEC;
    }

    private void ensureShapesInitialized() {
        if (!shapesInitialized) {
            // 1. 处理关闭状态
            VoxelShape closedRaw = buildShapeFromCubes(getClosedBBCubes());
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                shapeCache.put(dir, rotateShape(Direction.NORTH, dir, closedRaw));
            }

            // 2. 处理开启状态
            VoxelShape openRaw = buildShapeFromCubes(getOpenBBCubes());
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                openShapeCache.put(dir, rotateShape(Direction.NORTH, dir, openRaw));
            }
            shapesInitialized = true;
        }
    }

    @Override
    protected void runInitializeShapes() {

    }

    private VoxelShape buildShapeFromCubes(List<BBCube> cubes) {
        VoxelShape combined = Shapes.empty();
        for (BBCube cube : cubes) {
            combined = Shapes.or(combined, Block.box(
                    8.0 + cube.posX(),
                    cube.posY(),
                    8.0 + cube.posZ(),
                    8.0 + cube.posX() + cube.width(),
                    cube.posY() + cube.height(),
                    8.0 + cube.posZ() + cube.depth()
            ));
        }
        return combined;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        ensureShapesInitialized();
        Direction facing = state.getValue(FACING);
        boolean isOpen = state.getValue(OPEN);
        if (isOpen) {
            VoxelShape s = openShapeCache.get(facing);
            return s != null ? s : openShapeCache.get(Direction.NORTH);
        }
        VoxelShape s = shapeCache.get(facing);
        return s != null ? s : shapeCache.get(Direction.NORTH);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state,
                                                 @NotNull BlockGetter level,
                                                 @NotNull BlockPos pos,
                                                 @NotNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state,
                                                   @NotNull BlockGetter level,
                                                   @NotNull BlockPos pos) {
        return getShape(state, level, pos,CollisionContext.empty());
    }

//    private boolean isLockClicked(BlockState state, BlockHitResult hit) {
//        Vec3 rel = hit.getLocation().subtract(Vec3.atLowerCornerOf(hit.getBlockPos()));
//        double y = rel.y;
//        return y >= 1.18 && y <= 1.31;
//    }

    @Override
    @NotNull
    public InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
//        if (isBeenBindArms(player)) {
//            return InteractionResult.PASS;
//        }

        if (!level.isClientSide) {
            if(getLockType(level, pos).isEmpty()){
                boolean nextState = !state.getValue(OPEN);
                level.setBlock(pos, state.setValue(OPEN, nextState), 3);
                float pitch = nextState ? 1.0f : 0.8f;
                level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, pitch);
            }else{
               player.displayClientMessage(Component.translatable("block." + MODID + ".cage.is_locked")
                       .withStyle(ChatFormatting.DARK_RED),true);
            }

        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @NotNull
    public ItemInteractionResult useItemOn(@NotNull ItemStack heldItem,
                                           @NotNull BlockState state,
                                           @NotNull Level level,
                                           @NotNull BlockPos pos,
                                           @NotNull Player player,
                                           @NotNull InteractionHand hand,
                                           @NotNull BlockHitResult hit) {
        if(!state.getValue(OPEN)){
            return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
        }else{
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    public List<LivingEntity> getEntitiesInside(Level level, BlockPos pos) {
        AABB internalArea = new AABB(pos).deflate(0.1);
        return level.getEntitiesOfClass(LivingEntity.class, internalArea);
    }

    protected List<BBCube> getClosedBBCubes() {
        return List.of(
                new BBCube(-8, 37, -8, 16, 2, 16),
                new BBCube(-8, 0, -8, 16, 1, 16),
                new BBCube(-8, 1, -8, 16, 36, 1),
                new BBCube(-8, 1, -8, 1, 36, 16),
                new BBCube(-8, 1, 7, 16, 36, 1),
                new BBCube(7, 1, -8, 1, 36, 16)
        );
    }

    protected List<BBCube> getOpenBBCubes() {
        return List.of(
                new BBCube(-8, 37, -8, 16, 2, 16),
                new BBCube(-8, 0, -8, 16, 1, 16),
                new BBCube(7, 0, -23, 1, 39, 16),
                new BBCube(-8, 1, -8, 1, 36, 16),
                new BBCube(-8, 1, 7, 16, 36, 1),
                new BBCube(7, 1, -8, 1, 36, 16)
        );
    }

    @Override
    protected List<BBCube> getBBCubes() {
        return getClosedBBCubes();
    }

    @Override
    public boolean isMountableDevice(Level level,BlockPos pos){
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(OPEN, false) : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        BlockEntityType<? extends CageBlockEntity> type = (BlockEntityType<? extends CageBlockEntity>) this.blockEntityType.get();
        return type.create(pos, state);
    }
}