package com.twi.restraint_dungeon.utils.mod_utils.struggle;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.attributes.ModAttributes;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintLockItem;
import com.twi.restraint_dungeon.network.payload.player_struggle.StruggleOutOfIndexRestraintPayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.StruggleOutOfRestraintPayload;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.block_utils.BlockUtils.isNearPlacedSword;
import static com.twi.restraint_dungeon.utils.block_utils.BlockUtils.isNearTripwireHook;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.isCarrier;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.isKidnappingActive;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.isReleaseActive;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.self_bondage.SelfBondageUtils.isSelfBondaging;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.*;

public class StruggleUtils {

    private static final float DEFAULT_STRENGTH_DECAY_RATE = 0.01f;
    private static final float DEFAULT_STRENGTH_INCREMENT_RATE = 0.05f;
    private static final int DEFAULT_LOOSE_ARROW_LENGTH = 5;
    private static final float DEFAULT_LOOSE_INCREMENT_PROGRESS = 0.05f;
    private static final float DEFAULT_LOOSE_DECAY_PROGRESS = 0.05f;
    private static final float DEFAULT_UNLOCK_POINTER_SPEED = 0.01f;
    private static final float DEFAULT_UNLOCK_PROGRESS_INCREMENT = 0.05f;
    private static final float DEFAULT_UNLOCK_DECAY_PROGRESS = 0.05f;
    private static final float DEFAULT_UNLOCK_TARGET_ZONE = 0.1f;


    public enum StruggleDropType{
        DROP_TO_ENTITY,
        DROP_ON_GROUND
    }

    /* ----------------------------------------------------- 属性数据获取方法  ----------------------------------------------------- */

    /**
     * 获取玩家当前的挣扎模式
     */
    public static StruggleMode getPlayerStruggleMode(LivingEntity entity) {
        if (entity == null) return StruggleMode.NONE;
        return entity.getData(ModAttachments.ENTITY_STRUGGLE).getCurrentStruggleMode();
    }

    /**
     * 获取玩家是否正在进行挣扎（QTE进行中）
     */
    public static boolean getIsStruggling(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getData(ModAttachments.ENTITY_STRUGGLE).isStruggling();
    }

    /**
     * 获取玩家当前的挣扎进度 (0.0f - 1.0f)
     */
    public static float getStruggleProgress(LivingEntity entity) {
        if (entity == null) return 0.0f;
        return entity.getData(ModAttachments.ENTITY_STRUGGLE).getStruggleProgress();
    }


    /**
     * 更新玩家的挣扎模式
     */
    public static void updateStruggleMode(LivingEntity entity, StruggleMode mode) {
        if (entity == null) return;
        var data = entity.getData(ModAttachments.ENTITY_STRUGGLE);
        data.setCurrentStruggleMode(mode);
        entity.setData(ModAttachments.ENTITY_STRUGGLE, data);
    }

    /**
     * 设置挣扎激活状态
     */
    public static void updateIsStruggling(LivingEntity entity, boolean isStruggling) {
        if (entity == null) return;
        var data = entity.getData(ModAttachments.ENTITY_STRUGGLE);
        data.setStruggling(isStruggling);
        entity.setData(ModAttachments.ENTITY_STRUGGLE, data);
    }

    /**
     * 更新挣扎进度
     */
    public static void updateStruggleProgress(LivingEntity entity, float progress) {
        if (entity == null) return;
        var data = entity.getData(ModAttachments.ENTITY_STRUGGLE);
        data.setStruggleProgress(Mth.clamp(progress, 0.0f, 1.0f));
        entity.setData(ModAttachments.ENTITY_STRUGGLE, data);
    }

    /* ----------------------------------------------------- 挣扎物品数据/索引获取方法  ----------------------------------------------------- */

