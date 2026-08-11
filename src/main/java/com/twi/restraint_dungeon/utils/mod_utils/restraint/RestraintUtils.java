package com.twi.restraint_dungeon.utils.mod_utils.restraint;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.item.ModDataComponents;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintKeyItem;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintLockItem;
import com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isBeingCarried;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isCarrier;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.isKidnappingActive;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.isReleaseActive;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.self_bondage.SelfBondageUtils.isSelfBondaging;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.*;

public class RestraintUtils {

    /* ----------------------------------------------------- 栏位操作方法  ----------------------------------------------------- */

    /**
     * 获取玩家当前的目标部位
     * @param entity 目标实体
     */
    public static PlayerRestraintPart getEntityTargetPart(LivingEntity entity){
        return getTargetPart(entity);
    }


    /**
     * 检查指定部位是否包含目标物品
     * @param entity 目标实体
     * @param part 目标部位
     * @param stack 待检测的 ItemStack
     * @param isStrict 是否要求完全一致 (True: 数据必须完全相同; False: 仅判断物品种类)
     */
    public static boolean hasRestraint(LivingEntity entity, PlayerRestraintPart part, ItemStack stack, boolean isStrict) {
        if (entity == null || stack.isEmpty()) return false;

        List<ItemStack> list = getAllRestraintsByPart(entity, part);

        for (ItemStack cur_stack : list) {
            if (isStrict) {
                if (ItemStack.matches(cur_stack, stack)) {
                    return true;
                }
            } else {
                if (ItemStack.isSameItem(cur_stack, stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 快速检查某个部位是否穿戴了任何拘束具
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     */
    public static boolean hasAnyRestraint(LivingEntity entity, PlayerRestraintPart bodyPart) {
        return !getPartLastRestraint(entity, bodyPart).isEmpty();
    }


    /**
     * 获取某个部位当前穿戴的数量
     * @param entity 目标实体,
     * @param bodyPart 目标部位
     */
    public static int getRestraintCount(LivingEntity entity, PlayerRestraintPart bodyPart) {
        if (entity == null) return 0;
        return getAllRestraintsByPart(entity, bodyPart).size();
    }

    /**
     * 获取指定部位的最后一个拘束具
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     */
    public static ItemStack getPartLastRestraint(LivingEntity entity, PlayerRestraintPart bodyPart) {
        if (entity == null) return ItemStack.EMPTY;

        List<ItemStack> list = getAllRestraintsByPart(entity, bodyPart);

        if (!list.isEmpty()) {
            return list.getLast();
        }

        return ItemStack.EMPTY;
    }

    /**
     * 根据索引值获取指定部位的拘束具
     * @param entity 目标实体
     * @param bodyPart 目标部位
     * @param index 索引位置
     */
    public static ItemStack getPartRestraintByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, int index) {
        if (entity == null || index < 0) return ItemStack.EMPTY;
        return getRestraintByIndex(entity, bodyPart, index);
    }

    /**
     * 获取指定拘束具在特定部位中的索引位置
     * @param entity 目标实体
     * @param part 目标部位
     * @param stack 待查找的 ItemStack
     * @param isStrict 是否要求完全一致 (True: 数据/NBT必须完全相同; False: 仅判断物品种类)
     * @return 找到的第一个匹配项索引，未找到则返回 -1
     */
    public static int getRestraintIndex(LivingEntity entity, PlayerRestraintPart part, ItemStack stack, boolean isStrict) {
        if (entity == null || stack.isEmpty()) return -1;

        List<ItemStack> list = getAllRestraintsByPart(entity, part);

        for (int i = 0; i < list.size(); i++) {
            ItemStack current = list.get(i);

            if (isStrict) {
                if (ItemStack.matches(current, stack)) {
                    return i;
                }
            } else {
                if (ItemStack.isSameItem(current, stack)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 增加目标部位的拘束具物品(放置于该部位最后一个空位置，返回是否成功替换)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param stack 增加的拘束具的ItemStack
     */
    public static boolean addRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack){
        if(entity == null || stack.isEmpty()) return false;

        addRestraint(entity, bodyPart, stack);
        return true;
    }

    /**
     * 增加指定目标位置的拘束具物品(位置需要为空，返回是否成功替换)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param stack 增加的拘束具的ItemStack
     * @param index 索引值
     */
    public static boolean addRestraintItemByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack, int index){
        if(entity == null || stack.isEmpty()) return false;

        List<ItemStack> list = getAllRestraintsByPart(entity, bodyPart);

        if (index >= list.size()) {
            addRestraint(entity, bodyPart, stack, index);
            return true;
        }

        return false;
    }

    /**
     * 移除目标部位最后一个拘束具物品(返回移除的物品)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     */
    public static ItemStack removeRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart){
        if(entity == null) return ItemStack.EMPTY;

        return removeRestraint(entity, bodyPart);
    }

    /**
     * 移除目标部位指定索引的拘束具物品(返回移除的物品)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param index 索引值
     */
    public static ItemStack removeRestraintItemByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, int index){
        if(entity == null) return ItemStack.EMPTY;

        return removeRestraint(entity, bodyPart, index);
    }

    /**
     * 更换指定目标位置的拘束具物品(无需检测位置为空，返回是否成功替换)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param stack 替换后的拘束具的ItemStack
     * @param index 索引值
     */
    public static boolean replaceRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack, int index){
        if(entity == null) return false;

        List<ItemStack> list = getAllRestraintsByPart(entity, bodyPart);

        if (index >= 0 && index < list.size()) {
            replaceRestraintByIndex(entity, bodyPart, index, stack);
            return true;
        } else if (index == list.size()) {
            addRestraint(entity, bodyPart, stack);
            return true;
        }

        return false;
    }

    /**
     * 封装连接信息：包含物品堆栈、来源部位以及在该部位中的索引位置
     */
    public record ConnectingRestraint(ItemStack stack, PlayerRestraintPart sourcePart, int index) {}

    /**
     * 检查哪些拘束具会连接影响该部位，返回一个包含所有连接的相关拘束具的物品堆栈、来源部位以及在该部位中的索引位置封装对象的列表
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param index 该物品的栏位索引
     */
    public static List<ConnectingRestraint> getConnectingRestraintsInfo(LivingEntity entity, PlayerRestraintPart bodyPart, int index) {
        List<ConnectingRestraint> results = new ArrayList<>();
        if (entity == null) return results;

        for (PlayerRestraintPart sourcePart : PlayerRestraintPart.values()) {
            List<ItemStack> stacks = getAllRestraintsByPart(entity, sourcePart);

            for (int i = 0; i < stacks.size(); i++) {
                if (sourcePart == bodyPart && i == index) continue;

                ItemStack stack = stacks.get(i);
                if (stack.isEmpty() || !(stack.getItem() instanceof RestraintItem ri)) continue;

                // 获取该物品处于 sourcePart 时的连接配置
                Map<String, List<String>> connectMap = ri.getConnectPartMap();
                if (connectMap == null) continue;

                List<String> targets = connectMap.get(sourcePart.toString());
                if (targets != null && targets.contains(bodyPart.toString())) {
                    results.add(new ConnectingRestraint(stack, sourcePart, i));
                }
            }
        }
        return results;
    }

    /* ----------------------------------------------------- 拘束具生效情况检查  ----------------------------------------------------- */
    /**
     * 获取该部位是否被束缚
     * @param entity 目标实体,
     * @param bodyPart 需要检测是否束缚的部位
     */
    public static boolean partHasBeenBound(LivingEntity entity, PlayerRestraintPart bodyPart) {

        if(isRidingRestraintDevice(entity) && getRestraintDevice(entity) instanceof RestraintDevice device){
            RestraintDeviceUtils.DeviceContext context = getRestraintDeviceContext(entity);
            if(context != null){
                if(bodyPart == PlayerRestraintPart.restraint_blindfold && device.canBlindfold(context.state(),context.pos())){
                    return true;
                }
                if(bodyPart == PlayerRestraintPart.restraint_gag && device.canGag(context.state(),context.pos())){
                    return true;
                }
                if (bodyPart == PlayerRestraintPart.restraint_arms_bind && device.canBindArms(context.state(), context.pos())) {
                    return true;
                }
                if(bodyPart == PlayerRestraintPart.restraint_legs_bind && device.canBindLegs(context.state(), context.pos())){
                    return true;
                }
                if(bodyPart == PlayerRestraintPart.restraint_hands_bind && device.canBindHands(context.state(), context.pos())){
                    return true;
                }
            }
        }

//        if((isChangingRestraint(entity) == 1 && bodyPart == PlayerRestraintPart.restraint_arms_bind)
//                || (isChangingRestraint(entity) == 2 && bodyPart == PlayerRestraintPart.restraint_legs_bind)){
//            return true;
//        }

        List<ItemStack> restraints = getAllPartRestraint(entity, bodyPart);
        for(ItemStack restraint : restraints) {
            if(restraint.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                return true;
            }
        }
        PlayerRestraintPart[] BODY_PART = new PlayerRestraintPart[]{
                PlayerRestraintPart.restraint_blindfold,
                PlayerRestraintPart.restraint_gag,
                PlayerRestraintPart.restraint_collar,
                PlayerRestraintPart.restraint_body_bind,
                PlayerRestraintPart.restraint_arms_bind,
                PlayerRestraintPart.restraint_hands_bind,
                PlayerRestraintPart.restraint_legs_bind
        };
        for(PlayerRestraintPart part : BODY_PART) {
            if(part != bodyPart) {
                if(partHasBeenBoundByOtherPartRestraint(entity,bodyPart,part)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 辅助方法，检查其他部位的拘束具是否有同时限制该部位，以获取目标部位是否被束缚
     * @param entity 目标实体,
     * @param bodyPart 需要检测是否束缚的部位,
     * @param checkPart 当前检查的其他部位
     */
    private static boolean partHasBeenBoundByOtherPartRestraint(LivingEntity entity, PlayerRestraintPart bodyPart, PlayerRestraintPart checkPart) {

        List<ItemStack> restraints = getAllPartRestraint(entity,checkPart);
        if(restraints.isEmpty()) return false;
        boolean hasBeenBound = false;
        for(ItemStack restraint : restraints) {
            if(restraint.getItem() instanceof RestraintItem restraintItem && restraintItem.getBoundPartMap() != null && !restraintItem.getBoundPartMap().isEmpty()
                    && restraintItem.getBoundPartMap().get(checkPart) != null
                    && !restraintItem.getBoundPartMap().get(checkPart).isEmpty()
                    && restraintItem.getBoundPartMap().get(checkPart).contains(bodyPart)) {
                hasBeenBound = true;
                break;
            }
        }
        return hasBeenBound;
    }

    /**
     * 获取该部位是否被阻碍添加/移除束缚
     * @param entity 目标实体,
     * @param bodyPart 需要检测是否束缚的部位
     */
    public static boolean partHasBeenBlocked(LivingEntity entity,PlayerRestraintPart bodyPart,int index) {
        List<ItemStack> restraints = getAllPartRestraint(entity,bodyPart);
        for(int i = restraints.size()-1; i > index; i--) {
            if(index < 0){
                break;
            }
            if(restraints.get(i).getItem() instanceof RestraintItem restraintItem && restraintItem.canBlockConnectPart(entity)) {
                return true;
            }
        }
        PlayerRestraintPart[] BODY_PART = new PlayerRestraintPart[]{
                PlayerRestraintPart.restraint_blindfold,
                PlayerRestraintPart.restraint_gag,
                PlayerRestraintPart.restraint_collar,
                PlayerRestraintPart.restraint_body_bind,
                PlayerRestraintPart.restraint_arms_bind,
                PlayerRestraintPart.restraint_hands_bind,
                PlayerRestraintPart.restraint_legs_bind
        };
        for(PlayerRestraintPart part : BODY_PART) {
            if(part != bodyPart) {
                if(partHasBeenBlockByOtherPartRestraint(entity,bodyPart,part,index)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取该部位是否被内侧阻碍添加/移除束缚
     * @param entity 目标实体,
     * @param bodyPart 需要检测是否束缚的部位
     */
    public static boolean partHasBeenBlockByInnerRestraint(LivingEntity entity,PlayerRestraintPart bodyPart,int index) {
        List<ItemStack> restraints = getAllPartRestraint(entity,bodyPart);
        for(int i = 0; i < index; i++) {
            if(restraints.get(i).getItem() instanceof RestraintItem restraintItem && restraintItem.canBlockConnectPart(entity)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查其他部位的拘束具是否有同时阻碍该部位添加/释放束缚
     * @param entity 目标实体,
     * @param bodyPart 需要检测是否束缚的部位,
     * @param checkPart 当前检查的其他部位
     */
    private static boolean partHasBeenBlockByOtherPartRestraint(LivingEntity entity,PlayerRestraintPart bodyPart,PlayerRestraintPart checkPart,int index) {

        List<ItemStack> restraints = getAllPartRestraint(entity,checkPart);
        if(restraints.isEmpty()) return false;
        boolean hasBeenBlock = false;
        for(int i = restraints.size()-1; i >= index; i--) {
            if(index < 0){
                break;
            }
            if(restraints.get(i).getItem() instanceof RestraintItem restraintItem){
                if(restraintItem.getConnectPartMap() != null && !restraintItem.getConnectPartMap().isEmpty()
                        && restraintItem.getConnectPartMap().get(checkPart.toString()) != null && !restraintItem.getConnectPartMap().get(checkPart.toString()).isEmpty()
                        && restraintItem.getConnectPartMap().get(checkPart.toString()).contains(bodyPart.toString())
                        && restraintItem.canBlockConnectPart(entity)) {
                    hasBeenBlock = true;
                    break;
                }
            }
        }
        return hasBeenBlock;
    }

    /* ----------------------------------------------------- 部位拘束状态检查  ----------------------------------------------------- */


    /**
     * 获取目标是否正处于忙碌状态（被某个系统占用状态）
     * @param entity 目标实体
     */
    public static boolean isBusyState(LivingEntity entity) {

        return isSelfBondaging(entity)
                || isChangingRestraint(entity) != 0
                || isChangingPosition(entity)
                || getIsStruggling(entity)
                || isDoingAction(entity)
                || (entity instanceof Player targetPlayer_1 && isPlayerRestraintMoving(targetPlayer_1))
                || (entity instanceof Player targetPlayer_2 && isCarrier(targetPlayer_2))
                || isBeingCarried(entity)
                || isKidnappingActive(entity)
                || isReleaseActive(entity);
    }


    /**
     * 获取目标是否被蒙住眼睛（眼罩 栏位中存在至少一个物品有效）
     * @param entity 目标实体
     */
    public static boolean isBeenBlindfold(LivingEntity entity) {

        if(entity == null) return false;

        return partHasBeenBound(entity,PlayerRestraintPart.restraint_blindfold);
    }

    /**
     * 获取目标是否被堵嘴（堵嘴栏位有至少一个拘束具有效）
     * @param entity 目标实体
     */
    public static boolean isBeenGag(LivingEntity entity) {

        if(entity == null) return false;

        return RestraintUtils.partHasBeenBound(entity, PlayerRestraintPart.restraint_gag);
    }

    /**
     * 获取目标是否被塞住堵嘴
     * @param entity 目标实体
     */
    public static boolean isBeenStuffedGag(LivingEntity entity) {
        if(entity == null) return false;

        List<ItemStack> gags = getAllGag(entity);
        if(!gags.isEmpty()){
            for(ItemStack stack : gags) {
                if (stack.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                    if(restraintItem.canStuffedGag(entity, stack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取目标是否被封住堵嘴
     * @param entity 目标实体
     */
    public static boolean isBeenBlockedGag(LivingEntity entity) {
        if(entity == null) return false;

        List<ItemStack> gags = getAllGag(entity);
        if(!gags.isEmpty()){
            for(ItemStack stack : gags) {
                if (stack.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                    if(restraintItem.canBlockedGag(entity, stack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取目标是否被严格堵嘴（堵嘴栏位至少有一个塞嘴拘束具和一个封嘴拘束具）
     * @param entity 目标实体
     */
    public static boolean isBeenHeavyGag(LivingEntity entity) {

        if(entity == null) return false;
        boolean isStuffedGag = isBeenStuffedGag(entity);
        boolean isBlockedGag = isBeenBlockedGag(entity);

        return isStuffedGag && isBlockedGag;

    }

    /**
     * 获取目标是否被束缚手臂（手臂束缚栏位有至少一个拘束具有效）
     * @param entity 目标实体
     */
    public static boolean isBeenBindArms(LivingEntity entity) {

        if(entity == null) return false;

        return partHasBeenBound(entity,PlayerRestraintPart.restraint_arms_bind);
    }

    /**
     * 获取目标是否被束缚双手（双手束缚栏位有至少一个拘束具有效）
     * @param entity 目标实体
     */
    public static boolean isBeenBindHands(LivingEntity entity) {

        if(entity == null) return false;

        return partHasBeenBound(entity,PlayerRestraintPart.restraint_hands_bind);
    }

    /**
     * 获取目标是否被束缚双腿（双腿束缚栏位有至少一个拘束具有效）
     * @param entity 目标实体
     */
    public static boolean isBeenBindLegs(LivingEntity entity) {

        if(entity == null) return false;

        return partHasBeenBound(entity,PlayerRestraintPart.restraint_legs_bind);
    }

    /**
     * 获取目标是否被完全束缚（手臂和双腿束缚栏位各有至少一个拘束具有效）
     * @param entity 目标实体
     */
    public static boolean isBeenFullyBind(LivingEntity entity) {

        if(entity == null) return false;

        return partHasBeenBound(entity,PlayerRestraintPart.restraint_arms_bind)
                && partHasBeenBound(entity,PlayerRestraintPart.restraint_legs_bind);
    }

    /**
     * 获取目标是否被套上项圈（项圈束缚栏位有至少一个拘束具有效）
     * @param entity 目标实体
     */
    public static boolean isBeenCollar(LivingEntity entity) {

        if(entity == null) return false;

        return partHasBeenBound(entity,PlayerRestraintPart.restraint_collar);
    }

    /**
     * 获取目标是否被连接束缚（连接束缚栏位有至少一个拘束具有效）
     * @param entity 目标实体
     */
    public static boolean isBeenConnectBind(LivingEntity entity) {

        if(entity == null) return false;

        return partHasBeenBound(entity,PlayerRestraintPart.restraint_connection);
    }

    /* ----------------------------------------------------- 首个生效拘束具检查  ----------------------------------------------------- */

    /**
     * 获取目标最下层发挥作用的眼罩（最靠前的有效眼罩拘束具）
     * @param entity 目标实体
     */
    public static ItemStack getFirstBlindfold(LivingEntity entity) {

        if(entity == null) return ItemStack.EMPTY;

        List<ItemStack> stacks = getAllBlindfold(entity);
        if(!stacks.isEmpty()) {
            for(ItemStack stack : stacks) {
                if(stack.getItem() instanceof RestraintItem restraintItem) {
                    if (restraintItem.canBindCurrentPart(entity)) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取目标最下层发挥作用的堵嘴物（最靠前的有效堵嘴拘束具）
     * @param entity 目标实体
     */
    public static ItemStack getFirstGag(LivingEntity entity) {

        if(entity == null) return ItemStack.EMPTY;

        List<ItemStack> stacks = getAllGag(entity);
        if(!stacks.isEmpty()) {
            for(ItemStack stack : stacks) {
                if (stack.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }


    /**
     * 获取目标最下层可用的项圈（最靠前的有效项圈拘束具）
     * @param entity 目标实体
     */
    public static ItemStack getFirstCollar(LivingEntity entity) {

        if(entity == null) return ItemStack.EMPTY;

        List<ItemStack> stacks = getAllCollar(entity);

        if(!stacks.isEmpty()) {
            for(ItemStack stack : stacks) {
                if (stack.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取目标最下层可用的手臂束缚（最靠前的有效手臂拘束具）
     * @param entity 目标实体
     */
    public static ItemStack getFirstArmsBind(LivingEntity entity) {

        if(entity == null) return ItemStack.EMPTY;

        List<ItemStack> stacks = getAllArmsBind(entity);

        if(!stacks.isEmpty()) {
            for(ItemStack stack : stacks) {
                if (stack.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取目标最下层可用的双腿束缚（最靠前的有效双腿拘束具）
     * @param entity 目标实体
     */
    public static ItemStack getFirstLegsBind(LivingEntity entity) {

        if(entity == null) return ItemStack.EMPTY;

        List<ItemStack> stacks = getAllLegsBind(entity);

        if(!stacks.isEmpty()) {
            for(ItemStack stack : stacks) {
                if (stack.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取目标最下层可用的连接束缚（最靠前的有效连接拘束具）
     * @param entity 目标实体
     */
    public static ItemStack getFirstConnectBind(LivingEntity entity) {

        if(entity == null) return ItemStack.EMPTY;

        List<ItemStack> stacks = getAllConnectBind(entity);

        if(!stacks.isEmpty()) {
            for(ItemStack stack : stacks) {
                if (stack.getItem() instanceof RestraintItem restraintItem && restraintItem.canBindCurrentPart(entity)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /* ----------------------------------------------------- 部位拘束具获取  ----------------------------------------------------- */

    /**
     * 获取该目标指定部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     * @param bodyPart 指定的目标部位
     */
    public static List<ItemStack> getAllPartRestraint(LivingEntity entity, PlayerRestraintPart bodyPart) {
        return getAllRestraintsByPart(entity, bodyPart);
    }

    /**
     * 获取该目标“眼罩”部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllBlindfold(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_blindfold);
    }

    /**
     * 获取该目标“堵嘴物”部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllGag(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_gag);
    }

    /**
     * 获取该目标“项圈”部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllCollar(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_collar);
    }

    /**
     * 获取该目标“手臂”部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllArmsBind(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_arms_bind);
    }

    /**
     * 获取该目标“双手”部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllHandsBind(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_hands_bind);
    }

    /**
     * 获取该目标“身体”部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllBodyBind(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_body_bind);
    }

    /**
     * 获取该目标“双腿”部位的所有拘束具（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllLegsBind(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_legs_bind);
    }

    /**
     * 获取目标连接束缚的ItemStack
     * @param entity 目标实体
     */
    public static List<ItemStack> getAllConnectBind(LivingEntity entity) {
        return getAllPartRestraint(entity, PlayerRestraintPart.restraint_connection);
    }

    /**
     * 获取该目标所有部位的所有拘束具，合成为一个List（返回一个List<RestraintItem>）
     * @param entity 目标实体,
     */
    public static List<ItemStack> getAllRestraint(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        if (entity == null) return stacks;

        // 遍历所有部位并合并
        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            stacks.addAll(getAllPartRestraint(entity, part));
        }

        return stacks;
    }

    /* ----------------------------------------------------- 蒙眼功能相关  ----------------------------------------------------- */

    /**
     * 被有效眼罩蒙眼的玩家的蒙眼Overlay叠加渲染
     * @param player 目标玩家,
     * @param guiGraphics 渲染用GuiGraphics
     */
    public static void renderFullScreenOverlay(Player player, GuiGraphics guiGraphics) {
        List<ItemStack> restraints = getAllBlindfold(player);

        for (int i = restraints.size() - 1; i >= 0; i--) {
            ItemStack restraint = restraints.get(i);

            if (restraint.getItem() instanceof RestraintItem restraintItem) {
                restraintItem.renderBlindfoldOverlay(player.getUUID(), guiGraphics, restraint);
            }
        }
    }

    /* ----------------------------------------------------- 锁具功能相关  ----------------------------------------------------- */

    /**
     * 每Tick执行该目标身上所有的拘束具锁的Tick函数
     * @param entity 目标实体
     */
    public static void LockTick(LivingEntity entity) {
        // 眼罩部分锁Tick
        if(!getAllRestraint(entity).isEmpty()){
            List<ItemStack> stacks = getAllRestraint(entity);
            for (ItemStack stack : stacks) {
                if(stack.getItem() instanceof RestraintItem restraint_item){
                    if (stack != ItemStack.EMPTY
                            && restraint_item.getLockType(entity,stack).getItem() instanceof RestraintLockItem lock) {
                        lock.onLockTick();
                    }
                }
            }
        }
    }

    /**
     * 检查玩家指定部位的拘束具是否可以上锁
     * @param entity 目标实体,
     * @param bodyPart 动作者的目标部位
     * @param partRestraintItem 上锁的目标部位的最后一个拘束具
     */
    public static Component restraintCanBeLock(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack partRestraintItem) {
        if (entity == null) return Component.empty();

        if (partRestraintItem.isEmpty() || !(partRestraintItem.getItem() instanceof RestraintItem restraintItem)) {
            return Component.translatable("item.restraint_dungeon.message.lock.no_restraint").withStyle(ChatFormatting.DARK_RED);
        }

        // 检查是否已上锁
        if (!restraintItem.getLockType(entity,partRestraintItem).isEmpty()) {
            return Component.translatable("item.restraint_dungeon.message.lock.has_been_locked").withStyle(ChatFormatting.DARK_RED);
        }

        // 检查该物品本身是否允许上锁
        if (!restraintItem.isCanBeLocked()) {
            return Component.translatable("item.restraint_dungeon.message.lock.no_lockable_restraint").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }

    /**
     * 检查玩家指定的拘束装置是否可以上锁
     * @param entity 目标实体,
     * @param device 目标拘束装置，
     * @param pos 方块位置,
     * @param lockStack 锁具ItemStack
     */
    public static Component deviceCanBeLock(LivingEntity entity, RestraintDevice device, BlockPos pos,ItemStack lockStack,boolean isPlayer) {
        if (entity == null || device == null) return Component.empty();

        if(lockStack.getItem() instanceof RestraintLockItem lockItem && lockItem.getPairingID(lockStack) == null){
            return Component.translatable("item." + MODID +".message.lock.no_pair").withStyle(ChatFormatting.RED);
        }

        if(device.canBeLocked(entity,device,pos,lockStack,isPlayer) != null){
            return device.canBeLocked(entity,device,pos,lockStack,isPlayer);
        }

        return null;
    }

    /**
     * 检查玩家指定部位的拘束具是否可以解锁
     */
    public static Component restraintCanBeUnlock(LivingEntity target, ItemStack keyStack, PlayerRestraintPart bodyPart, ItemStack restraintStack) {
        if (target == null) return Component.empty();

        // 检查部位是否有拘束具
        if (restraintStack.isEmpty() || !(restraintStack.getItem() instanceof RestraintItem restraintItem)) {
            return Component.translatable("item.restraint_dungeon.message.key.no_unlockable_item").withStyle(ChatFormatting.DARK_RED);
        }

        // 检查拘束具是否上锁
        ItemStack lockInRestraint = restraintItem.getLockType(target,restraintStack);
        if (lockInRestraint.isEmpty() || !(lockInRestraint.getItem() instanceof RestraintLockItem)) {
            return Component.translatable("item.restraint_dungeon.message.key.not_locked").withStyle(ChatFormatting.DARK_RED);
        }

        // 检查钥匙匹配逻辑
        if (keyStack.getItem() instanceof RestraintKeyItem keyItem) {
            UUID keyID = keyStack.get(ModDataComponents.LOCK_PAIRING_ID);
            UUID lockID = lockInRestraint.get(ModDataComponents.LOCK_PAIRING_ID);

            if (keyID == null) {
                return Component.translatable("item.restraint_dungeon.message.key.no_pair").withStyle(ChatFormatting.DARK_RED);
            }

            if (!keyID.equals(lockID)) {
                return Component.translatable("item.restraint_dungeon.message.key.disable_pair").withStyle(ChatFormatting.DARK_RED);
            }
        }

        return null;
    }

    /**
     * 检查玩家指定的拘束装置是否可以解锁
     * @param entity 目标实体,
     * @param device 目标拘束装置，
     * @param pos 方块位置,
     * @param keyStack 钥匙ItemStack
     */
    public static Component deviceCanBeUnlock(LivingEntity entity, RestraintDevice device, BlockPos pos,ItemStack keyStack,boolean isPlayer) {
        if (entity == null || device == null) return Component.empty();

        if(device.canBeUnlocked(entity, device, pos, keyStack, isPlayer) != null){
            return device.canBeUnlocked(entity, device, pos, keyStack, isPlayer);
        }

        if (keyStack.getItem() instanceof RestraintKeyItem) {
            UUID keyID = keyStack.get(ModDataComponents.LOCK_PAIRING_ID);
            UUID lockID = device.getLockType(entity.level(),pos).get(ModDataComponents.LOCK_PAIRING_ID);

            if (keyID == null) {
                return Component.translatable("item.restraint_dungeon.message.key.no_pair").withStyle(ChatFormatting.DARK_RED);
            }

            if (!keyID.equals(lockID)) {
                return Component.translatable("item.restraint_dungeon.message.key.disable_pair").withStyle(ChatFormatting.DARK_RED);
            }
        }

        return null;
    }

    /* ----------------------------------------------------- 弃用功能方法（拘束值）  ----------------------------------------------------- */
    /**
     * 计算指定部位的拘束值（该部位所有有效拘束具的拘束值之和）
     * @param entity 目标实体
     * @param bodyPart 目标部位
     */
    public static int calRestraintValueByPart(LivingEntity entity, PlayerRestraintPart bodyPart) {
        if (entity == null) return 0;

        int restraintValue = 0;
        List<ItemStack> stacks = getAllPartRestraint(entity, bodyPart);

        if (!stacks.isEmpty()) {
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty() && stack.getItem() instanceof RestraintItem restraintItem
                        && restraintItem.canBindCurrentPart(entity)) {
                    restraintValue += restraintItem.getMaxResistance(stack);
                }
            }
        }
        return restraintValue;
    }

    /**
     * 计算上半身部位的总体捆绑值（身体、手臂、双手的有效拘束具拘束值加和）
     * @param entity 目标实体
     */
    public static int calRestraintValue_sum_UP_body(LivingEntity entity) {
        if (entity == null) return 0;

        return calRestraintValueByPart(entity, PlayerRestraintPart.restraint_body_bind)
                + calRestraintValueByPart(entity, PlayerRestraintPart.restraint_arms_bind)
                + calRestraintValueByPart(entity, PlayerRestraintPart.restraint_hands_bind);
    }

    /**
     * 计算全身的总体捆绑值（上身+下身）
     * @param entity 目标实体
     */
    public static int calRestraintValue_All_body(LivingEntity entity) {
        if (entity == null) return 0;

        return calRestraintValue_sum_UP_body(entity)
                + calRestraintValueByPart(entity, PlayerRestraintPart.restraint_legs_bind);
    }

    /**
     * 辅助方法：将具体的拘束数值映射为 0-5 级的等级
     * @param value 拘束总值
     */
    private static int getLevelFromValue(int value) {
        if (value <= 0) return 0;
        if (value <= 100) return 1;
        if (value <= 300) return 2;
        if (value <= 500) return 3;
        if (value <= 1000) return 4;
        return 5;
    }

    /**
     * 根据上半身部位的总体捆绑值计算上半身的捆绑等级
     * @param entity 目标实体
     */
    public static int calRestraintLevel_UP_body(LivingEntity entity) {
        if (entity == null) return 0;

        int restraintValue = calRestraintValue_sum_UP_body(entity);
        return getLevelFromValue(restraintValue);
    }

    /**
     * 根据下半身部位的总体捆绑值计算下半身的捆绑等级
     * @param entity 目标实体
     */
    public static int calRestraintLevel_Down_body(LivingEntity entity) {
        if (entity == null) return 0;

        // 下半身主要由双腿决定
        int restraintValue = calRestraintValueByPart(entity, PlayerRestraintPart.restraint_legs_bind);
        return getLevelFromValue(restraintValue);
    }

    /* ----------------------------------------------------- 弃用功能方法（单个部位连续拘束具）  ----------------------------------------------------- */
    /**
     * 检查是否有连续的N个拘束具
     * @param entity 目标实体,
     * @param itemStack 所需要检查的物品ItemStack
     * @param bodyPart 目标部位,
     * @param connectN 所需要检查的连接数
     */
    public static boolean isContinueRestraintPart(LivingEntity entity, ItemStack itemStack, PlayerRestraintPart bodyPart, int connectN) {
        if (entity == null || itemStack.isEmpty()) return false;

        List<ItemStack> list = getAllPartRestraint(entity, bodyPart);
        if (list.isEmpty() || list.size() < connectN) return false;

        int count = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            ItemStack currentStack = list.get(i);

            if (currentStack == itemStack) {
                count++;
            } else {
                break;
            }
        }

        return count == connectN;
    }
}