package com.twi.restraint_dungeon.compat.jade;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDeviceEntity;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintLockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public enum RestraintDeviceComponentProvider implements
        IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;

  @Override
  public void appendTooltip(
    ITooltip tooltip,
    BlockAccessor accessor,
    IPluginConfig config
  ) {
    if (accessor.getServerData().contains("lock_type")) {
      tooltip.add(
              Component.translatable("block." + MODID + ".restraint_device.lock_type").withStyle(ChatFormatting.WHITE)
                      .append(accessor.getServerData().getString("lock_type")).withStyle(ChatFormatting.GOLD)
      );
      if(accessor.getServerData().contains("lock_info")){

          if(accessor.getServerData().contains("id_use_magic_font") && accessor.getServerData().getBoolean("id_use_magic_font")) {
            Style magicStyle = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt")).withColor(ChatFormatting.GOLD);
            tooltip.add(Component.literal(accessor.getServerData().getString("lock_info_prefix")).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(accessor.getServerData().getString("lock_info")).withStyle(magicStyle)
                    ));
          }else{
            tooltip.add(Component.literal(accessor.getServerData().getString("lock_info_prefix")).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(accessor.getServerData().getString("lock_info")).withStyle(ChatFormatting.GOLD)
                    ));
          }
      }
    }
  }

  @Override
  public void appendServerData(CompoundTag data, BlockAccessor accessor) {
    BlockEntity blockEntity = accessor.getBlockEntity();
    RestraintDeviceEntity deviceBE = null;

    if (blockEntity instanceof RestraintDeviceEntity directBE) {
      deviceBE = directBE;
    } else if (blockEntity instanceof GhostBlockEntity ghostBE) {
      BlockPos mPos = ghostBE.getMasterPos();
      if (mPos != null && !mPos.equals(BlockPos.ZERO)) {
        BlockEntity masterBE = accessor.getLevel().getBlockEntity(mPos);
        if (masterBE instanceof RestraintDeviceEntity targetBE) {
          deviceBE = targetBE;
        }
      }
    }

    if (deviceBE != null) {
      ItemStack lockStack = deviceBE.getLockType();
      if(lockStack.getItem() instanceof RestraintLockItem lock){
        data.putString("lock_type",lock.getName(lockStack).getString());
        if(lock.getPairingID(lockStack) != null && lock.getShowPairingInfoOnDevice(lockStack) != null){
          data.putBoolean("id_use_magic_font",lock.shouldPairingIDUseMagicFont(lockStack));
          data.putString("lock_info_prefix",lock.getShowPairingPrefixOnDevice(lockStack));
          data.putString("lock_info",lock.getShowPairingInfoOnDevice(lockStack));
        }
      }
    }

  }

  @Override
  public ResourceLocation getUid() {
    return ResourceLocation.fromNamespaceAndPath(MODID, "device_lock_info");
  }
}