package com.twi.restraint_dungeon.utils.block_utils;

import com.twi.restraint_dungeon.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BlockUtils {

    private static final List<Block> CUT_BLOCKS = List.of(
            ModBlocks.PLACED_SWORD.get()
    );
    private static final List<Block> HOOK_BLOCKS = List.of(
            Blocks.TRIPWIRE_HOOK
    );

    /**
     * 检测实体周围是否存在插剑方块
     * @param entity 需要检测的生物
     * @return 是否靠近插剑方块
     */
    public static boolean isNearPlacedSword(LivingEntity entity) {
        Level level = entity.level();
        AABB searchBox = entity.getBoundingBox().inflate(1.0, 1.0, 1.0);

        int minX = Mth.floor(searchBox.minX);
        int maxX = Mth.floor(searchBox.maxX);
        int minY = Mth.floor(searchBox.minY);
        int maxY = Mth.floor(searchBox.maxY);
        int minZ = Mth.floor(searchBox.minZ);
        int maxZ = Mth.floor(searchBox.maxZ);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();


        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState state = level.getBlockState(mutablePos);

                    if (CUT_BLOCKS.contains(state.getBlock())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检测实体周围是否存在绊线钩
     * @param entity 需要检测的生物
     * @return 是否靠近绊线钩
     */
    public static boolean isNearTripwireHook(LivingEntity entity) {
        Level level = entity.level();
        AABB searchBox = entity.getBoundingBox().inflate(1.0, 1.0, 1.0);

        int minX = Mth.floor(searchBox.minX);
        int maxX = Mth.floor(searchBox.maxX);
        int minY = Mth.floor(searchBox.minY);
        int maxY = Mth.floor(searchBox.maxY);
        int minZ = Mth.floor(searchBox.minZ);
        int maxZ = Mth.floor(searchBox.maxZ);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState state = level.getBlockState(mutablePos);

                    if (HOOK_BLOCKS.contains(state.getBlock())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