    /**
     * 获取玩家当前选中挣扎的目标部位最外层的拘束具
     */
    public static ItemStack getPlayerStrugglingItem(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;

        PlayerRestraintPart part = getTargetPart(entity);
        if (part == null) return ItemStack.EMPTY;

        List<ItemStack> restraints = getAllRestraintsByPart(entity, part);

        for (int i = restraints.size() - 1; i >= 0; i--) {
            ItemStack stack = restraints.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof RestraintItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取正在挣扎的物品在当前部位列表中的索引值
     */
    public static int getPlayerStrugglingItemIndex(LivingEntity entity) {
        if (entity == null) return -1;

        PlayerRestraintPart part = getTargetPart(entity);
        if (part == null) return -1;

        List<ItemStack> restraints = getAllRestraintsByPart(entity, part);
        for (int i = restraints.size() - 1; i >= 0; i--) {
            if (!restraints.get(i).isEmpty() && restraints.get(i).getItem() instanceof RestraintItem) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取特定索引位置的挣扎物品
     */
    public static ItemStack getPlayerStrugglingItem(LivingEntity entity, int index) {
        if (entity == null || index < 0) return ItemStack.EMPTY;

        PlayerRestraintPart part = getTargetPart(entity);
        if (part == null) return ItemStack.EMPTY;

        List<ItemStack> restraints = getAllRestraintsByPart(entity, part);
        if (index < restraints.size()) {
            ItemStack stack = restraints.get(index);
            return stack.getItem() instanceof RestraintItem ? stack : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    /* ----------------------------------------------------- 玩家挣脱的服务端逻辑  ----------------------------------------------------- */

    /**
     * 判断玩家当前是否可挣脱（服务端）
     * @param player 目标玩家
     */
    public static Component canBeStruggle(Player player) {

        if(player == null){
            return Component.translatable("hud." + MODID + ".struggle.invalid_target");
        }

        PlayerRestraintPart currentPart = getTargetPart(player);
        ItemStack strugglingItem = getPlayerStrugglingItem(player);
        int itemIndex = getPlayerStrugglingItemIndex(player);

        if (getAllRestraintsByPart(player, currentPart).isEmpty()) {
            return Component.translatable("hud." + MODID + ".struggle.no_restraint_on_part").withStyle(ChatFormatting.YELLOW);
        }

        if (!(strugglingItem.getItem() instanceof RestraintItem restraintItem)) {
            return Component.translatable("hud." + MODID + ".struggle.item_not_restraint").withStyle(ChatFormatting.YELLOW);
        }

        if(currentPart == PlayerRestraintPart.restraint_connection && restraintItem.canConnectStruggle(player,strugglingItem) != null){
            return restraintItem.canConnectStruggle(player,strugglingItem);
        }

        if(restraintItem.canBeStruggle(player,strugglingItem,currentPart,itemIndex) != null){
            return restraintItem.canBeStruggle(player,strugglingItem,currentPart,itemIndex);
        }

        if (partHasBeenBlocked(player, currentPart, itemIndex)) {
            return Component.translatable("hud." + MODID + ".struggle.restraint_has_been_block").withStyle(ChatFormatting.YELLOW);
        }

        if(isSelfBondaging(player)
                || isChangingRestraint(player) != 0
                || isChangingPosition(player)
                || isDoingAction(player)
                || isPlayerRestraintMoving(player)
                || isCarrier(player)
                || isKidnappingActive(player)
                || isReleaseActive(player)){
            return Component.translatable("hud." + MODID + ".struggle.cant_struggle").withStyle(ChatFormatting.YELLOW);
        }

        return null;
    }

    /**
     * 判断玩家当前是否可自行释放（服务端）
     * @param player 目标玩家
     */
    public static Component canBeReleaseBySelf(Player player) {

        if(player == null){
            return Component.translatable("hud." + MODID + ".struggle.invalid_target");
        }

        PlayerRestraintPart currentPart = getTargetPart(player);
        ItemStack strugglingItem = getPlayerStrugglingItem(player);
        int itemIndex = getPlayerStrugglingItemIndex(player);

        if (getAllRestraintsByPart(player, currentPart).isEmpty()) {
            return Component.translatable("hud." + MODID + ".struggle.no_restraint_on_part").withStyle(ChatFormatting.YELLOW);
        }

        if (!(strugglingItem.getItem() instanceof RestraintItem restraintItem)) {
            return Component.translatable("hud." + MODID + ".struggle.item_not_restraint").withStyle(ChatFormatting.YELLOW);
        }

        if (!restraintItem.getLockType(player,strugglingItem).isEmpty()) {
            return Component.translatable("hud." + MODID + ".struggle.restraint_has_been_lock").withStyle(ChatFormatting.YELLOW);
        }

        if(currentPart == PlayerRestraintPart.restraint_connection && restraintItem.canConnectReleaseBySelf(player,strugglingItem) != null){
            return restraintItem.canConnectReleaseBySelf(player,strugglingItem);
        }

        if(restraintItem.canBeReleaseBySelf(player,strugglingItem,currentPart,itemIndex) != null){
            return restraintItem.canBeReleaseBySelf(player,strugglingItem,currentPart,itemIndex);
        }

        if (partHasBeenBlocked(player, currentPart, itemIndex)) {
            return Component.translatable("hud." + MODID + ".struggle.restraint_has_been_block").withStyle(ChatFormatting.YELLOW);
        }

        if(isSelfBondaging(player)
                || isChangingRestraint(player) != 0
                || isChangingPosition(player)
                || isDoingAction(player)
                || isPlayerRestraintMoving(player)
                || isCarrier(player)
                || isKidnappingActive(player)
                || isReleaseActive(player)){
            return Component.translatable("hud." + MODID + ".struggle.cant_struggle").withStyle(ChatFormatting.YELLOW);
        }

        return null;
    }


    /**
     * 服务端执行玩家挣脱该部位顶部束缚
     * @param player 目标玩家
     */
    public static void playerOutOfRestraint(Player player) {
        if (player == null) return;
        PlayerRestraintPart part = getTargetPart(player);
        PacketDistributor.sendToServer(new StruggleOutOfRestraintPayload(String.valueOf(part)));
    }

    /**
     * 服务端执行玩家挣脱该部位指定索引束缚
     * @param player 目标玩家
     */
    public static void playerOutOfRestraint(Player player, int index) {
        if (player == null) return;
        PlayerRestraintPart part = getTargetPart(player);
        PacketDistributor.sendToServer(new StruggleOutOfIndexRestraintPayload(String.valueOf(part), index));
    }

    /**
     * 服务端执行挣脱逻辑
     * @param entity 目标实体,
     * @param bodyPart 指定的部位
     */
    public static void handleStruggleResult(LivingEntity entity, String bodyPart) {
        PlayerRestraintPart part = PlayerRestraintPart.valueOf(bodyPart);
        List<ItemStack> list = getAllRestraintsByPart(entity, part);

        for (int i = list.size() - 1; i >= 0; i--) {
            ItemStack stack = list.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof RestraintItem restraintItem) {
                PlayerStruggleOutofRestraintOnServer(entity, part, i, stack, restraintItem);
                break;
            }
        }
    }

    /**
     * 服务端执行玩家挣脱逻辑
     * @param entity 目标实体,
     * @param bodyPart 指定的部位,
     * @param index 指定的索引值
     */
    public static void handleStruggleResult(LivingEntity entity, String bodyPart, int index) {
        PlayerRestraintPart part = PlayerRestraintPart.valueOf(bodyPart);
        ItemStack stack = getRestraintByIndex(entity, part, index);

        if (!stack.isEmpty() && stack.getItem() instanceof RestraintItem restraintItem) {
            PlayerStruggleOutofRestraintOnServer(entity, part, index, stack, restraintItem);
        }
    }



    private static void PlayerStruggleOutofRestraintOnServer(LivingEntity entity, PlayerRestraintPart part, int index, ItemStack stack, RestraintItem restraintItem) {
        if (restraintItem.getLockType(entity,stack) != ItemStack.EMPTY
                && restraintItem.cleanLockWhenStruggleOufOfBind(entity, stack, part, index)) {
            restraintItem.setLockType(entity,stack, ItemStack.EMPTY);
        }


        if (restraintItem.dropRestraintWhenStruggleOff(entity, stack, part, index)) {

            if(entity instanceof Player player){
                if (restraintItem.dropStruggleItemDirection(entity, stack, part, index) == StruggleDropType.DROP_TO_ENTITY) {
                    int selectedSlot = player.getInventory().selected;
                    ItemStack currentInHand = player.getInventory().getItem(selectedSlot);
                    if (currentInHand.isEmpty()) {
                        player.getInventory().setItem(selectedSlot, stack.copy());
                    } else if(!player.getInventory().add(stack.copy())){
                        player.drop(stack.copy(), false);
                    }
                } else {
                    ItemEntity itemEntity = new ItemEntity(
                            player.level(), player.getX(), player.getY(), player.getZ(),
                            stack.copy()
                    );
                    itemEntity.setDeltaMovement(
                            (player.getRandom().nextDouble() - 0.5) * 1.5,
                            player.getRandom().nextDouble() * 1.5 + 0.1,
                            (player.getRandom().nextDouble() - 0.5) * 1.5
                    );
                    player.level().addFreshEntity(itemEntity);
                }
            }
        }

        removeRestraint(entity, part, index);

        if(entity instanceof Player player){
            player.displayClientMessage(Component.translatable("hud.restraint_dungeon.struggle.success").withStyle(ChatFormatting.GREEN), true);
        }

        restraintItem.onStruggleOff(entity, stack, part, index);
    }

    /* ----------------------------------------------------- 挣扎HUD 参数计算 ----------------------------------------------------- */

    /**
     * 蛮力挣扎 属性影响 衰减系数计算（每帧衰减速率decayRate（float)
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float strengthStruggle_DecayRate(Player player, ItemStack itemStack) {
        if (player == null) return 0;
        double itemStrengthIndex;
        double playerStruggleStrengthIndex;

        // 获取连接影响信息
        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            itemStrengthIndex = restraintItem.getStrengthIndex(itemStack);
            playerStruggleStrengthIndex = player.getAttributeValue(ModAttributes.STRUGGLE_STRENGTH)
                    * player.getAttributeValue(ModAttributes.RESTRAINT_STRENGTH);

            itemStrengthIndex = restraintItem.onStrengthStruggle(player.getUUID(), itemStrengthIndex);

            if (restraintItem.getLockType(player,itemStack) != ItemStack.EMPTY
                    && restraintItem.getLockType(player,itemStack).getItem() instanceof RestraintLockItem lock) {
                itemStrengthIndex = itemStrengthIndex * (lock.onStrengthStruggle(player, lock, itemStrengthIndex));
            }

            // 处理连接拘束具的影响
            if (!connectingRestraintsInfo.isEmpty()) {
                double reduceIndex = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        reduceIndex += (restraint.getStrengthIndex(connectingStack) / 10.0);
                    }
                }
                itemStrengthIndex = Math.max(itemStrengthIndex / 10,itemStrengthIndex - reduceIndex);
            }

            if (!isBeenBindArms(player) && !isBeenBindHands(player)) {
                playerStruggleStrengthIndex = playerStruggleStrengthIndex * 10;
            }

            return Mth.clamp((float) (DEFAULT_STRENGTH_DECAY_RATE / (itemStrengthIndex * playerStruggleStrengthIndex)),
                    DEFAULT_STRENGTH_DECAY_RATE / 10,
                    DEFAULT_STRENGTH_DECAY_RATE * 10);
        }
        return DEFAULT_STRENGTH_DECAY_RATE;
    }

    /**
     * 蛮力挣扎 耐久度影响（影响每次按键增加的进度,float）
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float strengthStruggle_Increment(Player player, ItemStack itemStack) {
        if (player == null) return 0;

        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            double itemResistance = restraintItem.getMaxResistance(itemStack);
            if (!connectingRestraintsInfo.isEmpty()) {
                double increaseResistance = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        increaseResistance =  restraint.getMaxResistance(connectingStack) / 10.0;
                    }
                }
                itemResistance = Mth.clamp(itemResistance + increaseResistance,itemResistance,itemResistance * 10);
            }
            return (float) (DEFAULT_STRENGTH_INCREMENT_RATE / (itemResistance / 100.0));
        }
        return DEFAULT_STRENGTH_INCREMENT_RATE;
    }

    /**
     * 松动束缚 耐久度影响（箭头数量 最少5个，最多12个）
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static int looseStruggle_ArrowLength(Player player, ItemStack itemStack) {
        if (player == null) return 0;

        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            double itemResistance =  restraintItem.getMaxResistance(itemStack);
            if (!connectingRestraintsInfo.isEmpty()) {
                double increaseResistance = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        increaseResistance =  restraint.getMaxResistance(connectingStack) / 10.0;
                    }
                }
                itemResistance = Mth.clamp(itemResistance + increaseResistance,itemResistance,itemResistance * 10);
            }
            return Mth.clamp((int) (DEFAULT_LOOSE_ARROW_LENGTH * (itemResistance / 100.0f)),5,12);
        }
        return DEFAULT_LOOSE_ARROW_LENGTH;
    }

    /**
     * 松动束缚 属性值影响（序列成功进度 minProgress 默认为0.01f,不超过0.1f;maxProgress 默认为minProgress * 1.5）
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float looseStruggle_minProgress(Player player, ItemStack itemStack) {
        if (player == null) return 0;
        double itemLooseIndex;
        double playerStruggleSpeedIndex;

        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            itemLooseIndex = restraintItem.getLooseIndex(itemStack);
            playerStruggleSpeedIndex = player.getAttributeValue(ModAttributes.STRUGGLE_SPEED)
                    * player.getAttributeValue(ModAttributes.RESTRAINT_STRENGTH);

            itemLooseIndex = restraintItem.onLooseStruggle(player.getUUID(), itemLooseIndex);

            if (restraintItem.getLockType(player,itemStack) != ItemStack.EMPTY
                    && restraintItem.getLockType(player,itemStack).getItem() instanceof RestraintLockItem lock) {
                itemLooseIndex = (itemLooseIndex * lock.onLooseStruggle(player, lock, itemLooseIndex));
            }

            if (!connectingRestraintsInfo.isEmpty()) {
                double reduceIndex = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        reduceIndex += (restraint.getLooseIndex(connectingStack) / 10.0);
                    }
                }
                itemLooseIndex = Math.max(itemLooseIndex / 10,itemLooseIndex - reduceIndex);
            }

            if (!isBeenBindArms(player) && !isBeenBindHands(player)) {
                playerStruggleSpeedIndex = playerStruggleSpeedIndex * 10;
            }
            return Mth.clamp(
                    (float) (DEFAULT_LOOSE_INCREMENT_PROGRESS * (playerStruggleSpeedIndex * itemLooseIndex)),
                    DEFAULT_LOOSE_INCREMENT_PROGRESS / 10,
                    DEFAULT_LOOSE_INCREMENT_PROGRESS * 10
            ) ;
        }
        return DEFAULT_LOOSE_INCREMENT_PROGRESS;
    }

    /**
     * 松动束缚 属性值影响（按键失误衰退 minRegressProgress 默认为0.01f,不超过0.1f;maxRegressProgress 默认为minRegressProgress * 2）
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float looseStruggle_minRegressProgress(Player player, ItemStack itemStack) {
        if (player == null) return 0;
        double itemLooseIndex;
        double playerStruggleSpeedIndex;

        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            itemLooseIndex = restraintItem.getLooseIndex(itemStack);
            playerStruggleSpeedIndex = player.getAttributeValue(ModAttributes.STRUGGLE_SPEED)
                    * player.getAttributeValue(ModAttributes.RESTRAINT_STRENGTH);

            itemLooseIndex = restraintItem.onLooseStruggle(player.getUUID(), itemLooseIndex);

            if (restraintItem.getLockType(player,itemStack) != ItemStack.EMPTY
                    && restraintItem.getLockType(player,itemStack).getItem() instanceof RestraintLockItem lock) {
                itemLooseIndex = (itemLooseIndex * lock.onLooseStruggle(player, lock, itemLooseIndex));
            }

            if (!connectingRestraintsInfo.isEmpty()) {
                double reduceIndex = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        reduceIndex += (restraint.getLooseIndex(connectingStack) / 10.0);
                    }
                }
                itemLooseIndex = Math.max(itemLooseIndex / 10,itemLooseIndex - reduceIndex);
            }

            if (!isBeenBindArms(player) && !isBeenBindHands(player)) {
                playerStruggleSpeedIndex = playerStruggleSpeedIndex * 10;
            }
            return Mth.clamp(
                    (float) (DEFAULT_LOOSE_DECAY_PROGRESS / (playerStruggleSpeedIndex * itemLooseIndex)),
                    DEFAULT_LOOSE_DECAY_PROGRESS / 10,
                    DEFAULT_LOOSE_DECAY_PROGRESS * 10
            ) ;
        }
        return DEFAULT_LOOSE_DECAY_PROGRESS;
    }

    /**
     * 解开锁扣 仅物品属性值影响（指针移动速度，默认pointerSpeed = 0.01f,最大为0.05f,最小为0.005f）
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float unlockStruggle_PointerSpeed(Player player, ItemStack itemStack) {
        if (player == null) return 0;
        double itemLockIndex;

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            itemLockIndex = restraintItem.getLockIndex(itemStack);

            itemLockIndex = restraintItem.onUnlockStruggle(player.getUUID(),itemLockIndex);

            return Mth.clamp(
                    (float) (DEFAULT_UNLOCK_POINTER_SPEED / itemLockIndex),
                    DEFAULT_UNLOCK_POINTER_SPEED / 2,
                    DEFAULT_UNLOCK_POINTER_SPEED * 5
            ) ;
        }
        return DEFAULT_UNLOCK_POINTER_SPEED;
    }

    /**
     * 解开锁扣 耐久值影响（进度增加，默认minProgressIncrement = 0.05f,maxProgressIncrement为minProgressIncrement * 2）
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float unlockStruggle_ProgressIncrement(Player player, ItemStack itemStack) {
        if (player == null) return 0;
        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            double itemResistance = restraintItem.getMaxResistance(itemStack);
            if (!connectingRestraintsInfo.isEmpty()) {
                double increaseResistance = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        increaseResistance =  restraint.getMaxResistance(connectingStack) / 10.0;
                    }
                }
                itemResistance = Mth.clamp(
                        itemResistance + increaseResistance,
                        itemResistance,
                        itemResistance * 10);
            }
            return (float) Mth.clamp( DEFAULT_UNLOCK_PROGRESS_INCREMENT / (itemResistance / 100.0f),
                    DEFAULT_UNLOCK_PROGRESS_INCREMENT / 10,
                    DEFAULT_UNLOCK_PROGRESS_INCREMENT * 10);
        }
        return DEFAULT_UNLOCK_PROGRESS_INCREMENT;
    }

    /**
     * 解开锁扣 耐久值影响（错误时衰退，默认minRegressRate = 0.05f,maxRegressRate为minRegressRate * 2）
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float unlockStruggle_Regression(Player player, ItemStack itemStack) {
        if (player == null) return 0;
        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            double itemResistance = restraintItem.getMaxResistance(itemStack);
            if (!connectingRestraintsInfo.isEmpty()) {
                double increaseResistance = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        increaseResistance =  restraint.getMaxResistance(connectingStack) / 10.0;
                    }
                }
                itemResistance = Mth.clamp(
                        itemResistance + increaseResistance,
                        itemResistance,
                        itemResistance * 10);
            }
            return (float) Mth.clamp( DEFAULT_UNLOCK_DECAY_PROGRESS / (itemResistance / 100.0f),
                    DEFAULT_UNLOCK_DECAY_PROGRESS / 10,
                    DEFAULT_UNLOCK_DECAY_PROGRESS * 10);
        }
        return DEFAULT_UNLOCK_DECAY_PROGRESS;
    }

    /**
     * 解开锁扣 属性值影响 (目标区域比例，默认为0.1f,最大为0.2f,最小为0.05f)
     * @param player 当前正在挣扎的玩家
     * @param itemStack 正在挣扎的拘束具ItemStack
     **/
    public static float unlockStruggle_TargetZoneRatio(Player player, ItemStack itemStack) {
        if (player == null) return 0;
        double itemLockIndex;
        double playerStruggleRangeIndex;

        List<ConnectingRestraint> connectingRestraintsInfo = getConnectingRestraintsInfo(player,
                getTargetPart(player),
                getPlayerStrugglingItemIndex(player));

        if (itemStack.getItem() instanceof RestraintItem restraintItem) {
            itemLockIndex = restraintItem.getLockIndex(itemStack);
            playerStruggleRangeIndex = player.getAttributeValue(ModAttributes.STRUGGLE_RANGE)
                    * player.getAttributeValue(ModAttributes.RESTRAINT_STRENGTH);

            itemLockIndex = restraintItem.onUnlockStruggle(player.getUUID(), itemLockIndex);

            if (restraintItem.getLockType(player,itemStack) != ItemStack.EMPTY
                    && restraintItem.getLockType(player,itemStack).getItem() instanceof RestraintLockItem lock) {
                itemLockIndex = (itemLockIndex * lock.onUnlockStruggle(player, lock, itemLockIndex));
            }

            if (!connectingRestraintsInfo.isEmpty()) {
                double reduceIndex = 0.0;
                for (ConnectingRestraint info : connectingRestraintsInfo) {
                    ItemStack connectingStack = info.stack();
                    if (connectingStack.getItem() instanceof RestraintItem restraint) {
                        reduceIndex += (restraint.getLockIndex(connectingStack) / 10.0);
                    }
                }
                itemLockIndex = Math.max(itemLockIndex / 10,itemLockIndex - reduceIndex);
            }

            if (!isBeenBindArms(player) && !isBeenBindHands(player)) {
                playerStruggleRangeIndex = playerStruggleRangeIndex * 10;
            }
            return Mth.clamp(
                    (float) (DEFAULT_UNLOCK_TARGET_ZONE / (playerStruggleRangeIndex * itemLockIndex)),
                    DEFAULT_UNLOCK_TARGET_ZONE / 2,
                    DEFAULT_UNLOCK_TARGET_ZONE * 2
            ) ;
        }
        return DEFAULT_UNLOCK_TARGET_ZONE;
    }

    /* ----------------------------------------------------- 挣扎环境方法 ----------------------------------------------------- */


    /**
     * 判定玩家是否正靠近锋利物品/方块/结构（用于挣扎逻辑和GUI渲染）
     **/
    public static boolean isNearCutStrugglingState(LivingEntity entity){

        if(!isBeenBindHands(entity)){
            if(entity.getMainHandItem().getItem() instanceof SwordItem
                    || entity.getMainHandItem().getItem() instanceof ShearsItem){
                return true;
            }
        }

        if(isNearPlacedSword(entity)){
            return true;
        }

        return false;
    }

    /**
     * 判定玩家是否正靠近钩子物品/方块/结构（用于挣扎逻辑和GUI渲染）
     **/
    public static boolean isNearHookStrugglingState(LivingEntity entity){


        if(isNearTripwireHook(entity)){
            return true;
        }

        return false;
    }

}
