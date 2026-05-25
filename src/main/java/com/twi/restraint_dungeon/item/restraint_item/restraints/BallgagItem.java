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

public class BallgagItem extends RestraintItem {

    public static final RestraintDefaults BALL_GAG_DEFAULTS = new RestraintDefaults(
            100,
            20.0,
            0.25,
            0.75,
            1.25
    );

    private final List<String> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_gag.toString()
    );

    public BallgagItem(Properties properties) {
        super(properties.stacksTo(1), BALL_GAG_DEFAULTS);
        this.setCanEquipPartList(canEquipPartList);
        this.setConnectPartMap(new HashMap<>());
        this.setCanBeLocked(true);
    }

    @Override
    public boolean canStuffedGag(LivingEntity entity, ItemStack gagStack) {
        return true;
    }

    @Override
    public boolean canBlockedGag(LivingEntity entity, ItemStack gagStack) {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.ball_gag").withStyle(ChatFormatting.GRAY));
    }
}