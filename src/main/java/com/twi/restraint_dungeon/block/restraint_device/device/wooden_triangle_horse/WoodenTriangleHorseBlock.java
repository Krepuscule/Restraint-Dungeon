package com.twi.restraint_dungeon.block.restraint_device.device.wooden_triangle_horse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRidingEntity;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;

public class WoodenTriangleHorseBlock extends RestraintDevice {

    public static final MapCodec<WoodenTriangleHorseBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    propertiesCodec(),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("block_entity_type")
                            .forGetter(b -> b.blockEntityType.get()),
                    Codec.STRING.fieldOf("wood_type").forGetter(b -> b.woodType)
            ).apply(inst, (props, type, wood) -> new WoodenTriangleHorseBlock(props, () -> (BlockEntityType<? extends WoodenTriangleHorseBlockEntity>) type, wood))
    );

    protected final String woodType;

    public WoodenTriangleHorseBlock(Properties properties, Supplier<? extends BlockEntityType<? extends WoodenTriangleHorseBlockEntity>> beType, String woodType) {
        super(properties, beType);
        this.woodType = woodType;
        this.runInitializeShapes();
    }

    @Override
    public String getID(){
        return "WOODEN_TRIANGLE_HORSE";
    }

    @Override
    protected @NotNull MapCodec<? extends WoodenTriangleHorseBlock> codec() {
        return CODEC;
    }

    @Override
    protected List<BBCube> getBBCubes() {
        return List.of(
                new BBCube(-8,16,-8,16,1,16),
                new BBCube(-7,17,-8,15,1,16),
                new BBCube(-6,18,-8,13,1,16),
                new BBCube(-5,19,-8,11,1,16),
                new BBCube(-4,20,-8,9,1,16),
                new BBCube(-3,21,-8,7,1,16),
                new BBCube(-2,22,-8,5,1,16),
                new BBCube(-1,23,-8,3,1,16),
                new BBCube(0,24,-8,1,1,16),

                new BBCube(-2,2,-2,4,14,4),

                new BBCube(-8, 0, -8, 16, 2, 16)
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WoodenTriangleHorseBlockEntity.Variant(pos, state, this.woodType);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public double getBaseOffsetY(Entity passenger) {
        if(passenger instanceof BaseNPCEntity npc){
            return 0.8;
        }
        return 1.5;
    }


    @Override
    public Vector3f getRiderFirstCameraOffset(){
        return new Vector3f(0.0f,0.2f,0.1f);
    }

    @Override
    public boolean canBindHands(@NotNull BlockState state, @NotNull BlockPos pos){
        return false;
    }

    @Override
    public boolean canBindArms(@NotNull BlockState state, @NotNull BlockPos pos){
        return false;
    }

    @Override
    public boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        if (level instanceof Level world) {
            return this.getAnalogOutputSignal(state, world, pos);
        }
        return 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        LivingEntity rider = getRidingEntity(level, pos);
        return getSignalStrength(state,level,pos,rider);
    }

    @Override
    protected int getSignalStrength(BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        // TODO:完成NPC系统后补充
//        if (entity instanceof Player || entity instanceof BaseNPCEntity)
        if(entity instanceof Player) {
            return (int) getPleasantValue(entity) / 10;
        } 

        return 0;
    }
}