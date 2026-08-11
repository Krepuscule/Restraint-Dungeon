package com.twi.restraint_dungeon.item.restraint_item.restraints;


import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.DataComponentsUtils;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
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

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.addRestraintItem;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllGag;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;

public class CursedCollarItem extends RestraintItem {

    public static final RestraintDefaults CURSED_COLLAR_DEFAULTS = new RestraintDefaults(
            500,
            50.0,
            0.1,
            0.1,
            0.1
    );

    public CursedCollarItem(Properties properties) {
        super(properties.stacksTo(1), CURSED_COLLAR_DEFAULTS);
        this.setCanEquipPartList(List.of(PlayerRestraintPart.restraint_collar));
        this.setConnectPartMap(new HashMap<>());
        this.setCanBeLocked(true);
    }

    @Override
    public void onEquip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (!entity.level().isClientSide) {
            DataComponentsUtils.updateEquipTime(entity,stack,entity.level().getGameTime());
        }
    }

    @Override
    public void onUnequip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (!entity.level().isClientSide) {
            DataComponentsUtils.removeActivateTime(entity,stack);
        }
    }

    @Override
    public void onRestraintTick(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (entity == null || entity.level().isClientSide) return;

        if (part == PlayerRestraintPart.restraint_collar) {
            long equipTime = DataComponentsUtils.getEquipTime(stack);
            long currentTime = entity.level().getGameTime();
            long diff = currentTime - equipTime;

            if (diff > 0 && diff % getSpreadTime(entity,stack,part,index) == 0) {
                executeCurseSpread(entity);
            }
        }
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

    private void executeCurseSpread(LivingEntity entity) {
        PlayerRestraintPart targetPart = null;

        if (getAllRestraintsByPart(entity, PlayerRestraintPart.restraint_body_bind).isEmpty()) {
            targetPart = PlayerRestraintPart.restraint_body_bind;
        } else if (getAllRestraintsByPart(entity, PlayerRestraintPart.restraint_legs_bind).isEmpty()) {
            targetPart = PlayerRestraintPart.restraint_legs_bind;
        } else if (getAllRestraintsByPart(entity, PlayerRestraintPart.restraint_arms_bind).isEmpty()) {
            targetPart = PlayerRestraintPart.restraint_arms_bind;
        } else if (getAllGag(entity).isEmpty()) {
            targetPart = PlayerRestraintPart.restraint_gag;
        }

        if (targetPart != null) {
            boolean success = addRestraintItem(entity, targetPart, new ItemStack(ModRestraintItems.MAGIC_ROPE.get()));
            if (success && entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("item.restraint_dungeon.cursed_collar.add_magic_rope")
                        .withStyle(ChatFormatting.DARK_RED), true);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.cursed_collar").withStyle(ChatFormatting.GRAY));
    }

    private long getSpreadTime(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        return 1200;
    }
}