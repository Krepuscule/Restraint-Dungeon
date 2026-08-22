package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearCutStrugglingState;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class TapeItem extends RestraintItem {

    public static final RestraintDefaults TAPE_DEFAULTS = new RestraintDefaults(
            300,
            15.0,
            1.25,
            0.25,
            0.25
    );

    public TapeItem(Properties properties) {
        super(properties.stacksTo(1), TAPE_DEFAULTS);

        this.setCanEquipPartList(List.of(
                PlayerRestraintPart.restraint_blindfold,
                PlayerRestraintPart.restraint_gag,
                PlayerRestraintPart.restraint_body_bind,
                PlayerRestraintPart.restraint_arms_bind,
                PlayerRestraintPart.restraint_legs_bind,
                PlayerRestraintPart.restraint_hands_bind
        ));

        this.setConnectPartMap(new HashMap<>());

        this.setCanBeLocked(false);
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

    public RestraintCapability.ArmsPose setBindArmsPose(LivingEntity entity){
        if(this.canBindCurrentPart(entity)){
            return RestraintCapability.ArmsPose.CROSS_UP;
        }
        return RestraintCapability.ArmsPose.NONE;
    }


    @Override
    public boolean canStuffedGag(LivingEntity entity, ItemStack gagStack) {
        return false;
    }


    @Override
    public boolean canBlockedGag(LivingEntity entity, ItemStack gagStack) {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.tape")
                .withStyle(ChatFormatting.GRAY));
    }
}