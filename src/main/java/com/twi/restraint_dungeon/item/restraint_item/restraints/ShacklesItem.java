package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class ShacklesItem extends RestraintItem {

    public static final RestraintDefaults SHACKLES_DEFAULTS = new RestraintDefaults(
            500,
            5.0,
            0.25,
            0.5,
            1.5
    );

    public ShacklesItem(Properties properties) {
        super(properties.stacksTo(1), SHACKLES_DEFAULTS);

        this.setCanEquipPartList(List.of(
                PlayerRestraintPart.restraint_arms_bind.toString(),
                PlayerRestraintPart.restraint_legs_bind.toString()
        ));

        this.setConnectPartMap(new HashMap<>());

        this.setCanBeLocked(true);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {

        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.shackles")
                .withStyle(ChatFormatting.GRAY));
    }
}