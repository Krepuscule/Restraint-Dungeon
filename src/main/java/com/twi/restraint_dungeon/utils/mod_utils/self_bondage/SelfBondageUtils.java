package com.twi.restraint_dungeon.utils.mod_utils.self_bondage;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.SelfBondageCapability;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isBeingCarried;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isCarrier;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.isKidnappingActive;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.isReleaseActive;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.isChangingPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.isPartFull;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils.isMaxToolsEquip;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils.isToolsFull;

public class SelfBondageUtils {

    public record SelfBindResult(boolean canBind, Component message) {
        public static final SelfBindResult SUCCESS = new SelfBindResult(true, Component.empty());
        public static SelfBindResult fail(Component msg) {
            return new SelfBindResult(false, msg);
        }
    }


    public static boolean isSelfBondaging(LivingEntity entity) {
        SelfBondageCapability data = getData(entity);
        return data != null && data.isActive();
    }


    public static float getSelfBondageProgress(LivingEntity entity) {
        SelfBondageCapability data = getData(entity);
        return data != null ? data.getProgress() : 0.0f;
    }

    public static SelfBondageCapability getData(LivingEntity entity) {
        if (entity == null) return null;
        return entity.getData(ModAttachments.ENTITY_SELF_BONDAGE.get());
    }


    public static void updateData(LivingEntity entity, boolean active, float progress) {
        SelfBondageCapability data = getData(entity);
        if (data == null) return;

        data.setActive(active);
        data.setProgress(progress);

        entity.setData(ModAttachments.ENTITY_SELF_BONDAGE.get(), data);
    }

    public static void clearData(LivingEntity entity) {
        updateData(entity, false, 0.0f);
    }

    public static SelfBindResult entityCanBindSelf(LivingEntity entity, ItemStack stack, PlayerRestraintPart part) {

        if(stack.getItem() instanceof RestraintItem restraintItem){

            if (stack.isEmpty()) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.need_restraint_item").withStyle(ChatFormatting.DARK_RED));
            }

            if(isPartFull(entity,part)){
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.part_full").withStyle(ChatFormatting.DARK_RED));
            }


            if (isBeenBindArms(entity) || isBeenBindHands(entity)) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.hands_blocked").withStyle(ChatFormatting.DARK_RED));
            }


