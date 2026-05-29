package com.twi.restraint_dungeon.action.type;

import com.twi.restraint_dungeon.action.BaseAction;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;

public abstract class CarryingAction extends BaseAction {

    /** 是否在动作结束后释放目标 */
    public boolean shouldEndCarry(){
          return false;
    }

    /** 该动作是否需要处于“正在抱起实体”的状态才能在菜单中显示 */
    public boolean requiresCarrying() { return true; }

    @Override
    public Component canUse(Player actionPlayer, @Nullable HitResult result) {

        if(getCarriedPassenger(actionPlayer) == null || actionPlayer == null){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        LivingEntity target = getCarriedPassenger(actionPlayer);

        if(!isCarrier(actionPlayer) || !isBeingCarried(target)){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(target == null || !actionPlayer.isAlive() || !target.isAlive()){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(target.distanceToSqr(actionPlayer) >= getMaxDistance() * getMaxDistance()){
            return Component.translatable("action." + MODID + ".fail_common.too_far").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBeenBindArms(actionPlayer) || isBeenBindHands(actionPlayer) || isBeenBindLegs(actionPlayer)) {
            return Component.translatable("action." + MODID + ".fail_common.is_being_binding").withStyle(ChatFormatting.DARK_RED);
        }

        if(!isBeenFullyBind(target)) {
            return Component.translatable("action." + MODID + ".fail_carry.need_bind").withStyle(ChatFormatting.DARK_RED);
        }

        if(!target.isPassenger() || !(target.getVehicle() instanceof Player) || getPartnerUUID(target) != actionPlayer.getUUID()) {
            return Component.translatable("action." + MODID + ".fail_carrying.not_carrying").withStyle(ChatFormatting.DARK_RED);
        }

        if(getIsStruggling(target)){
            return Component.translatable("action." + MODID + ".fail_carrying.target_is_sturggling")
                    .withStyle(ChatFormatting.RED);
        }

        return null;
    }

    @Override
    public boolean canContinueUse(ServerPlayer actionPlayer, LivingEntity targetEntity) {
        return actionPlayer.isAlive() && targetEntity.isAlive()
                && actionPlayer.getUUID().equals(getPartnerUUID(targetEntity))
                && isBeenFullyBind(targetEntity);
    }


    @Override
    public boolean shouldShowInMenu(Player actionPlayer, @Nullable LivingEntity target,String CarryingState,Boolean isCarryTarget) {

        if(getCarriedPassenger(actionPlayer) == null || actionPlayer == null){
            return false;
        }

        if(!isCarrier(actionPlayer) || !isBeingCarried(target)){
            return false;
        }

        if(target == null || !actionPlayer.isAlive() || !target.isAlive()){
            return false;
        }

        if(target.distanceToSqr(actionPlayer) >= getMaxDistance() * getMaxDistance()){
            return false;
        }

        if(isBeenBindArms(actionPlayer) || isBeenBindHands(actionPlayer) || isBeenBindLegs(actionPlayer)) {
            return false;
        }

        if(!isBeenFullyBind(target)) {
            return false;
        }

        return true;
    }

    @Override
    public void onStart(ServerPlayer carrier, LivingEntity target) {

    }

    @Override
    public void onFinish(ServerPlayer carrier, LivingEntity target) {
        if (shouldEndCarry()) {
             PlayerCarryUtils.stopCarrying(carrier);

            Vec3 safePos = findSafePos(carrier);
            target.teleportTo(safePos.x, safePos.y, safePos.z);
        }
    }

    @Override
    public void onAbort(ServerPlayer carrier, LivingEntity target) {

    }

    /** 查找释放时的安全位置 */
    protected Vec3 findSafePos(ServerPlayer carrier) {
        Vec3 look = carrier.getLookAngle();
        return carrier.position().subtract(look.x, 0, look.z);
    }
}