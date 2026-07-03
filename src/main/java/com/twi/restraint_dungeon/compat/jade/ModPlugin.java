package com.twi.restraint_dungeon.compat.jade;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDeviceEntity;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlock;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.*;

@WailaPlugin
public class ModPlugin implements IWailaPlugin {

  @Override
  public void register(IWailaCommonRegistration registration) {
      registration.registerBlockDataProvider(RestraintDeviceComponentProvider.INSTANCE, RestraintDeviceEntity.class);
    registration.registerBlockDataProvider(RestraintDeviceComponentProvider.INSTANCE, GhostBlockEntity.class);
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {

    registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {

      if (accessor instanceof BlockAccessor blockAccessor) {

        if (blockAccessor.getBlock() instanceof GhostBlock) {

          if (blockAccessor.getBlockEntity() instanceof GhostBlockEntity ghostBE) {
            BlockPos mPos = ghostBE.getMasterPos();

            if (mPos != null && !mPos.equals(BlockPos.ZERO)) {
              BlockState mState = blockAccessor.getLevel().getBlockState(mPos);

              if (mState.getBlock() instanceof RestraintDevice) {
                return registration.blockAccessor()
                        .from(blockAccessor)
                        .blockState(mState)
                        .blockEntity(blockAccessor.getLevel().getBlockEntity(mPos))
                        .build();
              }
            }
          }
        }
      }
      return accessor;
    });

    registration.registerBlockComponent(RestraintDeviceComponentProvider.INSTANCE, RestraintDevice.class);
  }
}