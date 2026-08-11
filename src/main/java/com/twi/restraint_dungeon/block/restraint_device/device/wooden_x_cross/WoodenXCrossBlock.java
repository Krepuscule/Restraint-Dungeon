package com.twi.restraint_dungeon.block.restraint_device.device.wooden_x_cross;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class WoodenXCrossBlock extends RestraintDevice {

    public static final MapCodec<WoodenXCrossBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    propertiesCodec(),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("block_entity_type")
                            .forGetter(b -> b.blockEntityType.get()),
                    Codec.STRING.fieldOf("wood_type").forGetter(b -> b.woodType)
            ).apply(inst, (props, type, wood) -> new WoodenXCrossBlock(props, () -> (BlockEntityType<? extends WoodenXCrossBlockEntity>) type, wood))
    );

    protected final String woodType;

    public WoodenXCrossBlock(Properties properties, Supplier<? extends BlockEntityType<? extends WoodenXCrossBlockEntity>> beType, String woodType) {
        super(properties, beType);
        this.woodType = woodType;
        this.runInitializeShapes();
    }

    @Override
    public String getID(){
        return "WOODEN_X_CROSS";
    }

    @Override
    protected @NotNull MapCodec<? extends WoodenXCrossBlock> codec() {
        return CODEC;
    }

    @Override
    protected List<BBCube> getBBCubes() {

        return List.of(
                new BBCube(-18, 2, -1, 36, 41, 2),

                new BBCube(-8,0,-8,16,2,16)
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WoodenXCrossBlockEntity.Variant(pos, state, this.woodType);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public double getBaseOffsetZ(Entity passenger) {
        return -0.25;
    }

    @Override
    public double getBaseOffsetY(Entity passenger) {

        if(passenger instanceof BaseNPCEntity npc){
            return 0.3;
        }

        return 1.0;
    }

    @Override
    public Vector3f getRiderFirstCameraOffset(){
        return new Vector3f(0.0f,0.2f,0.1f);
    }

    @Override
    public boolean canBindHands(@NotNull BlockState state, @NotNull BlockPos pos){
        return false;
    }
}