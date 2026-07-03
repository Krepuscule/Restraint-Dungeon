package com.twi.restraint_dungeon.event.system_handler_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import com.twi.restraint_dungeon.event.custom_event.RestraintToolUpdateEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintToolsEquipEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintUpdateEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintEquipEvent;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.LockTick;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils.getAllRestraintTools;

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

    /**
     * 响应装备事件
     */
    @SubscribeEvent
    public static void onRestraintToolsEquipped(RestraintToolsEquipEvent.Equipped event) {
        if (event.getStack().getItem() instanceof RestraintToolItem restraintToolItem) {
            restraintToolItem.onEquip(event.getEntity(), event.getStack(), event.getIndex());
        }
    }

    /**
     * 响应卸下事件
     */
    @SubscribeEvent
    public static void onRestraintToolsUnequipped(RestraintToolsEquipEvent.Unequipped event) {
        if (event.getStack().getItem() instanceof RestraintToolItem restraintToolItem) {
            restraintToolItem.onUnequip(event.getEntity(), event.getStack(), event.getIndex());
        }
    }

    /**
     * RestraintToolItem 每Tick事件
     */
    @SubscribeEvent
    public static void onLivingTick_restraintToolsItem(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity.level().isClientSide) return;


            var list = getAllRestraintTools(entity);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = list.get(i);
                if (stack.getItem() instanceof RestraintToolItem item) {
                    item.onEquipTick(entity, stack, i);
                }
            }

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
        boolean keepInventory = serverLevel.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

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

        var toolList = RestraintToolsUtils.getAllRestraintTools(entity);
        for (int i = toolList.size() - 1; i >= 0; i--) {
            ItemStack toolStack = toolList.get(i);

            if (toolStack.getItem() instanceof RestraintToolItem toolItem) {
                RestraintToolItem.RestraintToolDropRule toolRule = toolItem.getDropRule(entity, source, recentlyHit, toolStack);

                RestraintToolItem.RestraintToolDropRule finalToolRule = toolRule;
                if (toolRule == RestraintToolItem.RestraintToolDropRule.DEFAULT) {
                    finalToolRule = keepInventory ? RestraintToolItem.RestraintToolDropRule.ALWAYS_KEEP : RestraintToolItem.RestraintToolDropRule.ALWAYS_DROP;
                }

                switch (finalToolRule) {
                    case ALWAYS_DROP -> {
                        ItemEntity itemEntity = entity.spawnAtLocation(toolStack.copy());
                        if (itemEntity != null) {
                            itemEntity.setPickUpDelay(40);
                            event.getDrops().add(itemEntity);
                        }
                        RestraintToolsUtils.removeRestraintTool(entity, i);
                    }
                    case ALWAYS_KEEP -> {

                    }
                    case DESTROY -> {
                        RestraintToolsUtils.removeRestraintTool(entity, i);
                    }
                }
            }
        }
    }

    private static final Map<UUID, Map<PlayerRestraintPart, List<ItemStack>>> PREVIOUS_RESTRAINTS = new HashMap<>();
    private static final Map<UUID, List<ItemStack>> PREVIOUS_TOOLS = new HashMap<>();


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

                    RestraintUpdateEvent changeEvent = new RestraintUpdateEvent(
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

        List<ItemStack> currentTools = RestraintToolsUtils.getAllRestraintTools(living);
        List<ItemStack> previousTools = PREVIOUS_TOOLS.get(uuid);
        boolean isNewEntityTool = (previousTools == null);

        if (previousTools == null) {
            previousTools = new ArrayList<>();
            for (int i = 0; i < currentTools.size(); i++) {
                previousTools.add(ItemStack.EMPTY);
            }
            if (!currentTools.isEmpty()) {
                PREVIOUS_TOOLS.put(uuid, previousTools);
            }
        }

        boolean toolsChanged = false;
        int maxToolSlots = Math.max(currentTools.size(), previousTools.size());

        for (int i = 0; i < maxToolSlots; i++) {
            ItemStack currentToolStack = i < currentTools.size() ? currentTools.get(i) : ItemStack.EMPTY;
            ItemStack previousToolStack = i < previousTools.size() ? previousTools.get(i) : ItemStack.EMPTY;

            if (!ItemStack.matches(currentToolStack, previousToolStack)) {
                RestraintToolUpdateEvent toolChangeEvent = new RestraintToolUpdateEvent(
                        living, i, previousToolStack.copy(), currentToolStack
                );
                NeoForge.EVENT_BUS.post(toolChangeEvent);
                toolsChanged = true;
            }
        }

        if (toolsChanged || currentTools.size() != previousTools.size()) {
            List<ItemStack> nextToolSnapshot = new ArrayList<>();
            for (ItemStack current : currentTools) {
                nextToolSnapshot.add(current.copy());
            }
            PREVIOUS_TOOLS.put(uuid, nextToolSnapshot);
        }

        if (!isNewEntityTool && currentTools.isEmpty()) {
            PREVIOUS_TOOLS.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {

            PREVIOUS_RESTRAINTS.remove(entity.getUUID());
            PREVIOUS_TOOLS.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {

        if (event.getEntity().level().isClientSide) return;

        Entity teleportingEntity = event.getEntity();

        if(!(teleportingEntity instanceof LivingEntity living)) return;

        AABB searchBox = teleportingEntity.getBoundingBox().inflate(0.5);
        List<SeatEntity> nearbySeats = teleportingEntity.level().getEntitiesOfClass(SeatEntity.class, searchBox);

        for (SeatEntity seat : nearbySeats) {
            BlockPos sourcePos = seat.getSourcePos();
            if (sourcePos != null) {
                BlockState state = living.level().getBlockState(sourcePos);

                if (state.getBlock() instanceof RestraintDevice device) {

                    device.forceDismount(living.level(), sourcePos, living);

                    seat.discard();

                    break;
                }
            }
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