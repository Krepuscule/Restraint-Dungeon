package com.twi.restraint_dungeon.block.restraint_device.device.wooden_triangle_horse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

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
    protected @NotNull MapCodec<? extends WoodenTriangleHorseBlock> codec() {
        return CODEC;
    }

    @Override
    protected List<BBCube> getBBCubes() {
        return List.of(
                new BBCube(-1,30,-8,2,1,16),
                new BBCube(-2,29,-8,4,1,16),
                new BBCube(-2,28,-8,4,1,16),
                new BBCube(-3,27,-8,6,1,16),
                new BBCube(-3,26,-8,6,1,16),
                new BBCube(-4,25,-8,8,1,16),
                new BBCube(-4,24,-8,8,1,16),
                new BBCube(-5,23,-8,10,1,16),
                new BBCube(-5,22,-8,10,1,16),
                new BBCube(-6,21,-8,12,1,16),
                new BBCube(-6,20,-8,12,1,16),
                new BBCube(-7,19,-8,14,1,16),
                new BBCube(-7,18,-8,14,1,16),
                new BBCube(-8,17,-8,16,1,16),
                new BBCube(-8,16,-8,16,1,16),
                new BBCube(-2, 2, -2, 4, 14, 4),
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

    @Override protected double getBaseOffsetY() { return 1.0; }
    @Override protected double getBaseOffsetX() { return 0.5; }
    @Override protected double getBaseOffsetZ() { return 0.5; }

    /**
     * 重写红石信号强度逻辑
     */
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