package com.twi.restraint_dungeon.action;

import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenFullyBind;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBusyState;

public abstract class BaseAction {

    /** 唯一的 Action ID */
    public abstract String getActionId();

    /** 动画/持续时间（Tick） */
    public abstract int getAnimTicks();

    /** 最大交互距离 */
    public double getMaxDistance() { return 2.0; }

    /** 获取本地化显示名称 */
    public Component getDisplayName() {
        return Component.translatable("action." + MODID + "." + getActionId().toLowerCase());
    }

    /** 该动作是否渲染动作玩家主手的物品 */
    public boolean renderMainHandItem(Player actionPlayer) {
        return false;
    }

    /**
     * 进一步的前置条件检查（返回 null 表示通过，返回 Component 表示不可执行的原因）
     */
    @Nullable
    public Component canUse(Player actionPlayer, @Nullable HitResult result) {

        if(!(result instanceof EntityHitResult) || !(((EntityHitResult) result).getEntity() instanceof LivingEntity living)) {
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(!actionPlayer.isAlive() || !living.isAlive()){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBusyState(living)) {
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(living.distanceToSqr(actionPlayer) >= getMaxDistance() * getMaxDistance()){
            return Component.translatable("action." + MODID + ".fail_common.too_far").withStyle(ChatFormatting.DARK_RED);
        }

        if(living.isPassenger() && !(living.getVehicle() instanceof SeatEntity)) {
            return Component.translatable("action." + MODID + ".fail_common.target_riding").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBusyState(actionPlayer)) {
            return Component.translatable("action." + MODID + ".fail_common.cant_action_state").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBeenFullyBind(actionPlayer)) {
            return Component.translatable("action." + MODID + ".fail_common.is_being_binding").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }

    /** 检查 Action 是否能继续进行（用于每 Tick 轮询） */
    public boolean canContinueUse(ServerPlayer actionPlayer, LivingEntity targetEntity) {
        return actionPlayer.isAlive() && targetEntity.isAlive()
                && actionPlayer.distanceToSqr(targetEntity) < getMaxDistance() * getMaxDistance();
    }

    /**
     * 判定该 Action 是否应该出现在圆环菜单中
     * @param actionPlayer 当前玩家
     * @param target 准星瞄准的目标（可能为 null）
     * @param CarryingState 被携带的状态ID
     * @param isCarryTarget 是否是正在被携带的目标(默认为false，即使ID为“NONE”)
     */
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target,String CarryingState,Boolean isCarryTarget) {
        if(!actionPlayer.isAlive() || (target != null && !target.isAlive()) || target == null){
            return false;
        }

        if(target.distanceToSqr(actionPlayer) >= getMaxDistance() * getMaxDistance()){
            return false;
        }

        if(target.isPassenger() && !(target.getVehicle() instanceof SeatEntity)) {
            return false;
        }


        if(isBeenFullyBind(actionPlayer)) {
            return false;
        }

        return true;
    }

    /** 
     * Action 开始时执行 (仅服务端)
     */
    public void onStart(ServerPlayer carrier, LivingEntity target) {

    }

    /** 
     * 每 Tick 执行 (仅服务端)
     * @param ticksRemaining 距离结束还剩多少 Tick
     */
    public void onTick(ServerPlayer carrier, LivingEntity target, int ticksRemaining) {

    }

    /** 
     * Action 正常播放结束时执行 (仅服务端)
     */
    public void onFinish(ServerPlayer carrier, LivingEntity target) {

    }

    /** 
     * 当 canContinueUse 不满足或外部强行中断时调用
     * 此时应清理所有 Action 状态，确保玩家不会卡在固定状态
     */
    public void onAbort(ServerPlayer carrier, LivingEntity target) {

    }
}