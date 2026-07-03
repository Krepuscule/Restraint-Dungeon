package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.*;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearCutStrugglingState;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class RopeItem extends RestraintItem {

    public static final RestraintDefaults ROPE_DEFAULTS = new RestraintDefaults(
            100,
            30.0,
            0.5,
            1.0,
            0.5
    );

    // 允许装备的部位列表
    private final List<PlayerRestraintPart> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_gag,
            PlayerRestraintPart.restraint_body_bind,
            PlayerRestraintPart.restraint_arms_bind,
            PlayerRestraintPart.restraint_legs_bind,
            PlayerRestraintPart.restraint_connection
    );

    public RopeItem(Properties properties) {
        super(properties.stacksTo(1), ROPE_DEFAULTS);

        this.setCanEquipPartList(canEquipPartList);

        Map<String, List<String>> connectMap = new HashMap<>();
        connectMap.put(PlayerRestraintPart.restraint_body_bind.toString(),
                List.of(PlayerRestraintPart.restraint_arms_bind.toString(), PlayerRestraintPart.restraint_legs_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_arms_bind.toString(),
                List.of(PlayerRestraintPart.restraint_body_bind.toString(), PlayerRestraintPart.restraint_connection.toString()));
        connectMap.put(PlayerRestraintPart.restraint_legs_bind.toString(),
                List.of(PlayerRestraintPart.restraint_body_bind.toString(), PlayerRestraintPart.restraint_connection.toString()));
        connectMap.put(PlayerRestraintPart.restraint_connection.toString(),
                List.of(PlayerRestraintPart.restraint_arms_bind.toString(), PlayerRestraintPart.restraint_legs_bind.toString()));

        this.setConnectPartMap(connectMap);

        this.setCanBeLocked(false);
    }


    @Override
    public Component canBeStruggle(LivingEntity entity, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        // 如果处于连接束缚状态，无法挣脱手臂和腿部的绳子
        if(RestraintUtils.isBeenConnectBind(entity)){
            if(bodyPart == PlayerRestraintPart.restraint_arms_bind || bodyPart == PlayerRestraintPart.restraint_legs_bind){
                return Component.translatable("item.restraint_dungeon.rope.cant_be_released_when_connect").withStyle(ChatFormatting.DARK_RED);
            }
        }
        return null;
    }

    @Override
    public Component canBeReleaseBySelf(LivingEntity entity, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        if(entity instanceof Player player){
            if(RestraintUtils.isBeenConnectBind(player)){
                if(bodyPart == PlayerRestraintPart.restraint_arms_bind || bodyPart == PlayerRestraintPart.restraint_legs_bind){
                    return Component.translatable("item.restraint_dungeon.rope.cant_be_released_when_connect").withStyle(ChatFormatting.DARK_RED);
                }
            }
        }
//        else if(entity instanceof BaseNPCEntity) {
//            return null; // NPC默认允许或由AI控制
//        }
        return null;
    }

    @Override
    public Component canBeReleased(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        // 他人解助时的逻辑
        if(RestraintUtils.isBeenConnectBind(target)){
            if(bodyPart == PlayerRestraintPart.restraint_arms_bind || bodyPart == PlayerRestraintPart.restraint_legs_bind){
                return Component.translatable("item.restraint_dungeon.rope.cant_be_released_when_connect").withStyle(ChatFormatting.DARK_RED);
            }
        }
        return null;
    }

    @Override
    public Component canConnectBind(LivingEntity actionEntity, LivingEntity target, ItemStack stack) {
        boolean hasRopeOnArms = false;
        boolean hasRopeOnLegs = false;

        // 检查手臂是否有绳子
        for(ItemStack item : RestraintUtils.getAllArmsBind(target)){
            if(item.getItem() instanceof RopeItem) {
                hasRopeOnArms = true;
                break;
            }
        }
        // 检查腿部是否有绳子
        for(ItemStack item : RestraintUtils.getAllLegsBind(target)){
            if(item.getItem() instanceof RopeItem) {
                hasRopeOnLegs = true;
                break;
            }
        }


        if(getRestraintPosition(target) != RestraintPosition.LYING_DOWN){
            return Component.translatable("item.restraint_dungeon.rope.connect_bind.need_lying").withStyle(ChatFormatting.RED);
        }


        if(!hasRopeOnArms){
            return Component.translatable("item.restraint_dungeon.rope.connect_bind.need_rope_bind_arms").withStyle(ChatFormatting.RED);
        }
        if(!hasRopeOnLegs){
            return Component.translatable("item.restraint_dungeon.rope.connect_bind.need_rope_bind_legs").withStyle(ChatFormatting.RED);
        }

        if(getArmsPose(target) != ArmsPose.CROSS_BEHIND_BACK){
            return Component.translatable("item.restraint_dungeon.rope.connect_bind.need_correct_arms_pose").withStyle(ChatFormatting.RED);
        }

        if(getLegsPose(target) != LegsPose.LEGS_TOGETHER){
            return Component.translatable("item.restraint_dungeon.rope.connect_bind.need_correct_legs_pose").withStyle(ChatFormatting.RED);
        }

        return null;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，玩家在切换到连接状态前所需要的姿势,若为Null则该连接拘束具不会限制姿势切换
     * @param entity 当前实体
     */
    public RestraintPosition getConnectBindPreviousPosition(LivingEntity entity){
        return RestraintPosition.LYING_DOWN;
    }

    @Override
    public String getConnectBindAnimation(LivingEntity entity){
        return "connection_rope";
    }

    @Override
    public String getConnectBindTranslateAnimation(LivingEntity entity){
        return "connection_rope_to";
    }

    @Override
    public String getConnectBindReleaseAnimation(LivingEntity entity){
        return "connection_rope_back";
    }

    @Override
    public String getConnectBindStrugglingAnimation(LivingEntity entity){
        return "connection_rope_struggle";
    }

    @Override
    public Vector3f getConnectBindViewOffset(Player player, ItemStack stack){
        return new Vector3f(0.0f, -1.25f, -1.0f);
    }

    @Override
    public double onStrengthStruggle(UUID playerUUID, double ItemStrengthIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearCutStrugglingState(player)) {
                return ItemStrengthIndex * 2.0;
            }
        }
        return super.onStrengthStruggle(playerUUID, ItemStrengthIndex);
    }
    @Override
    public double onLooseStruggle(UUID playerUUID,double ItemLooseIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearCutStrugglingState(player)) {
                return ItemLooseIndex * 2.0;
            }
        }
        return super.onLooseStruggle(playerUUID, ItemLooseIndex);
    }
    @Override
    public double onUnlockStruggle(UUID playerUUID,double ItemLockIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearHookStrugglingState(player)) {
                return ItemLockIndex * 2.0;
            }
        }
        return super.onLooseStruggle(playerUUID, ItemLockIndex);
    }

    @Override
    public Vector3f getConnectBindViewRotation(Player player, ItemStack stack){
        return new Vector3f(0.0f, 180.0f,0.0f);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.rope").withStyle(ChatFormatting.GRAY));

    }
}
