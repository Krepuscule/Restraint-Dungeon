package com.twi.restraint_dungeon.event.system_handler_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.event.custom_event.RestraintChangeEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintEquipEvent;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.LockTick;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;

@EventBusSubscriber(modid = MODID)
public class RestraintItemSystemHandler {

    /**
     * 响应装备事件
     */
    @SubscribeEvent
    public static void onRestraintEquipped(RestraintEquipEvent.Equipped event) {
        if (event.getStack().getItem() instanceof RestraintItem restraintItem) {
            restraintItem.onEquip(event.getEntity(), event.getStack(), event.getPart(), event.getIndex());
        }
    }

    /**
     * 响应卸下事件
     */
    @SubscribeEvent
    public static void onRestraintUnequipped(RestraintEquipEvent.Unequipped event) {
        if (event.getStack().getItem() instanceof RestraintItem restraintItem) {
            restraintItem.onUnequip(event.getEntity(), event.getStack(), event.getPart(), event.getIndex());
        }
    }

    /**
     * RestraintItem 每Tick事件
     */
    @SubscribeEvent
    public static void onLivingTick_restraintItem(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity.level().isClientSide) return;

            for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
                var list = RestraintStackUtils.getAllRestraintsByPart(entity, part);
                for (int i = 0; i < list.size(); i++) {
                    ItemStack stack = list.get(i);
                    if (stack.getItem() instanceof RestraintItem item) {
                        item.onRestraintTick(entity, stack, part, i);
                    }
                }
            }
        }
    }

    /**
     * RestraintLockItem 每Tick事件
     */
    @SubscribeEvent
    public static void onLivingTick_restraintLockItem(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity.level().isClientSide) return;

            LockTick(entity);
        }
    }



    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        DamageSource source = event.getSource();
        boolean recentlyHit = event.isRecentlyHit();

        // 获取当前世界是否开启了死亡不掉落
        boolean keepInventory = serverLevel.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY);

        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            var list = RestraintStackUtils.getAllRestraintsByPart(entity, part);

            for (int i = list.size() - 1; i >= 0; i--) {
                ItemStack stack = list.get(i);

                if (stack.getItem() instanceof RestraintItem item) {
                    RestraintItem.RestraintDropRule rule = item.getDropRule(entity, source, recentlyHit, stack);

                    RestraintItem.RestraintDropRule finalRule = rule;
                    if (rule == RestraintItem.RestraintDropRule.DEFAULT) {
                        finalRule = keepInventory ? RestraintItem.RestraintDropRule.ALWAYS_KEEP : RestraintItem.RestraintDropRule.ALWAYS_DROP;
                    }

                    switch (finalRule) {
                        case ALWAYS_DROP -> {
                            ItemEntity itemEntity = entity.spawnAtLocation(stack.copy());
                            if (itemEntity != null) {
                                itemEntity.setPickUpDelay(40);
                                event.getDrops().add(itemEntity);
                            }
                            RestraintStackUtils.removeRestraint(entity, part, i);
                        }

                        case ALWAYS_KEEP -> {
                        }

                        case DESTROY -> {
                            RestraintStackUtils.removeRestraint(entity, part, i);
                        }
                    }
                }
            }
        }
    }

    private static final Map<UUID, Map<PlayerRestraintPart, List<ItemStack>>> PREVIOUS_RESTRAINTS = new HashMap<>();


    @SubscribeEvent
    public static void onLivingTickPost(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        if(!(entity instanceof LivingEntity living)) return;

        if (entity.level().isClientSide()) return;

        UUID uuid = entity.getUUID();

        Map<PlayerRestraintPart, List<ItemStack>> entityRestraintsItemMap = PREVIOUS_RESTRAINTS.get(uuid);
        boolean isNewEntity = (entityRestraintsItemMap == null);

        if (isNewEntity) {
            entityRestraintsItemMap = new EnumMap<>(PlayerRestraintPart.class);
        }

        boolean entityHasAnyRestraint = false;

        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {

            List<ItemStack> currentRestraints = getAllPartRestraint(living, part);

            if (!currentRestraints.isEmpty()) {
                entityHasAnyRestraint = true;
            }

            List<ItemStack> previousRestraints = entityRestraintsItemMap.get(part);

            if (previousRestraints == null) {
                previousRestraints = new ArrayList<>();
                for (int i = 0; i < currentRestraints.size(); i++) {
                    previousRestraints.add(ItemStack.EMPTY);
                }
                entityRestraintsItemMap.put(part, previousRestraints);
            }

            boolean partChanged = false;
            int maxSlots = Math.max(currentRestraints.size(), previousRestraints.size());

            for (int i = 0; i < maxSlots; i++) {
                ItemStack currentStack = i < currentRestraints.size() ? currentRestraints.get(i) : ItemStack.EMPTY;
                ItemStack previousStack = i < previousRestraints.size() ? previousRestraints.get(i) : ItemStack.EMPTY;

                if (!ItemStack.matches(currentStack, previousStack)) {

                    RestraintChangeEvent changeEvent = new RestraintChangeEvent(
                            living,
                            part,
                            i,
                            previousStack.copy(),
                            currentStack
                    );
                    NeoForge.EVENT_BUS.post(changeEvent);

                    partChanged = true;
                }
            }

            if (partChanged || currentRestraints.size() != previousRestraints.size()) {
                List<ItemStack> nextPartSnapshot = new ArrayList<>();
                for (ItemStack current : currentRestraints) {
                    nextPartSnapshot.add(current.copy());
                }
                entityRestraintsItemMap.put(part, nextPartSnapshot);
            }
        }
        if (isNewEntity && entityHasAnyRestraint) {
            PREVIOUS_RESTRAINTS.put(uuid, entityRestraintsItemMap);
        }
        else if (!isNewEntity && !entityHasAnyRestraint) {
            PREVIOUS_RESTRAINTS.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {

            PREVIOUS_RESTRAINTS.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {

            PREVIOUS_RESTRAINTS.remove(player.getUUID());
        }
    }
}