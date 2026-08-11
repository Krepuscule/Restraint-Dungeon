package com.twi.restraint_dungeon.api;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RestraintApiImpl implements IRestraintApi {

    @Override
    public boolean hasRestraint(LivingEntity entity, PlayerRestraintPart part, ItemStack stack, boolean isStrict) {
        if (entity == null || stack.isEmpty()) return false;
        return RestraintUtils.hasRestraint(entity, part, stack, isStrict);
    }

    @Override
    public boolean hasAnyRestraint(LivingEntity entity, PlayerRestraintPart bodyPart) {
        return RestraintUtils.hasAnyRestraint(entity, bodyPart);
    }

    @Override
    public int getRestraintCount(LivingEntity entity, PlayerRestraintPart bodyPart) {
        return RestraintUtils.getRestraintCount(entity, bodyPart);
    }

    @Override
    public ItemStack getPartLastRestraint(LivingEntity entity, PlayerRestraintPart bodyPart) {
        return RestraintUtils.getPartLastRestraint(entity, bodyPart);
    }

    @Override
    public ItemStack getPartRestraintByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, int index) {
        return RestraintUtils.getPartRestraintByIndex(entity, bodyPart, index);
    }

    @Override
    public int getRestraintIndex(LivingEntity entity, PlayerRestraintPart part, ItemStack stack, boolean isStrict) {
        return RestraintUtils.getRestraintIndex(entity, part, stack, isStrict);
    }

    @Override
    public boolean addRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack) {
        return RestraintUtils.addRestraintItem(entity, bodyPart, stack);
    }

    @Override
    public boolean addRestraintItemByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack, int index) {
        return RestraintUtils.addRestraintItemByIndex(entity, bodyPart, stack, index);
    }

    @Override
    public ItemStack removeRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart) {
        return RestraintUtils.removeRestraintItem(entity, bodyPart);
    }

    @Override
    public ItemStack removeRestraintItemByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, int index) {
        return RestraintUtils.removeRestraintItemByIndex(entity, bodyPart, index);
    }

    @Override
    public boolean replaceRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack, int index) {
        return RestraintUtils.replaceRestraintItem(entity, bodyPart, stack, index);
    }


}