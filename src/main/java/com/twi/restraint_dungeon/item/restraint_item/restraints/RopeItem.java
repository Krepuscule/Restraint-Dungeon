package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils;
import net.minecraft.ChatFormatting;
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

public class RopeItem extends RestraintItem {

    public static final RestraintDefaults ROPE_DEFAULTS = new RestraintDefaults(
            100,
            30.0,
            0.5,
            1.25,
            0.75
    );

    // 允许装备的部位列表
    private final List<String> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_gag.toString(),
            PlayerRestraintPart.restraint_body_bind.toString(),
            PlayerRestraintPart.restraint_arms_bind.toString(),
            PlayerRestraintPart.restraint_legs_bind.toString(),
            PlayerRestraintPart.restraint_connection.toString()
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


//        if(RestraintUtils.getPlayerRestraintPosition(target) != PlayerPosition.LYING){
//            return Component.translatable("item.restraint.rope.connect_bind.need_lying").withStyle(ChatFormatting.RED);
//        }


        if(!hasRopeOnArms){
            return Component.translatable("item.restraint_dungeon.rope.connect_bind.need_rope_bind_arms").withStyle(ChatFormatting.RED);
        }
        if(!hasRopeOnLegs){
            return Component.translatable("item.restraint_dungeon.rope.connect_bind.need_rope_bind_legs").withStyle(ChatFormatting.RED);
        }

        return null;
    }

    @Override
    public String getConnectBindAnimation(LivingEntity entity){
        return "player_connect_rope_common";
    }

    @Override
    public String getConnectBindTranslateAnimation(LivingEntity entity){
        return "player_connect_rope_bind";
    }

    @Override
    public String getConnectBindReleaseAnimation(LivingEntity entity){
        return "player_connect_rope_release";
    }

    @Override
    public String getConnectBindStrugglingAnimation(LivingEntity entity){
        return "player_connect_rope_struggle";
    }

    @Override
    public Vec3 getConnectBindViewOffset(Player player, ItemStack stack){
        return new Vec3(0.05, -1.2, -0.25);
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
