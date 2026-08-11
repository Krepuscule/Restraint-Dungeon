package com.twi.restraint_dungeon.utils.mod_utils.kidnap;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.RestraintDungeon.NULL_UUID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isBeingCarried;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isCarrier;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.isReleaseActive;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.isReleaser;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.isChangingPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.self_bondage.SelfBondageUtils.isSelfBondaging;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.isPartFull;

public class KidnapUtils {

    public static final double DEFAULT_BIND_DISTANCE = 2.0;
    public static final long DEFAULT_BIND_TIME = 5000;

    public record BindResult(boolean canBind, Component messageKey) {
        public static final BindResult SUCCESS = new BindResult(true, Component.empty());
        public static BindResult fail(Component key) { return new BindResult(false, key); }
    }

    /**
     * 获取玩家当前的绑架/被绑架进度
     */
    public static float getKidnapProgress(LivingEntity entity) {
        if (entity == null) return 0.0F;
        return entity.getData(ModAttachments.ENTITY_KIDNAP).getProgress();
    }

    /**
     * 获取玩家当前是否处于绑架流程中
     */
    public static boolean isKidnappingActive(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getData(ModAttachments.ENTITY_KIDNAP).isKidnapping();
    }

    /**
     * 获取玩家是否为绑架发起者
     */
    public static boolean isKidnapper(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getData(ModAttachments.ENTITY_KIDNAP).isKidnapper();
    }

    /**
     * 获取绑架对方的 UUID
     */
    public static UUID getPartnerUUID(LivingEntity entity) {
        if (entity == null) return NULL_UUID;
        return entity.getData(ModAttachments.ENTITY_KIDNAP).getPartnerUUID().orElse(NULL_UUID);
    }

    /**
     * 获取用于渲染的拘束具物品
     */
    public static ItemStack getKidnappingItem(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;
        return entity.getData(ModAttachments.ENTITY_KIDNAP).getKidnappingItem();
    }