            if (isChangingPosition(entity)
                    || getIsStruggling(entity)
                    || isDoingAction(entity)
                    || (entity instanceof Player targetPlayer_1 && isPlayerRestraintMoving(targetPlayer_1))
                    || (entity instanceof Player targetPlayer_2 && isCarrier(targetPlayer_2))
                    || isBeingCarried(entity)
                    || isKidnappingActive(entity)
                    || isReleaseActive(entity)) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.invalid_state"));
            }


            if (!restraintItem.getCanEquipPartList().contains(part)) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.cant_use_on_part").withStyle(ChatFormatting.DARK_RED));
            }

            SelfBindResult innerCheck = checkRestraintStack(stack, entity, entity, part);
            if (!innerCheck.canBind()) {
                return SelfBindResult.fail(innerCheck.message);
            }
        }else{

            if (stack.isEmpty() || (!(stack.getItem() instanceof RestraintToolItem rt))) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.need_restraint_tool").withStyle(ChatFormatting.DARK_RED));
            }

            if(isToolsFull(entity)){
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.restraint_tool_full").withStyle(ChatFormatting.DARK_RED));
            }


            if (isBeenBindArms(entity) || isBeenBindHands(entity)) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.hands_blocked").withStyle(ChatFormatting.DARK_RED));
            }


            if (isChangingPosition(entity)
                    || getIsStruggling(entity)
                    || isDoingAction(entity)
                    || (entity instanceof Player targetPlayer_1 && isPlayerRestraintMoving(targetPlayer_1))
                    || (entity instanceof Player targetPlayer_2 && isCarrier(targetPlayer_2))
                    || isBeingCarried(entity)
                    || isKidnappingActive(entity)
                    || isReleaseActive(entity)) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.invalid_state").withStyle(ChatFormatting.DARK_RED));
            }


            if(!rt.canEquip(entity)){
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.cant_equip_tool").withStyle(ChatFormatting.DARK_RED));
            }

            if(isMaxToolsEquip(entity,stack,false)){
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".self_bondage.has_max_useage").withStyle(ChatFormatting.DARK_RED));
            }

            SelfBindResult innerCheck = checkRestraintStack(stack, entity, entity, part);
            if (!innerCheck.canBind()) {
                return SelfBindResult.fail(innerCheck.message);
            }
        }

        return SelfBindResult.SUCCESS;
    }

    public static void executeSelfBind(LivingEntity entity, ItemStack stack, PlayerRestraintPart part) {
        if (stack.getItem() instanceof RestraintToolItem toolItem) {
            ItemStack bindStack = stack.copy();
            bindStack.setCount(1);

            RestraintToolsUtils.addRestraintTool(entity, bindStack);

            if(!(entity instanceof Player player) || !player.isCreative()){
                stack.shrink(1);
            }

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("event." + MODID + ".self_bondage.done").withStyle(ChatFormatting.GREEN), true);
            }

            clearData(entity);
            return;
        }

        if (!(stack.getItem() instanceof RestraintItem restraintItem)) return;

        ItemStack bindStack = stack.copy();
        bindStack.setCount(1);

        boolean success = addRestraintItem(entity, part, bindStack);

        if (success) {
            int index = getRestraintCount(entity, part) - 1;

            if (restraintItem.reduceStackWhenKidnapFinish(entity, entity, stack, part, index)) {
                stack.shrink(1);
            }

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("event." + MODID + ".self_bondage.done").withStyle(ChatFormatting.GREEN), true);
            }

            restraintItem.onKidnapToTarget(entity, entity, bindStack, part, index);

            clearData(entity);
        }
    }

    private static SelfBindResult checkRestraintStack(ItemStack stack, LivingEntity target, LivingEntity actionEntity, PlayerRestraintPart part) {

        if (stack.getItem() instanceof RestraintToolItem) {
            return SelfBindResult.SUCCESS;
        }

        RestraintItem item = (RestraintItem) stack.getItem();

        List<ItemStack> currentRestraints = getAllPartRestraint(target, part);
        int targetIndex = currentRestraints.size();

        if (part == PlayerRestraintPart.restraint_connection) {
            Component error = item.canConnectBind(actionEntity, target, stack);
            if (error != null) return SelfBindResult.fail(error);

            if (partHasBeenBlocked(target, part, targetIndex) && item.canBeBlockByConnect(target)) {
                return SelfBindResult.fail(Component.translatable("event." + MODID + ".kidnap.block_by_connect").withStyle(ChatFormatting.DARK_RED));
            }
            return SelfBindResult.SUCCESS;
        }

        if (part == PlayerRestraintPart.restraint_gag) {
            if (stack.getItem() instanceof RestraintItem gagItem && gagItem.canStuffedGag(target, stack)) {
                for (ItemStack gag : currentRestraints) {
                    if (gag.getItem() instanceof RestraintItem cur_gag && cur_gag.canBlockedGag(target, stack)) {
                        return SelfBindResult.fail(Component.translatable("event." + MODID + ".kidnap.gag_has_been_block").withStyle(ChatFormatting.DARK_RED));
                    }
                }
                return SelfBindResult.SUCCESS;
            } else {
                return SelfBindResult.SUCCESS;
            }
        }

        Component itemResult = item.canUseKidnap(actionEntity, target, stack, part, targetIndex);
        if (itemResult != null) return SelfBindResult.fail(itemResult);

        if (partHasBeenBlocked(target, part, targetIndex) && item.canBeBlockByConnect(target)) {
            return SelfBindResult.fail(Component.translatable("event." + MODID + ".kidnap.block_by_connect").withStyle(ChatFormatting.DARK_RED));
        }
        if (partHasBeenBlockByInnerRestraint(target, part, targetIndex) && item.canBeBlockByInner(target)) {
            return SelfBindResult.fail(Component.translatable("event." + MODID + ".kidnap.block_by_inner").withStyle(ChatFormatting.DARK_RED));
        }

        return SelfBindResult.SUCCESS;
    }
}