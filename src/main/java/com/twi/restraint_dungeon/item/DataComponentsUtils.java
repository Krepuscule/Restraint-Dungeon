package com.twi.restraint_dungeon.item;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.calPlayerThrillLevel;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.updateThrillLevel;

public class DataComponentsUtils {
    public static void syncDataComponents(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);
        entity.setData(ModAttachments.RESTRAINT_STACK, cap);
    }

    // --- MaxResistance ---
    public static int getMaxResistance(ItemStack stack, int defaultValue) {
        return stack.getOrDefault(ModDataComponents.MAX_RESISTANCE.get(), defaultValue);
    }

    public static void updateMaxResistance(LivingEntity entity, ItemStack stack, int value) {
        stack.set(ModDataComponents.MAX_RESISTANCE.get(), value);
        syncDataComponents(entity);
    }

    // --- ThrillValue ---
    public static double getThrillValue(ItemStack stack, double defaultValue) {
        return stack.getOrDefault(ModDataComponents.THRILL_VALUE.get(), defaultValue);
    }

    public static void updateThrillValue(LivingEntity entity, ItemStack stack, double value) {
        stack.set(ModDataComponents.THRILL_VALUE.get(), value);
        syncDataComponents(entity);

        int newThrill = calPlayerThrillLevel(entity);
        updateThrillLevel(entity, newThrill);
    }

    // --- StrengthIndex ---
    public static double getStrengthIndex(ItemStack stack, double defaultValue) {
        return stack.getOrDefault(ModDataComponents.STRENGTH_INDEX.get(), defaultValue);
    }

    public static void updateStrengthIndex(LivingEntity entity, ItemStack stack, double value) {
        stack.set(ModDataComponents.STRENGTH_INDEX.get(), value);
        syncDataComponents(entity);
    }

    // --- LooseIndex ---
    public static double getLooseIndex(ItemStack stack, double defaultValue) {
        return stack.getOrDefault(ModDataComponents.LOOSE_INDEX.get(), defaultValue);
    }

    public static void updateLooseIndex(LivingEntity entity, ItemStack stack, double value) {
        stack.set(ModDataComponents.LOOSE_INDEX.get(), value);
        syncDataComponents(entity);
    }

    // --- LockIndex ---
    public static double getLockIndex(ItemStack stack, double defaultValue) {
        return stack.getOrDefault(ModDataComponents.LOCK_INDEX.get(), defaultValue);
    }

    public static void updateLockIndex(LivingEntity entity, ItemStack stack, double value) {
        stack.set(ModDataComponents.LOCK_INDEX.get(), value);
        syncDataComponents(entity);
    }

    // --- LockItem ---
    public static ItemStack getLockItem(LivingEntity entity, ItemStack stack) {
        CustomData data = stack.get(ModDataComponents.LOCK_ITEM.get());
        if (data == null || data.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parse(entity.level().registryAccess(), data.copyTag()).orElse(ItemStack.EMPTY);
    }

    public static void updateLockItem(LivingEntity entity, ItemStack stack, ItemStack lockItem) {
        if (lockItem.isEmpty()) {
            stack.remove(ModDataComponents.LOCK_ITEM.get());
        } else {
            Tag nbt = lockItem.save(entity.level().registryAccess());
            stack.set(ModDataComponents.LOCK_ITEM.get(), CustomData.of((CompoundTag) nbt));
        }
        syncDataComponents(entity);
    }

    // --- MiraiModules (Map) ---
    public static Map<String, Boolean> getMiraiModules(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MIRAI_MODULES.get(), Collections.emptyMap());
    }

    public static void updateMiraiModules(LivingEntity entity, ItemStack stack, Map<String, Boolean> modules) {
        stack.set(ModDataComponents.MIRAI_MODULES.get(), Map.copyOf(modules));
        syncDataComponents(entity);
    }

    public static void updateSingleMiraiModule(LivingEntity entity, ItemStack stack, String key, boolean value) {
        Map<String, Boolean> modules = new HashMap<>(getMiraiModules(stack));
        modules.put(key, value);
        updateMiraiModules(entity, stack, modules);
    }

    public static void removeMiraiModules(LivingEntity entity, ItemStack stack) {
        stack.remove(ModDataComponents.MIRAI_MODULES.get());
        syncDataComponents(entity);
    }

    public static void removeSingleMiraiModule(LivingEntity entity, ItemStack stack, String key) {
        Map<String, Boolean> modules = new HashMap<>(getMiraiModules(stack));
        if (modules.containsKey(key)) {
            modules.remove(key);
            updateMiraiModules(entity, stack, modules);
        }
    }

    // --- ArousedMode ---
    public static boolean getArousedMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.AROUSED_MODE.get(), false);
    }

    public static void updateArousedMode(LivingEntity entity, ItemStack stack, boolean active) {
        stack.set(ModDataComponents.AROUSED_MODE.get(), active);
        syncDataComponents(entity);
    }

    public static void removeArousedMode(LivingEntity entity, ItemStack stack) {
        stack.remove(ModDataComponents.AROUSED_MODE.get());
        syncDataComponents(entity);
    }

    // --- DenyMode ---
    public static boolean getDenyMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.DENY_MODE.get(), false);
    }

    public static void updateDenyMode(LivingEntity entity, ItemStack stack, boolean active) {
        stack.set(ModDataComponents.DENY_MODE.get(), active);
        syncDataComponents(entity);
    }

    public static void removeDenyMode(LivingEntity entity, ItemStack stack) {
        stack.remove(ModDataComponents.DENY_MODE.get());
        syncDataComponents(entity);
    }

    // --- EquipTime & ActivateTime ---
    public static long getEquipTime(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.EQUIP_TIME.get(), 0L);
    }

    public static void updateEquipTime(LivingEntity entity, ItemStack stack, long time) {
        stack.set(ModDataComponents.EQUIP_TIME.get(), time);
        syncDataComponents(entity);
    }

    public static void removeEquipTime(LivingEntity entity, ItemStack stack) {
        stack.remove(ModDataComponents.EQUIP_TIME.get());
        syncDataComponents(entity);
    }

    public static long getActivateTime(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ACTIVATE_TIME.get(), 0L);
    }

    public static void updateActivateTime(LivingEntity entity, ItemStack stack, long time) {
        stack.set(ModDataComponents.ACTIVATE_TIME.get(), time);
        syncDataComponents(entity);
    }

    public static void removeActivateTime(LivingEntity entity, ItemStack stack) {
        stack.remove(ModDataComponents.ACTIVATE_TIME.get());
        syncDataComponents(entity);
    }

    // --- DENY_ACTIVATE_TIME ---

    public static long getDenyActivateTime(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.DENY_ACTIVATE_TIME.get(), 0L);
    }

    public static void updateDenyActivateTime(LivingEntity entity, ItemStack stack, long time) {
        stack.set(ModDataComponents.DENY_ACTIVATE_TIME.get(), time);
        syncDataComponents(entity);
    }

    public static void removeDenyActivateTime(LivingEntity entity, ItemStack stack) {
        stack.remove(ModDataComponents.DENY_ACTIVATE_TIME.get());
        syncDataComponents(entity);
    }
}
