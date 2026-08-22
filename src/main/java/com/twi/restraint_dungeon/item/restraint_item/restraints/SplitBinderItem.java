package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class SplitBinderItem extends RestraintItem {

    public static final RestraintDefaults SPLIT_BINDER_DEFAULTS = new RestraintDefaults(
            300,
            40.0,
            0.1,
            0.1,
            1.25
    );

    public SplitBinderItem(Properties properties) {
        super(properties.stacksTo(1), SPLIT_BINDER_DEFAULTS);

        this.setCanEquipPartList(List.of(PlayerRestraintPart.restraint_arms_bind,PlayerRestraintPart.restraint_legs_bind));

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

    @Override
    public Component canUseKidnap(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        if(getRestraintPosition(target) == RestraintPositionEvent.RestraintPosition.STANDING
                || getRestraintPosition(target) == RestraintPositionEvent.RestraintPosition.CARRIED
                || getRestraintPosition(target) == RestraintPositionEvent.RestraintPosition.CONNECTING
                || getRestraintPosition(target) == RestraintPositionEvent.RestraintPosition.RIDING){
            return Component.translatable("item." + MODID + ".split_binder.invalid_position").withStyle(ChatFormatting.DARK_RED);
        }
        return null;
    }

    @Override
    public RestraintCapability.ArmsPose setBindArmsPose(LivingEntity entity){
        if(this.canBindCurrentPart(entity)){
            return RestraintCapability.ArmsPose.SPLIT_ARMS;
        }
        return RestraintCapability.ArmsPose.NONE;
    }

    @Override
    public RestraintCapability.LegsPose setBindLegsPose(LivingEntity entity){
        if(this.canBindCurrentPart(entity)){
            return RestraintCapability.LegsPose.SPLIT_LEGS;
        }
        return RestraintCapability.LegsPose.NONE;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.split_binder").withStyle(ChatFormatting.GRAY));
    }
}