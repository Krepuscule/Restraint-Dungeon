package com.twi.restraint_dungeon.effect.ModEffect;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.LatexItem;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.MerchantSlimeItem;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.SlimeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.addRestraintItem;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;

public class StickyEffect extends MobEffect {
    public StickyEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {

        PlayerRestraintPart targetPart = null;
        if (checkCanAdd(entity, PlayerRestraintPart.restraint_legs_bind)) targetPart = PlayerRestraintPart.restraint_legs_bind;
        else if (checkCanAdd(entity, PlayerRestraintPart.restraint_body_bind)) targetPart = PlayerRestraintPart.restraint_body_bind;
        else if (checkCanAdd(entity, PlayerRestraintPart.restraint_arms_bind)) targetPart = PlayerRestraintPart.restraint_arms_bind;
        else if (checkCanAdd(entity, PlayerRestraintPart.restraint_hands_bind)) targetPart = PlayerRestraintPart.restraint_hands_bind;
        else if (checkCanAdd(entity, PlayerRestraintPart.restraint_gag)) targetPart = PlayerRestraintPart.restraint_gag;
        else if (checkCanAdd(entity, PlayerRestraintPart.restraint_blindfold)) targetPart = PlayerRestraintPart.restraint_blindfold;

        if (targetPart != null) {
            ItemStack slimeStack;
            String messageKey;

            if (amplifier == 0) {
                slimeStack = new ItemStack(ModRestraintItems.SLIME.get());
                messageKey = "item.restraint_dungeon.slime.adding";
            } else {
                slimeStack = new ItemStack(ModRestraintItems.MERCHANT_SLIME.get());
                messageKey = "item.restraint_dungeon.merchant_slime.adding";
            }


            boolean success = addRestraintItem(entity, targetPart, slimeStack);

            if (success && entity instanceof Player player) {
                player.displayClientMessage(
                        Component.translatable(messageKey).withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }
        }

        return true;
    }

    private boolean checkCanAdd(LivingEntity entity, PlayerRestraintPart part) {

        if(!(entity instanceof Player)) return false;

        List<ItemStack> existing = getAllPartRestraint(entity, part);
        if (existing.isEmpty()) return true;
        for (ItemStack s : existing) {
            Item item = s.getItem();
            if (item instanceof MerchantSlimeItem || item instanceof SlimeItem || item instanceof LatexItem) {
                return false;
            }
        }
        return true;
    }
}