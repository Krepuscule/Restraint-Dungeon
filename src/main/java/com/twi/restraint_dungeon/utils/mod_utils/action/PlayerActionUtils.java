package com.twi.restraint_dungeon.utils.mod_utils.action;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.action.utils.ActionManager;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerActionCapability;
import net.minecraft.Util;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerActionUtils {

    /**
     * 获取指定实体的动作数据
     */
    public static PlayerActionCapability getData(LivingEntity entity) {
        return entity.getData(ModAttachments.PLAYER_ACTION);
    }

    /**
     * 更新实体的动作数据
     */
    public static void setData(LivingEntity entity, PlayerActionCapability data) {
        entity.setData(ModAttachments.PLAYER_ACTION, data);
    }


    /**
     * 设置当前动作 ID
     */
    public static void setCurrentAction(LivingEntity entity, String actionId) {
        PlayerActionCapability data = getData(entity);
        data.setCurrentAction(actionId);
        setData(entity, data);
    }

    public static String getCurrentActionId(LivingEntity entity) {
        return getData(entity).getCurrentAction();
    }

    /**
     * 设置伙伴 UUID
     */
    public static void setPartner(LivingEntity entity, @Nullable UUID partnerUUID) {
        PlayerActionCapability data = getData(entity);
        data.setPartnerUUID(partnerUUID);

        setData(entity, data);
    }

    @Nullable
    public static UUID getActionPartnerUUID(LivingEntity entity) {
        return getData(entity).getPartnerUUID();
    }

    /**
     * 设置是否为动作目标方
     */
    public static void setIsTarget(LivingEntity entity, boolean isTarget) {
        PlayerActionCapability data = getData(entity);
        data.setTarget(isTarget);
        setData(entity, data);
    }

    public static boolean isTarget(LivingEntity entity) {
        return getData(entity).isTarget();
    }

    /**
     * 重置所有动作状态
     */
    public static void reset(LivingEntity entity) {
        PlayerActionCapability data = getData(entity);
        data.reset();
        setData(entity, data);
    }

    /**
     * 判定实体当前是否处于任何动作中
     */
    public static boolean isDoingAction(LivingEntity entity) {
        return !getCurrentActionId(entity).equals("NONE");
    }

    /**
     * 获取指定实体当前正在执行的 BaseAction 实例
     * @param entity 目标实体
     * @return 对应的 BaseAction，如果没在执行动作或动作不存在则返回 null
     */
    @Nullable
    public static BaseAction getCurrentAction(LivingEntity entity) {
        String actionId = getCurrentActionId(entity);
        if (actionId == null || actionId.equals("NONE")) {
            return null;
        }
        return ActionManager.get(actionId);
    }

    /**
     * 针对单人的action动作执行绑定
     *
     * @param action  主动方（发起者）
     * @param actionId 动作ID
     *
     */
    public static void boundSingleAction(LivingEntity action,String actionId){
        PlayerActionCapability carrierData = getData(action);
        carrierData.setCurrentAction(actionId);
        carrierData.setPartnerUUID(Util.NIL_UUID);
        carrierData.setTarget(false);
        setData(action, carrierData);
    }

    /**
     * 快速建立两个实体间的动作关系
     *
     * @param action  主动方（发起者）
     * @param target   被动方（目标）
     * @param actionId 动作ID
     */
    public static void linkAction(LivingEntity action, LivingEntity target, String actionId) {
        // 设置发起方
        PlayerActionCapability carrierData = getData(action);
        carrierData.setCurrentAction(actionId);
        carrierData.setPartnerUUID(target.getUUID());
        carrierData.setTarget(false);
        setData(action, carrierData);

        // 设置接收方
        PlayerActionCapability targetData = getData(target);
        targetData.setCurrentAction(actionId);
        targetData.setPartnerUUID(action.getUUID());
        targetData.setTarget(true);
        setData(target, targetData);
    }

    /**
     * 清除单人的action信息
     *
     * @param action  主动方（发起者）
     * @param actionId 动作ID
     *
     */
    public static void resetSingleAction(LivingEntity action,String actionId){
        reset(action);
    }

    /**
     * 快速解除两个实体间的动作关系
     */
    public static void unlinkAction(LivingEntity action, @Nullable LivingEntity target) {
        reset(action);
        if (target != null) {
            reset(target);
        }
    }
}