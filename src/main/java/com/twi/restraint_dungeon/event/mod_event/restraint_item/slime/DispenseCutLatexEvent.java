package com.twi.restraint_dungeon.event.mod_event.restraint_item.slime;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.LatexItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRidingEntity;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isBeenRiding;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.removeRestraintItem;

@EventBusSubscriber(modid = MODID)
public class DispenseCutLatexEvent {
    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DispenseCutLatexEvent::registerDispenserBehavior);
    }

    private static void registerDispenserBehavior() {
        DispenseItemBehavior latexCutBehavior = new DispenseItemBehavior() {
            @Override
            public @NotNull ItemStack dispense(BlockSource blockSource, @NotNull ItemStack itemStack) {
                ServerLevel level = blockSource.level();
                Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
                BlockPos targetPos = blockSource.pos().relative(direction);

                LivingEntity finalTarget = null;
                boolean isTargetOnRestraintDevice = false;
                boolean hasIntercepted = false;

                BlockPos actualDevicePos = targetPos;
                if (level.getBlockEntity(targetPos) instanceof GhostBlockEntity ghostBE) {
                    BlockPos master = ghostBE.getMasterPos();
                    if (master != null) actualDevicePos = master;
                }

                BlockState targetState = level.getBlockState(actualDevicePos);
                if (targetState.getBlock() instanceof RestraintDevice) {
                    hasIntercepted = true;
                    if (isBeenRiding(level, actualDevicePos)) {
                        LivingEntity ridingEntity = getRidingEntity(level, actualDevicePos);
                        if (ridingEntity != null) {
                            finalTarget = ridingEntity;
                            isTargetOnRestraintDevice = true;
                        }
                    }
                }

                if (finalTarget == null) {
                    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(targetPos));
                    if (!entities.isEmpty()) {
                        finalTarget = entities.get(0);
                        hasIntercepted = true;
                    }
                }

                if (finalTarget != null) {
                    boolean success;
                    PlayerRestraintPart[] parts = {
                            PlayerRestraintPart.restraint_blindfold,
                            PlayerRestraintPart.restraint_gag,
                            PlayerRestraintPart.restraint_hands_bind,
                            PlayerRestraintPart.restraint_arms_bind,
                            PlayerRestraintPart.restraint_body_bind,
                            PlayerRestraintPart.restraint_legs_bind
                    };

                    for (PlayerRestraintPart bodyPart : parts) {
                        success = cutLatexByDispense(finalTarget, bodyPart, itemStack, isTargetOnRestraintDevice);
                        if (success) {
                            if (itemStack.isDamageableItem()) {
                                itemStack.hurtAndBreak(1, level, null, item -> itemStack.setCount(0));
                            }
                            return itemStack;
                        }
                    }
                }

                if (hasIntercepted) {

                    level.playSound(null, blockSource.pos(), SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F);
                    return itemStack;
                }

                return new DefaultDispenseItemBehavior().dispense(blockSource, itemStack);
            }
        };

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof SwordItem) {
                DispenserBlock.registerBehavior(item, latexCutBehavior);
            }
        }
    }

    private static boolean cutLatexByDispense(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack itemStack, boolean isRestraintDevice) {
        if (getAllPartRestraint(entity, bodyPart).isEmpty()) return false;

        List<ItemStack> stacks = getAllPartRestraint(entity, bodyPart);

        if (stacks.isEmpty()
                || !(itemStack.getItem() instanceof SwordItem)
                || !(getPartLastRestraint(entity, bodyPart).getItem() instanceof LatexItem)) return false;

        ItemStack latex = getPartLastRestraint(entity, bodyPart).copy();

        entity.level().playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.PLAYERS,
                0.8F,
                1.0F
        );

        if (isRestraintDevice) {

            ItemEntity itemEntity = new ItemEntity(
                    entity.level(), entity.getX(), entity.getY() + 0.2, entity.getZ(),
                    latex
            );

            itemEntity.setDeltaMovement(0, 0, 0);

            itemEntity.setNoPickUpDelay();

            entity.level().addFreshEntity(itemEntity);
        } else {
            ItemEntity itemEntity = new ItemEntity(
                    entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                    latex
            );
            itemEntity.setDeltaMovement(
                    (entity.getRandom().nextDouble() - 0.5) * 1.5,
                    entity.getRandom().nextDouble() * 1.5 + 0.1,
                    (entity.getRandom().nextDouble() - 0.5) * 1.5
            );
            entity.level().addFreshEntity(itemEntity);
        }

        removeRestraintItem(entity, bodyPart);
        return true;
    }
}
