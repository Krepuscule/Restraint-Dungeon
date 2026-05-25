package com.twi.restraint_dungeon.utils.mod_utils.pleasant;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;

public class ThrillUtils {

    private static final double BLINDFOLD_THRILL_MULTIP = 2.0;
    private static final double GAG_THRILL_MULTIP = 1.5;
    private static final double COLLAR_THRILL_MULTIP = 2.0;
    private static final double BODY_THRILL_MULTIP = 1.5;

    // 计算单个部位的敏感值
    public static double calThrillValueByPart(LivingEntity entity, PlayerRestraintPart bodyPart) {
        if (entity == null) return 0;
        double totalThrill = 0;

        List<ItemStack> stacks = getAllPartRestraint(entity, bodyPart);
        double multiplier = getMultiplierForPart(bodyPart);

        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof RestraintItem restraintItem) {
                totalThrill += restraintItem.getThrillValue(stack) * multiplier;
            }
        }
        return totalThrill;
    }

    private static double getMultiplierForPart(PlayerRestraintPart part) {
        return switch (part) {
            case restraint_blindfold -> BLINDFOLD_THRILL_MULTIP;
            case restraint_gag -> GAG_THRILL_MULTIP;
            case restraint_collar -> COLLAR_THRILL_MULTIP;
            case restraint_body_bind -> BODY_THRILL_MULTIP;
            default -> 1.0;
        };
    }

    // 计算全身的敏感值总和（所有部位的拘束具敏感度加和）
    public static double calPlayerFullThrillLevel(LivingEntity entity) {
        double sum = 0;
        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            sum += calThrillValueByPart(entity, part);
        }
        return sum;
    }

    // 计算敏感度等级
    public static int calPlayerThrillLevel(LivingEntity entity) {
        if (entity == null) return 0;

        double thrillValue = calPlayerFullThrillLevel(entity);
        if (thrillValue <= 0) return 0;
        if (thrillValue <= 50) return 1;
        if (thrillValue <= 200) return 2;
        if (thrillValue <= 500) return 3;
        if (thrillValue <= 1000) return 4;
        if (thrillValue <= 1200) return 5;
        if (thrillValue <= 1500) return 6;
        if (thrillValue <= 2000) return 7;
        if (thrillValue <= 2500) return 8;
        if (thrillValue <= 3000) return 9;
        return 10;
    }

    // 获取敏感值等级
    public static int getThrillLevel(LivingEntity entity) {
        if (entity == null) return 0;
        return entity.getData(ModAttachments.ENTITY_PLEASANT).getThrillLevel();
    }

    // 更新敏感度等级
    public static void updateThrillLevel(LivingEntity entity, int thrillLevel) {
        if (entity == null) return;
        var data = entity.getData(ModAttachments.ENTITY_PLEASANT);
        data.setThrillLevel(thrillLevel);
        entity.setData(ModAttachments.ENTITY_PLEASANT, data);
    }
}
