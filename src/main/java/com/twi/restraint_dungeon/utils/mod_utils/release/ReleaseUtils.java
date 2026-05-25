package com.twi.restraint_dungeon.utils.mod_utils.release;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.NULL_UUID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.isKidnappingActive;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.isChangingPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;

public class ReleaseUtils {

    public static final double DEFAULT_RELEASE_DISTANCE = 2.0;
    public static final long DEFAULT_RELEASE_TIME = 5000;

    public enum ReleaseDropType{
        DROP_TO_ACTION_ENTITY,
        DROP_TO_TARGET,
        DROP_ON_GROUND
    }

    public record ReleaseResult(boolean canRelease, Component messageKey) {
        public static final ReleaseResult SUCCESS = new ReleaseResult(true, Component.empty());
        public static ReleaseResult fail(Component key) { return new ReleaseResult(false, key); }
    }

    public static boolean isReleaseActive(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_RELEASE).isReleasing();
    }

    public static boolean isReleaser(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_RELEASE).isReleaser();
    }

    public static float getReleaseProgress(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_RELEASE).getProgress();
    }

    public static UUID getPartnerUUID(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_RELEASE).getPartnerUUID().orElse(NULL_UUID);
    }

    public static PlayerRestraintPart getReleasingPart(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_RELEASE).getReleasingPart();
    }

    /**
     * 获取渲染用的物品
     */
    public static ItemStack getReleasingItem(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_RELEASE).getReleasingItem();
    }

    public static ItemStack getReleaseTool(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;
        return entity.getData(ModAttachments.ENTITY_RELEASE).getReleaseTool();
    }

    /**
     * 统一更新释放数据
     */
    public static void updateReleaseData(LivingEntity entity, boolean isReleaser, float progress, @Nullable UUID partner,ItemStack stack,PlayerRestraintPart releasingPart,ItemStack tool) {
        if (entity == null) return;

        var cap = entity.getData(ModAttachments.ENTITY_RELEASE);

        cap.setReleasing(true);
        cap.setReleaser(isReleaser);
        cap.setProgress(progress);
        cap.setPartnerUUID(partner);
        cap.setReleasingItem(stack.copy());
        cap.setReleasingPart(releasingPart);
        cap.setReleaseTool(tool.copy());

        entity.setData(ModAttachments.ENTITY_RELEASE, cap);
    }

    /**
     * 更新释放进度（用于 Tick 过程中）
     */
    public static void updateProgress(LivingEntity entity, float progress) {
        if (entity == null) return;
        var cap = entity.getData(ModAttachments.ENTITY_RELEASE);
        cap.setProgress(progress);
        entity.setData(ModAttachments.ENTITY_RELEASE, cap);
    }

    /**
     * 清除释放状态
     */
    public static void clearReleaseData(LivingEntity entity) {
        if (entity == null) return;

        var cap = entity.getData(ModAttachments.ENTITY_RELEASE);

        cap.setReleasing(false);
        cap.setReleaser(false);
        cap.setProgress(0.0f);
        cap.setPartnerUUID(null);
        cap.setReleasingItem(ItemStack.EMPTY);
        cap.setReleasingPart(PlayerRestraintPart.restraint_connection);
        cap.setReleaseTool(ItemStack.EMPTY);

        entity.setData(ModAttachments.ENTITY_RELEASE, cap);
    }

    /**
     * 检查操作者能否释放目标
     */
    public static ReleaseResult targetCanBeRelease(LivingEntity target, LivingEntity actionEntity) {
        // 自身限制检查
        if (isBeenBindArms(actionEntity) || isBeenBindHands(actionEntity)) {
            return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.has_been_bind").withStyle(ChatFormatting.RED));
        }

        // 手持物品检查
        // TODO:完成释放工具后更换

//        if(!actionEntity.getMainHandItem().isEmpty() || !(actionEntity.getMainHandItem().getItem() instanceof ReleaseToolItem)){}
        if(!actionEntity.getMainHandItem().isEmpty()){
            return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.need_main_hand_empty_or_release_tool").withStyle(ChatFormatting.RED));
        }

        // 正在释放其他目标的检查
        if (isReleaseActive(actionEntity) && isReleaser(actionEntity)) {
            if (!getPartnerUUID(actionEntity).equals(target.getUUID())) {
                return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.already_releasing").withStyle(ChatFormatting.RED));
            }
        }

        // 目标状态检查
        if (getIsStruggling(target)) {
            return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.need_stop_struggle").withStyle(ChatFormatting.RED));
        }

        // 目标冲突检查
        if (isKidnappingActive(target)
                || (isReleaseActive(target) && !getPartnerUUID(target).equals(actionEntity.getUUID()))) {
            return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.target_is_being_occupy").withStyle(ChatFormatting.RED));
        }

        // 发起者状态冲突
        if (isKidnappingActive(actionEntity)
                || getIsStruggling(actionEntity)
                || isChangingPosition(actionEntity)
                || (actionEntity instanceof Player actionPlayer && isPlayerRestraintMoving(actionPlayer))) {
            return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.cant_releasing_state").withStyle(ChatFormatting.RED));
        }

        // 执行部位与具体物品检查
        return checkRestraintStack(target, actionEntity);
    }

    private static ReleaseResult checkRestraintStack(LivingEntity target, LivingEntity actionEntity) {
        PlayerRestraintPart part = getEntityTargetPart(actionEntity);

        // 获取该部位所有拘束具
        List<ItemStack> restraints = getAllPartRestraint(target, part);
        if (restraints.isEmpty()) {
            return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.no_restraint").withStyle(ChatFormatting.RED));
        }


        int lastIndex = restraints.size() - 1;
        ItemStack stack = restraints.get(lastIndex);

        if (stack.getItem() instanceof RestraintItem ri) {
            // 锁定检查
            if (!ri.getLockType(target,stack).isEmpty()) {
                return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.has_been_locked").withStyle(ChatFormatting.RED));
            }

            // 距离检查
            double dist = actionEntity.distanceTo(target);
            if (dist > ri.getReleaseDistance(actionEntity, target, stack, part)) {
                return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.too_far").withStyle(ChatFormatting.RED));
            }

            // 特殊逻辑：连接类拘束具
            if (part == PlayerRestraintPart.restraint_connection) {
                Component customMsg = ri.canConnectRelease(actionEntity, target, stack);
                return customMsg == null ? ReleaseResult.SUCCESS : ReleaseResult.fail(customMsg);
            }

            // 通用 Release 检查回调
            Component canReleaseMsg = ri.canBeReleased(actionEntity, target, stack, part, lastIndex);
            return canReleaseMsg == null ? ReleaseResult.SUCCESS : ReleaseResult.fail(canReleaseMsg);
        }

        return ReleaseResult.fail(Component.translatable("event.restraint_dungeon.release.no_restraint").withStyle(ChatFormatting.RED));
    }


    /**
     * 实际执行释放操作
     */
    public static void executeRelease(LivingEntity actionEntity, LivingEntity target) {
        PlayerRestraintPart part = getEntityTargetPart(actionEntity);
        List<ItemStack> restraints = getAllPartRestraint(target, part);

        if (restraints.isEmpty()) return;

        int slotIndex = restraints.size() - 1;
        ItemStack stack = restraints.get(slotIndex);

        if (stack.getItem() instanceof RestraintItem ri) {
            // 从数据层移除
            boolean removed = !removeRestraintItemByIndex(target, part, slotIndex).isEmpty();

            if (removed) {
                // 处理掉落逻辑
                handleReleaseDrop(actionEntity, target, stack, ri, part, slotIndex);

                // 发送反馈消息
                if (actionEntity instanceof Player actionPlayer) {
                    actionPlayer.displayClientMessage(Component.translatable("event.restraint_dungeon.release.done").withStyle(ChatFormatting.GREEN), true);
                }
                if (target instanceof Player targetPlayer) {
                    targetPlayer.displayClientMessage(Component.translatable("event.restraint_dungeon.release.released").withStyle(ChatFormatting.GREEN), true);
                }

                ri.onReleaseOff(actionEntity, target, stack, part, slotIndex);
            }
        }
    }

    private static void handleReleaseDrop(LivingEntity actionEntity, LivingEntity target, ItemStack stack, RestraintItem ri, PlayerRestraintPart part, int slot) {

        if (!ri.dropRestraintWhenRelease(actionEntity, target, stack, part, slot)) return;

        ReleaseDropType dropType = ri.dropReleaseItemDirection(actionEntity, target, stack, part, slot);

        if (dropType == ReleaseDropType.DROP_TO_ACTION_ENTITY ) {
            if(actionEntity instanceof Player actionPlayer){
                int selectedSlot = actionPlayer.getInventory().selected;
                ItemStack currentInHand = actionPlayer.getInventory().getItem(selectedSlot);
                if (currentInHand.isEmpty()){
                    actionPlayer.getInventory().setItem(selectedSlot, stack.copy());
                }else if(!actionPlayer.getInventory().add(stack.copy())){
                    actionPlayer.drop(stack.copy(), false);
                }
            }
        } else if (dropType == ReleaseDropType.DROP_TO_TARGET) {
            if(target instanceof Player targetPlayer){
                if (!targetPlayer.getInventory().add(stack.copy())) {
                    targetPlayer.drop(stack.copy(), false);
                }
            }
        } else {
            ItemEntity itemEntity = new ItemEntity(target.level(), target.getX(), target.getY() + 0.5, target.getZ(), stack.copy());
            itemEntity.setDeltaMovement(
                    (target.getRandom().nextDouble() - 0.5) * 0.5,
                    0.2,
                    (target.getRandom().nextDouble() - 0.5) * 0.5
            );
            target.level().addFreshEntity(itemEntity);
        }
    }
}
