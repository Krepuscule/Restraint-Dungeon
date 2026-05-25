package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicRopeItem extends RestraintItem {

    public static final RestraintDefaults MAGIC_ROPE_DEFAULTS = new RestraintDefaults(
            200,
            50.0,
            0.5,
            1.25,
            0.75
    );

    public MagicRopeItem(Properties properties) {
        super(properties.stacksTo(1), MAGIC_ROPE_DEFAULTS);

        this.setCanEquipPartList(List.of(
                PlayerRestraintPart.restraint_gag.toString(),
                PlayerRestraintPart.restraint_body_bind.toString(),
                PlayerRestraintPart.restraint_arms_bind.toString(),
                PlayerRestraintPart.restraint_legs_bind.toString()
        ));

        // 设置互联逻辑
        Map<String, List<String>> connectMap = new HashMap<>();
        connectMap.put(PlayerRestraintPart.restraint_body_bind.toString(),
                List.of(PlayerRestraintPart.restraint_arms_bind.toString(), PlayerRestraintPart.restraint_legs_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_arms_bind.toString(),
                List.of(PlayerRestraintPart.restraint_body_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_legs_bind.toString(),
                List.of(PlayerRestraintPart.restraint_body_bind.toString()));

        this.setConnectPartMap(connectMap);
        this.setCanBeLocked(false);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.magic_rope").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public RestraintDropRule getDropRule(LivingEntity entity, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return RestraintDropRule.DESTROY;

    }

    @Override
    public boolean dropRestraintWhenStruggleOff(LivingEntity actionEntity, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        return false;
    }

    @Override
    public boolean dropRestraintWhenRelease(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        return false;
    }
}