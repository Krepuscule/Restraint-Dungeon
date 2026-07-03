package com.twi.restraint_dungeon.utils.restraint_stack;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.restraint_stack.RestraintTools;
import com.twi.restraint_dungeon.event.custom_event.RestraintToolsEquipEvent;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;

public class RestraintToolsUtils {

    public static void sync(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;

        var cap = entity.getData(ModAttachments.RESTRAINT_TOOLS);
        entity.setData(ModAttachments.RESTRAINT_TOOLS, cap);
    }

    public static ItemStack getRestraintToolByIndex(LivingEntity entity, int index) {
        if (entity == null) return ItemStack.EMPTY;
        return entity.getData(ModAttachments.RESTRAINT_TOOLS).getByIndex(index);
    }

    public static List<ItemStack> getAllRestraintTools(LivingEntity entity) {
        if (entity == null) return List.of();
        return entity.getData(ModAttachments.RESTRAINT_TOOLS).getToolsList();
    }

    public static boolean hasTool(LivingEntity entity, ItemStack stack, boolean isStrict){
        if (entity == null || stack.isEmpty() || !(stack.getItem() instanceof RestraintToolItem)) return false;
        List<ItemStack> list = getAllRestraintTools(entity);
        for (ItemStack cur_stack : list) {
            if (isStrict ? ItemStack.matches(cur_stack, stack) : ItemStack.isSameItem(cur_stack, stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMaxToolsEquip(LivingEntity entity, ItemStack stack, boolean isStrict){
        if (entity == null || stack.isEmpty() || !(stack.getItem() instanceof RestraintToolItem rt)) return false;
        int count = 0;
        List<ItemStack> list = getAllRestraintTools(entity);
        for (ItemStack cur_stack : list) {
            if (isStrict ? ItemStack.matches(cur_stack, stack) : ItemStack.isSameItem(cur_stack, stack)) {
                count++;
            }
        }
        return rt.getMaxUsage(entity, stack) <= count;
    }

    public static boolean isToolsFull(LivingEntity entity) {
        if (entity == null) return true;
        return getAllRestraintTools(entity).size() >= 5;
    }

    public static void addRestraintTool(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack.isEmpty()) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_TOOLS);
        int targetIndex = cap.getToolsList().size();
        ItemStack newStack = stack.copy();

        if (newStack.getItem() instanceof RestraintToolItem toolItem && toolItem.canEquip(entity)) {
            cap.addToLast(newStack);
            entity.setData(ModAttachments.RESTRAINT_TOOLS, cap);

            sync(entity);

            NeoForge.EVENT_BUS.post(new RestraintToolsEquipEvent.Equipped(entity, targetIndex, newStack));
        }
    }

    public static void addRestraintTool(LivingEntity entity, ItemStack stack, int index) {
        if (entity == null || stack.isEmpty()) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_TOOLS);
        ItemStack newStack = stack.copy();

        if (newStack.getItem() instanceof RestraintToolItem toolItem && toolItem.canEquip(entity)) {
            cap.addByIndex(index, newStack);
            entity.setData(ModAttachments.RESTRAINT_TOOLS, cap);

            sync(entity);

            NeoForge.EVENT_BUS.post(new RestraintToolsEquipEvent.Equipped(entity, index, newStack));
        }
    }

    public static ItemStack removeRestraintTool(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;
        var cap = entity.getData(ModAttachments.RESTRAINT_TOOLS);
        int lastIndex = cap.getToolsList().size() - 1;

        ItemStack removed = cap.removeFromLast();
        if (!removed.isEmpty() && removed.getItem() instanceof RestraintToolItem toolItem && toolItem.canUnequip(entity, lastIndex)) {
            entity.setData(ModAttachments.RESTRAINT_TOOLS, cap);

            sync(entity);

            NeoForge.EVENT_BUS.post(new RestraintToolsEquipEvent.Unequipped(entity, lastIndex, removed));
        }
        return removed;
    }

    public static ItemStack removeRestraintTool(LivingEntity entity, int index) {
        if (entity == null) return ItemStack.EMPTY;
        var cap = entity.getData(ModAttachments.RESTRAINT_TOOLS);

        ItemStack removed = cap.removeByIndex(index);
        if (!removed.isEmpty() && removed.getItem() instanceof RestraintToolItem toolItem && toolItem.canUnequip(entity, index)) {
            entity.setData(ModAttachments.RESTRAINT_TOOLS, cap);

            sync(entity);

            NeoForge.EVENT_BUS.post(new RestraintToolsEquipEvent.Unequipped(entity, index, removed));
        }
        return removed;
    }

    public static void replaceRestraintToolByIndex(LivingEntity entity, int index, ItemStack newStack) {
        if (entity == null) return;
        removeRestraintTool(entity, index);
        addRestraintTool(entity, newStack, index);
    }

    public static void dropAndClearRestraintTools(LivingEntity entity) {
        if (entity == null) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_TOOLS);
        List<ItemStack> list = cap.getToolsList();

        for (int i = list.size() - 1; i >= 0; i--) {
            ItemStack item = list.get(i);
            entity.spawnAtLocation(item);
            NeoForge.EVENT_BUS.post(new RestraintToolsEquipEvent.Unequipped(entity, i, item));
        }
        cap.clear();
        entity.setData(ModAttachments.RESTRAINT_TOOLS, cap);

        sync(entity);
    }
}