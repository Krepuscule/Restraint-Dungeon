package com.twi.restraint_dungeon.event.mod_event.release;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getEntityTargetPart;

@EventBusSubscriber(modid = MODID)
public class PlayerReleaseServerEvent {

    private static final Map<UUID, Long> START_TIME = new HashMap<>();
    private static final Map<UUID, ItemStack> INITIAL_STACK = new HashMap<>();
    private static final Map<UUID, Integer> INITIAL_INDEX = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        if (!ReleaseUtils.isReleaseActive(sp) || !ReleaseUtils.isReleaser(sp)) return;

        UUID targetUUID = ReleaseUtils.getPartnerUUID(sp);
        LivingEntity target = sp.serverLevel().getEntity(targetUUID) instanceof LivingEntity le ? le : null;

        if (target == null) {
            stop(sp);
            return;
        }

        if(getAllPartRestraint(target,getEntityTargetPart(sp)).isEmpty()){
            return;
        }
        var result = ReleaseUtils.targetCanBeRelease(target, sp);

        boolean itemInvalid = false;
        PlayerRestraintPart part = getEntityTargetPart(sp);
        List<ItemStack> currentRestraints = getAllPartRestraint(target, part);
        int expectedIndex = INITIAL_INDEX.getOrDefault(sp.getUUID(), -1);

        if (expectedIndex < 0 || expectedIndex >= currentRestraints.size()) {
            itemInvalid = true;
        } else {
            ItemStack currentStack = currentRestraints.get(expectedIndex);
            if (!ItemStack.isSameItem(currentStack, INITIAL_STACK.getOrDefault(sp.getUUID(), ItemStack.EMPTY))) {
                itemInvalid = true;
            }
        }

        if (!result.canRelease() || itemInvalid) {
            Component msg = itemInvalid ? Component.translatable("event.restraint_dungeon.release.target_item_lost").withStyle(ChatFormatting.RED) : result.messageKey();
            sp.displayClientMessage(msg, true);
            stop(sp);
            return;
        }

        // 3. 计算进度
        ItemStack stack = INITIAL_STACK.get(sp.getUUID());
        if (stack.getItem() instanceof RestraintItem ri) {
            long elapsed = sp.level().getGameTime() - START_TIME.getOrDefault(sp.getUUID(), sp.level().getGameTime());
            float totalTicks = ri.getReleaseTime(sp, target, stack, part) / 50.0f;
            float progress = Math.min(100f, (elapsed / totalTicks) * 100f);

            if (progress >= 100f) {
                ReleaseUtils.executeRelease(sp, target);
                stop(sp);
            } else {
                ReleaseUtils.updateProgress(sp, progress);
                ReleaseUtils.updateProgress(target, progress);
            }
        }
    }

    public static void start(ServerPlayer sp, LivingEntity target) {
        PlayerRestraintPart part = getEntityTargetPart(sp);

        if(getAllPartRestraint(target,getEntityTargetPart(sp)).isEmpty()){
            return;
        }
        var result = ReleaseUtils.targetCanBeRelease(target, sp);
        if (!result.canRelease()) {
            sp.displayClientMessage(result.messageKey(), true);
            return;
        }

        List<ItemStack> restraints = getAllPartRestraint(target, part);
        int index = restraints.size() - 1;
        ItemStack stack = restraints.get(index).copy();
        ItemStack tool = sp.getMainHandItem();

        START_TIME.put(sp.getUUID(), sp.level().getGameTime());
        INITIAL_STACK.put(sp.getUUID(), stack);
        INITIAL_INDEX.put(sp.getUUID(), index);

        ReleaseUtils.updateReleaseData(sp, true, 0, target.getUUID(), stack, part,tool);

        ReleaseUtils.updateReleaseData(target, false, 0, sp.getUUID(), stack, part,tool);

    }

    public static void stop(ServerPlayer sp) {
        UUID targetUUID = ReleaseUtils.getPartnerUUID(sp);
        Entity target = sp.serverLevel().getEntity(targetUUID);

        START_TIME.remove(sp.getUUID());
        INITIAL_STACK.remove(sp.getUUID());
        INITIAL_INDEX.remove(sp.getUUID());

        ReleaseUtils.clearReleaseData(sp);
        if (target instanceof LivingEntity le) {
            ReleaseUtils.clearReleaseData(le);
        }
    }
}