    /**
     * 获取用于渲染的拘束部位
     */
    public static PlayerRestraintPart getKidnapPart(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_KIDNAP).getKidnapPart();
    }


    /**
     * 设置绑架进度
     */
    public static void updateProgress(LivingEntity entity, float progress) {
        if (entity == null) return;
        var cap = entity.getData(ModAttachments.ENTITY_KIDNAP);
        cap.setProgress(progress);
        entity.setData(ModAttachments.ENTITY_KIDNAP, cap);
    }

    /**
     * 统一更新绑架数据（包含渲染所需的物品信息）
     */
    public static void updateKidnapData(LivingEntity entity, boolean isBinder, float progress, @Nullable UUID partner, ItemStack item, PlayerRestraintPart part) {
        if (entity == null) return;

        var cap = entity.getData(ModAttachments.ENTITY_KIDNAP);

        cap.setKidnapping(true);
        cap.setKidnapper(isBinder);
        cap.setProgress(progress);
        cap.setPartnerUUID(partner);
        cap.setKidnappingItem(item.copy());
        cap.setKidnapPart(part);

        entity.setData(ModAttachments.ENTITY_KIDNAP, cap);
    }

    /**
     * 清除所有绑架相关数据
     */
    public static void clearKidnapData(LivingEntity entity) {
        if (entity == null) return;

       var cap = entity.getData(ModAttachments.ENTITY_KIDNAP);

        cap.setKidnapping(false);
        cap.setKidnapper(false);
        cap.setProgress(0.0f);
        cap.setPartnerUUID(null);
        cap.setKidnappingItem(ItemStack.EMPTY);
        cap.setKidnapPart(PlayerRestraintPart.restraint_blindfold);

        entity.setData(ModAttachments.ENTITY_KIDNAP, cap);
    }

    /**
     * 核心判定方法：目标是否可以被绑架/束缚
     */
    public static BindResult targetCanBeBound(ItemStack stack, LivingEntity target, LivingEntity actionEntity,PlayerRestraintPart part) {
        // 自身限制检查
        if (isBeenBindArms(actionEntity) || isBeenBindHands(actionEntity)) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.has_been_bind").withStyle(ChatFormatting.DARK_RED));
        }

        // 基础物品检查
        if (stack.isEmpty() || !(stack.getItem() instanceof RestraintItem restraintItem)) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.need_handle_restraint").withStyle(ChatFormatting.DARK_RED));
        }

        if(isPartFull(target,part)){
            return  BindResult.fail(Component.translatable("event." + MODID + ".kidnap.part_full").withStyle(ChatFormatting.DARK_RED));
        }

        // 检查是否正在捆绑其他目标
        if (isKidnappingActive(actionEntity) && isKidnapper(actionEntity)) {
            if (!getPartnerUUID(actionEntity).equals(target.getUUID())) {
                return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.already_binding").withStyle(ChatFormatting.DARK_RED));
            }
        }

        // 检查目标是否正在被他人捆绑
        if (isKidnappingActive(target) && !isKidnapper(target)) {
            if (!getPartnerUUID(target).equals(actionEntity.getUUID())) {
                return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.target_is_being_binding").withStyle(ChatFormatting.DARK_RED));
            }
        }
        // 检查目标是否正在被他人释放
        if (isReleaseActive(target) && !isReleaser(target)) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.target_is_being_releasing").withStyle(ChatFormatting.DARK_RED));
        }

        // 检查目标是否正在挣扎
        if(getIsStruggling(target)){
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.target_is_struggling").withStyle(ChatFormatting.DARK_RED));
        }

        if(isChangingPosition(target)
                || isDoingAction(target)
                || (target instanceof Player targetPlayer_1 && isPlayerRestraintMoving(targetPlayer_1))
                || (target instanceof Player targetPlayer_2) && isCarrier(targetPlayer_2)
                || isBeingCarried(target)
                || isSelfBondaging(target)){
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.invalid_target").withStyle(ChatFormatting.DARK_RED));
        }

        // 发起者状态冲突
        if (isReleaseActive(actionEntity)
                || getIsStruggling(actionEntity)
                || isSelfBondaging(actionEntity)
                || isChangingPosition(actionEntity)
                || (actionEntity instanceof Player actionPlayer && isPlayerRestraintMoving(actionPlayer))) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.cant_kidnapping_state").withStyle(ChatFormatting.DARK_RED));
        }


        // 距离检查
        if (actionEntity.distanceTo(target) > restraintItem.getKidnapDistance(actionEntity,target,stack,part)) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.too_far").withStyle(ChatFormatting.DARK_RED));
        }

        // 部位匹配检查
        PlayerRestraintPart partSelect = getEntityTargetPart(actionEntity);
        if (!restraintItem.getCanEquipPartList().contains(partSelect)) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.cant_use_on_this_part").withStyle(ChatFormatting.DARK_RED));
        }

        if (target instanceof BaseNPCEntity npc && !npc.canBeKidnap()) {
            return BindResult.fail(Component.translatable("event." + MODID +".kidnap.fail").withStyle(ChatFormatting.DARK_RED));
        }

        // 槽位深度与阻塞判定逻辑
        return checkRestraintStack(stack, target, actionEntity, partSelect);
    }

    private static BindResult checkRestraintStack(ItemStack stack, LivingEntity target, LivingEntity actionEntity, PlayerRestraintPart part) {
        RestraintItem item = (RestraintItem) stack.getItem();

        // 获取当前部位已有的拘束具列表
        List<ItemStack> currentRestraints = getAllPartRestraint(target, part);
        int targetIndex = currentRestraints.size();

        if (part == PlayerRestraintPart.restraint_connection) {
            Component error = item.canConnectBind(actionEntity, target, stack);
            if (error != null) return BindResult.fail(error);

            if (partHasBeenBlocked(target, part, targetIndex) && item.canBeBlockByConnect(target)) {
                return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.block_by_connect").withStyle(ChatFormatting.RED));
            }
            return BindResult.SUCCESS;
        }

        if (part == PlayerRestraintPart.restraint_gag) {
            if (stack.getItem() instanceof RestraintItem gagItem && gagItem.canStuffedGag(target, stack)) {
                for (ItemStack gag : currentRestraints) {
                    if (gag.getItem() instanceof RestraintItem cur_gag && cur_gag.canBlockedGag(target, stack)) {
                        return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.gag_has_been_block").withStyle(ChatFormatting.RED));
                    }
                }
                return BindResult.SUCCESS;
            }else{
                return BindResult.SUCCESS;
            }
        }


        // 物品本身的逻辑前置判定
        Component itemResult = item.canUseKidnap(actionEntity, target, stack, part, targetIndex);
        if (itemResult != null) return BindResult.fail(itemResult);

        //外部/连接阻塞判定：检查其他部位是否有拘束具“遮盖”或“固定”了该部位
        if (partHasBeenBlocked(target, part, targetIndex) && item.canBeBlockByConnect(target)) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.block_by_connect").withStyle(ChatFormatting.RED));
        }

        //  内部阻塞判定：检查该部位索引下层是否有物品阻碍添加
        if (partHasBeenBlockByInnerRestraint(target, part, targetIndex) && item.canBeBlockByInner(target)) {
            return BindResult.fail(Component.translatable("event." + MODID + ".kidnap.block_by_inner").withStyle(ChatFormatting.RED));
        }

        return BindResult.SUCCESS;
    }

    /**
     * 执行绑定逻辑：将拘束具正式添加到目标的 Attachment 栏位中
     * @param actionEntity 绑架发起者
     * @param target 绑架目标
     * @param stack 当前手持的拘束具物品堆栈
     */
    public static void executeBind(LivingEntity actionEntity, LivingEntity target, ItemStack stack) {
        PlayerRestraintPart partSelect = getEntityTargetPart(actionEntity);

        if (!(stack.getItem() instanceof RestraintItem restraintItem)) return;

        ItemStack bindStack = stack.copy();
        bindStack.setCount(1);

        boolean success = addRestraintItem(target, partSelect, bindStack);

        if (success) {
            int index = getRestraintCount(target, partSelect) - 1;

            if (restraintItem.reduceStackWhenKidnapFinish(actionEntity, target, stack, partSelect, index)) {
                stack.shrink(1);
            }

            if(actionEntity instanceof Player actionPlayer){
                actionPlayer.displayClientMessage(
                        Component.translatable("event." + MODID + ".kidnap.done").withStyle(ChatFormatting.YELLOW),
                        true
                );
            }

            if (target instanceof ServerPlayer targetPlayer) {
                targetPlayer.displayClientMessage(
                        Component.translatable("event." + MODID + ".kidnap.kidnapped").withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }

            restraintItem.onKidnapToTarget(actionEntity, target, bindStack, partSelect, index);

            clearKidnapData(actionEntity);
            clearKidnapData(target);
        }
    }
}
