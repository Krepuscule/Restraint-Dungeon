package com.twi.restraint_dungeon.utils.mod_utils.pleasant;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.attributes.ModAttributes;
import com.twi.restraint_dungeon.effect.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;

public class PleasantUtils {

    // 获取高潮值信息
    public static double getPleasantValue(LivingEntity entity) {
        if (entity == null) return 0;
        return entity.getData(ModAttachments.ENTITY_PLEASANT).getPleasantValue();
    }

    public static void updatePleasantValue(LivingEntity entity, double pleasantValue) {
        if (entity == null) return;
        var data = entity.getData(ModAttachments.ENTITY_PLEASANT);
        data.setPleasantValue(pleasantValue);
        entity.setData(ModAttachments.ENTITY_PLEASANT, data);
    }


    public static void IncreasePleasantValue(LivingEntity entity, double value) {
        if (entity == null) return;
        if (entity.hasEffect(ModEffects.CLIMAX)
                || entity.hasEffect(ModEffects.CALM)) {
            return;
        }

        double currentVal = getPleasantValue(entity);
        double pleasantValue = currentVal + value;

        if (pleasantValue >= 100 && !entity.hasEffect(ModEffects.CLIMAX_DENY)) {
            int amplifier = Math.max(0, getThrillLevel(entity) - 1);
            entity.addEffect(new MobEffectInstance(
                    ModEffects.CLIMAX,
                    6000,
                    amplifier,
                    false,
                    false
            ));
            pleasantValue = 100;
        }

        if (pleasantValue < 0) pleasantValue = 0;

        if(pleasantValue >= 100 && entity.hasEffect(ModEffects.CLIMAX_DENY)) return;

        updatePleasantValue(entity, pleasantValue);
    }

    public static void DecreasePleasantValue(LivingEntity entity, double value) {
        if (entity == null) return;
        if (entity.hasEffect(ModEffects.CLIMAX) || entity.hasEffect(ModEffects.CALM)) {
            return;
        }

        double pleasantValue = Math.max(0, getPleasantValue(entity) - value);

        if(pleasantValue >= 100 && entity.hasEffect(ModEffects.CLIMAX_DENY)) return;

        updatePleasantValue(entity, pleasantValue);
    }


    private static final ResourceLocation PLEASANT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "pleasant_effect");

    /**
     * 更新实体的属性值
     */
    private static final Map<UUID, Double> LAST_REDUCTION_MAP = new WeakHashMap<>();

    public static void updatePleasantAttributes(LivingEntity entity, double pleasantValue) {
        double reduction;
        if (pleasantValue >= 100) reduction = -0.50;
        else if (pleasantValue >= 90) reduction = -0.80;
        else if (pleasantValue >= 85) reduction = -0.50;
        else if (pleasantValue >= 75) reduction = -0.30;
        else if (pleasantValue >= 50) reduction = -0.20;
        else reduction = 0.0;

        UUID entityUUID = entity.getUUID();
        double lastReduction = LAST_REDUCTION_MAP.getOrDefault(entityUUID, 999.0);
        if (Math.abs(lastReduction - reduction) < 1e-4) {
            return;
        }
        LAST_REDUCTION_MAP.put(entityUUID, reduction);

        applyOrRemove(entity, Attributes.MOVEMENT_SPEED, PLEASANT_MODIFIER_ID.withSuffix("_movement_speed"), reduction);
        applyOrRemove(entity, Attributes.ATTACK_DAMAGE, PLEASANT_MODIFIER_ID.withSuffix("_attack_damage"), reduction);
        applyOrRemove(entity, Attributes.ATTACK_SPEED, PLEASANT_MODIFIER_ID.withSuffix("_attack_speed"), reduction);
        applyOrRemove(entity, Attributes.ATTACK_KNOCKBACK, PLEASANT_MODIFIER_ID.withSuffix("_attack_knockback"), reduction);
        applyOrRemove(entity, Attributes.KNOCKBACK_RESISTANCE, PLEASANT_MODIFIER_ID.withSuffix("_knockback_resistance"), reduction);

        applyOrRemove(entity, ModAttributes.RESTRAINT_STRENGTH, PLEASANT_MODIFIER_ID.withSuffix("_restraint_strength"), reduction);
        applyOrRemove(entity, ModAttributes.STRUGGLE_STRENGTH, PLEASANT_MODIFIER_ID.withSuffix("_struggle_strength"), reduction);
        applyOrRemove(entity, ModAttributes.STRUGGLE_SPEED, PLEASANT_MODIFIER_ID.withSuffix("_struggle_speed"), reduction);
        applyOrRemove(entity, ModAttributes.STRUGGLE_RANGE, PLEASANT_MODIFIER_ID.withSuffix("_struggle_range"), reduction);
    }

    private static void applyOrRemove(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id, double amount) {
        AttributeInstance inst = entity.getAttribute(attr);
        if (inst == null) return;

        if (inst.getModifier(id) != null) {
            inst.removeModifier(id);
        }

        if (amount != 0.0) {
            inst.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
