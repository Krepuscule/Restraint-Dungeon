package com.twi.restraint_dungeon.block.restraint_device.device.doll_stand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRidingEntity;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllRestraint;

public class DollStandBlock extends RestraintDevice {
    // 材质映射 Codec
    public static final MapCodec<DollStandBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    propertiesCodec(),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("block_entity_type")
                            .forGetter(b -> b.blockEntityType.get()),
                    Codec.STRING.fieldOf("material_type").forGetter(b -> b.materialType)
            ).apply(inst, (props, type, mat) -> new DollStandBlock(props, () -> (BlockEntityType<? extends DollStandBlockEntity>) type, mat))
    );

    protected final String materialType;

    public DollStandBlock(Properties properties, Supplier<? extends BlockEntityType<? extends DollStandBlockEntity>> beType, String materialType) {
        super(properties, beType);
        this.materialType = materialType;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.runInitializeShapes();
    }

    @Override
    protected @NotNull MapCodec<? extends DollStandBlock> codec() {
        return CODEC;
    }

    @Override
    protected List<BBCube> getBBCubes() {
        return List.of(new BBCube(-8, 0, -8, 16, 22, 16));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DollStandBlockEntity.Variant(pos, state, this.materialType);
    }

    // --- 红石逻辑适配 ---

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
        return getSignalStrength(rider);
    }

    protected int getSignalStrength(LivingEntity entity) {
        if (entity instanceof Player player) {
            return Math.min(getAllRestraint(player).size(), 15);
        }
        return 0;
    }

    @Override protected double getBaseOffsetY() { return 0.2; }
    @Override protected double getBaseOffsetX() { return 0.5; }
    @Override protected double getBaseOffsetZ() { return 0.5; }
}