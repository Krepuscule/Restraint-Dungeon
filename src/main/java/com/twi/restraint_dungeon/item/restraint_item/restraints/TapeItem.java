package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

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
                PlayerRestraintPart.restraint_blindfold.toString(),
                PlayerRestraintPart.restraint_gag.toString(),
                PlayerRestraintPart.restraint_body_bind.toString(),
                PlayerRestraintPart.restraint_arms_bind.toString(),
                PlayerRestraintPart.restraint_legs_bind.toString(),
                PlayerRestraintPart.restraint_hands_bind.toString()
        ));

        this.setConnectPartMap(new HashMap<>());

        this.setCanBeLocked(false);
    }

    /**
     * 是否可以作为填充式口球使用
     */
    @Override
    public boolean canStuffedGag(LivingEntity entity, ItemStack gagStack) {
        return false;
    }

    /**
     * 是否可以作为覆盖/封口式口球使用
     */
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