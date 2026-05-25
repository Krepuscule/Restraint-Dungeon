package com.twi.restraint_dungeon.utils.block_utils;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RestraintDeviceUtils {

    /**
     * 检查实体是否正骑乘在任何 RestraintDevice 上
     */
    public static boolean isRidingRestraintDevice(LivingEntity entity) {
        return getRestraintDevice(entity) != null;
    }

    /**
     * 获取实体正在骑乘的 RestraintDevice 实例
     */
    @Nullable
    public static Block getRestraintDevice(LivingEntity entity) {

        Entity vehicle = entity.getVehicle();

        if (vehicle instanceof SeatEntity) {
            BlockPos pos = vehicle.blockPosition();
            BlockState state = entity.level().getBlockState(pos);

            if (state.getBlock() instanceof RestraintDevice) {
                return state.getBlock();
            }
        }
        return null;
    }

    /**
     * 检查指定的 RestraintDevice 位置是否正有实体骑乘
     */
    public static boolean isBeenRiding(Level level, BlockPos pos) {
        return searchEntityOnRestraintDevice(level, pos) != null;
    }

    /**
     * 检查指定的 RestraintDevice 上骑乘的实体
     */
    @Nullable
    public static LivingEntity getRidingEntity(Level level, BlockPos pos) {
        BlockPos targetPos = pos;

        // 如果是幽灵方块，先重定向到母块坐标
        if (level.getBlockEntity(pos) instanceof GhostBlockEntity ghostBE) {
            BlockPos masterPos = ghostBE.getMasterPos();
            if (masterPos != null) {
                targetPos = masterPos;
            }
        }

        return searchEntityOnRestraintDevice(level, targetPos);
    }

    /**
     * 内部辅助方法，检查RestraintDevice上的SeatEntity
     */
    @Nullable
    private static LivingEntity searchEntityOnRestraintDevice(Level level, BlockPos pos) {

        List<SeatEntity> seats = level.getEntitiesOfClass(
            SeatEntity.class,
//            new AABB(pos).inflate(0.1)
            new AABB(pos).deflate(0.1)
        );

        for (SeatEntity seat : seats) {
            Entity passenger = seat.getFirstPassenger();
            if (passenger instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}