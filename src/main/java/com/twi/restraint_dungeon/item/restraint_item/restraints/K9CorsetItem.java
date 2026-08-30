package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class K9CorsetItem extends RestraintItem {
    public static final RestraintDefaults K9_Corset_DEFAULTS = new RestraintDefaults(
            300,
            50.0,
            0.1,
            0.1,
            0.1
    );

    public K9CorsetItem(Properties properties) {
        super(properties.stacksTo(1), K9_Corset_DEFAULTS);

        this.setCanEquipPartList(List.of(RestraintCapability.PlayerRestraintPart.restraint_connection));

        this.setCanBeLocked(true);
    }

    @Override
    public double onStrengthStruggle(UUID playerUUID, double ItemStrengthIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearHookStrugglingState(player)) {
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
            if(isNearHookStrugglingState(player)) {
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

    public RestraintPositionEvent.RestraintPosition getConnectBindPreviousPosition(LivingEntity entity){
        return RestraintPositionEvent.RestraintPosition.LYING_DOWN;
    }

    @Override
    public boolean shouldRenderConnectionBind(LivingEntity entity){
        return true;
    }

    @Override
    public Component canConnectBind(LivingEntity actionEntity, LivingEntity target, ItemStack stack) {

        boolean hasBinderOnArms = false;
        boolean hasBinderOnLegs = false;

        // 检查手臂是否有连缚拘束套
        for(ItemStack item : RestraintUtils.getAllArmsBind(target)){
            if(item.getItem() instanceof SplitBinderItem) {
                hasBinderOnArms = true;
                break;
            }
        }
        // 检查腿部是否有连缚拘束套
        for(ItemStack item : RestraintUtils.getAllLegsBind(target)){
            if(item.getItem() instanceof SplitBinderItem) {
                hasBinderOnLegs = true;
                break;
            }
        }


        if(getRestraintPosition(target) != RestraintPositionEvent.RestraintPosition.LYING_DOWN){
            return Component.translatable("item." + MODID + ".k9_corset.connect_bind.need_lying_down").withStyle(ChatFormatting.RED);
        }


        if(!hasBinderOnArms){
            return Component.translatable("item." + MODID + ".k9_corset.connect_bind.need_split_bind_arms").withStyle(ChatFormatting.RED);
        }
        if(!hasBinderOnLegs){
            return Component.translatable("item." + MODID + ".k9_corset.connect_bind.need_split_bind_legs").withStyle(ChatFormatting.RED);
        }

        if(getArmsPose(target) != RestraintCapability.ArmsPose.SPLIT_ARMS){
            return Component.translatable("item." + MODID + ".k9_corset.connect_bind.need_correct_arms_pose").withStyle(ChatFormatting.RED);
        }

        if(getLegsPose(target) != RestraintCapability.LegsPose.SPLIT_LEGS){
            return Component.translatable("item." + MODID + ".k9_corset.connect_bind.need_correct_legs_pose").withStyle(ChatFormatting.RED);
        }

        return null;
    }

    @Override
    public String getConnectBindAnimation(LivingEntity entity){
        return "connection_k9";
    }

    @Override
    public String getConnectBindTranslateAnimation(LivingEntity entity){
        return "connection_k9_to";
    }

    @Override
    public String getConnectBindReleaseAnimation(LivingEntity entity){
        return "connection_k9_back";
    }

    @Override
    public String getConnectBindStrugglingAnimation(LivingEntity entity){
        return "connection_k9_struggle";
    }

    @Override
    public Vector3f getConnectBindViewOffset(Player player, ItemStack stack){
        return new Vector3f(0.0f, -0.15f, -1.0f);
    }

    @Override
    public Vector3f getConnectBindViewRotation(Player player, ItemStack stack){
        return new Vector3f(0.0f, 180.0f, 0.0f);
    }

    @Override
    public List<Float> getConnectBindEntityDimensions(LivingEntity entity, ItemStack stack){

        return List.of(1.25F,0.6F);
    }

    @Override
    public List<Double> getConnectBindLeashOffset(LivingEntity entity, ItemStack stack){

        return List.of(0.0D, -0.3D, -1.2D);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.k9_corset").withStyle(ChatFormatting.GRAY));
    }
}