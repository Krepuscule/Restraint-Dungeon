package com.twi.restraint_dungeon.utils.restraint_stack;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.restraint_stack.RestraintStack;
import com.twi.restraint_dungeon.event.custom_event.RestraintEquipEvent;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public class RestraintStackUtils {

    /**
     * 检查实体某个部位是否已经达到拘束具上限
     */
    public static boolean isPartFull(LivingEntity entity, PlayerRestraintPart part) {
        if (entity == null) return true;

        int currentCount = getAllRestraintsByPart(entity, part).size();
        int limit = RestraintStack.getPartRestraintLimit(part);

        return currentCount >= limit;
    }

    public static void addRestraint(LivingEntity entity, PlayerRestraintPart part, ItemStack stack) {
        if (entity == null || stack.isEmpty()) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);

        int targetIndex = cap.getPartList(part).size();
        ItemStack newStack = stack.copy();
        if(newStack.getItem() instanceof RestraintItem restraintItem && restraintItem.canEquip(entity, part)) {
            cap.addToLast(part, newStack);
            entity.setData(ModAttachments.RESTRAINT_STACK, cap);

            // 发送 Equipped 事件
            NeoForge.EVENT_BUS.post(new RestraintEquipEvent.Equipped(entity, part, targetIndex, newStack));
        }
    }

    public static void addRestraint(LivingEntity entity, PlayerRestraintPart part,  ItemStack stack, int index) {
        if (entity == null || stack.isEmpty()) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);

        ItemStack newStack = stack.copy();
        if(newStack.getItem() instanceof RestraintItem restraintItem && restraintItem.canEquip(entity, part)) {
            cap.addToLast(part, newStack);
            entity.setData(ModAttachments.RESTRAINT_STACK, cap);

            // 发送 Equipped 事件
            NeoForge.EVENT_BUS.post(new RestraintEquipEvent.Equipped(entity, part, index, newStack));
        }
    }

    public static ItemStack removeRestraint(LivingEntity entity, PlayerRestraintPart part) {
        if (entity == null) return ItemStack.EMPTY;
        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);
        int lastIndex = cap.getPartList(part).size() - 1;

        ItemStack removed = cap.removeFromLast(part);
        if (!removed.isEmpty()
                && removed.getItem() instanceof RestraintItem restraintItem
                && restraintItem.canUnequip(entity, part,lastIndex)) {
            entity.setData(ModAttachments.RESTRAINT_STACK, cap);
            NeoForge.EVENT_BUS.post(new RestraintEquipEvent.Unequipped(entity, part, lastIndex, removed));
        }
        return removed;
    }

    public static ItemStack removeRestraint(LivingEntity entity, PlayerRestraintPart part, int index) {

        if (entity == null) return ItemStack.EMPTY;
        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);

        ItemStack removed = cap.removeByIndex(part, index);
        if (!removed.isEmpty()
                && removed.getItem() instanceof RestraintItem restraintItem
                && restraintItem.canUnequip(entity, part,index)) {
            entity.setData(ModAttachments.RESTRAINT_STACK, cap);
            NeoForge.EVENT_BUS.post(new RestraintEquipEvent.Unequipped(entity, part, index, removed));
        }
        return removed;
    }

    /** * 替换指定索引的物品
     * 触发顺序：Unequipped (旧物品) -> Equipped (新物品)
     */
    public static void replaceRestraintByIndex(LivingEntity entity, PlayerRestraintPart part, int index, ItemStack newStack) {
        if (entity == null) return;
        removeRestraint(entity, part, index);
        addRestraint(entity, part, newStack,index);
    }

    public static ItemStack getRestraintByIndex(LivingEntity entity, PlayerRestraintPart part, int index) {
        if (entity == null) return ItemStack.EMPTY;
        return entity.getData(ModAttachments.RESTRAINT_STACK).getByIndex(part, index);
    }

    public static List<ItemStack> getAllRestraintsByPart(LivingEntity entity, PlayerRestraintPart part) {
        if (entity == null) return List.of();
        return entity.getData(ModAttachments.RESTRAINT_STACK).getPartList(part);
    }

    public static void dropAndClearAllRestraints(LivingEntity entity) {
        if (entity == null) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);
        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            List<ItemStack> list = cap.getPartList(part);
            for (int i = list.size() - 1; i >= 0; i--) {
                ItemStack item = list.get(i);
                if(item.getItem() instanceof RestraintItem ri && ri.dropRestraintWhenRelease(entity,entity,item,part,i)){
                    entity.spawnAtLocation(item);
                }
                NeoForge.EVENT_BUS.post(new RestraintEquipEvent.Unequipped(entity, part, i, item));
            }
            cap.clear(part);
        }
        entity.setData(ModAttachments.RESTRAINT_STACK, cap);
    }

    public static void dropAndClearPartRestraints(LivingEntity entity,PlayerRestraintPart part) {
        if (entity == null) return;
        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);
        List<ItemStack> list = cap.getPartList(part);
        for (int i = list.size() - 1; i >= 0; i--) {
            ItemStack item = list.get(i);
            if(item.getItem() instanceof RestraintItem ri && ri.dropRestraintWhenRelease(entity,entity,item,part,i)){
                entity.spawnAtLocation(item);
            }
            NeoForge.EVENT_BUS.post(new RestraintEquipEvent.Unequipped(entity, part, i, item));
        }
        cap.clear(part);

        entity.setData(ModAttachments.RESTRAINT_STACK, cap);
    }


    /**
     * 玩家死亡后，重新排列所有部位中未掉落的拘束具
     */
    public static void rebalanceAllRestraintsAfterDeath(LivingEntity entity) {
        if (entity == null) return;
        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            rebalanceRestraintsAfterDeath(entity, part);
        }
    }

    /**
     * 玩家死亡后，重新排列某个部位中未掉落的拘束具
     */
    public static void rebalanceRestraintsAfterDeath(LivingEntity entity, PlayerRestraintPart part) {
        if (entity == null) return;

        List<ItemStack> poppedItems = new ArrayList<>();

        while (true) {
            ItemStack removed = removeRestraint(entity, part);
            if (removed.isEmpty()) {
                break;
            }
            poppedItems.add(removed);
        }

        for (int i = poppedItems.size() - 1; i >= 0; i--) {
            ItemStack stackToInsert = poppedItems.get(i);
            addRestraint(entity, part, stackToInsert);
        }
    }
}