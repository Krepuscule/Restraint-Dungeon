package com.twi.restraint_dungeon.block.restraint_device.device.wooden_cross;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class WoodenCrossBlock extends RestraintDevice {
    public static final MapCodec<WoodenCrossBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    propertiesCodec(),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("block_entity_type")
                            .forGetter(b -> b.blockEntityType.get()),
                    Codec.STRING.fieldOf("wood_type").forGetter(b -> b.woodType)
            ).apply(inst, (props, type, wood) -> new WoodenCrossBlock(props, () -> (BlockEntityType<? extends WoodenCrossBlockEntity>) type, wood))
    );

    protected final String woodType;

    public WoodenCrossBlock(Properties properties, Supplier<? extends BlockEntityType<? extends WoodenCrossBlockEntity>> beType, String woodType) {
        super(properties, beType);
        this.woodType = woodType;
        this.runInitializeShapes();
    }

    @Override
    protected @NotNull MapCodec<? extends WoodenCrossBlock> codec() {
        return CODEC;
    }

    @Override
    protected List<BBCube> getBBCubes() {
        return List.of(
                new BBCube(-8, 0, -8, 16, 2, 16),
                new BBCube(-6, 2, -1, 12, 48, 2),
                new BBCube(-18, 29, -1, 12, 8, 2),
                new BBCube(6, 29, -1, 12, 8, 2)
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WoodenCrossBlockEntity.Variant(pos, state, this.woodType);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override protected double getBaseOffsetY() { return 1.0; }
    @Override protected double getBaseOffsetX() { return 0.5; }
    @Override protected double getBaseOffsetZ() { return 0.5; }
    @Override protected double getPushBack() { return 0.1; }
}