package com.twi.restraint_dungeon.event.mod_event.self_bondage;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.utils.mod_utils.self_bondage.SelfBondageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class SelfBondageServerEvent {

    private static final Map<UUID, Long> START_TIME_MAP = new HashMap<>();
    private static final Map<UUID, PlayerRestraintPart> CURRENT_PART_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof LivingEntity entity)) return;

        if (!SelfBondageUtils.isSelfBondaging(entity)) return;

        UUID uuid = entity.getUUID();
        ItemStack stack = entity.getMainHandItem();
        PlayerRestraintPart part = CURRENT_PART_MAP.get(uuid);

        if (part == null || !SelfBondageUtils.entityCanBindSelf(entity, stack, part).canBind()) {
            if(entity instanceof Player player && !SelfBondageUtils.entityCanBindSelf(entity, stack, part).canBind()){
                player.displayClientMessage(SelfBondageUtils.entityCanBindSelf(entity, stack, part).message(),true);
            }
            stopSelfBondage(entity);
            return;
        }

        float totalTicks = 0.0f;
        if (stack.getItem() instanceof RestraintItem item) {
            totalTicks = item.getKidnapTime(entity, entity, stack, part) / 50.0f;
        } else if (stack.getItem() instanceof RestraintToolItem toolItem) {
            // 3秒
            totalTicks = 60.0f;
        }

        if (totalTicks <= 0) {
            stopSelfBondage(entity);
            return;
        }

        long startTime = START_TIME_MAP.getOrDefault(uuid, entity.level().getGameTime());
        long elapsed = entity.level().getGameTime() - startTime;
        float progress = Math.min(100f, ((float) elapsed / totalTicks) * 100f);

        SelfBondageUtils.updateData(entity, true, progress);

        if (progress >= 100f) {
            SelfBondageUtils.executeSelfBind(entity, stack, part);

            START_TIME_MAP.remove(uuid);
            CURRENT_PART_MAP.remove(uuid);
        }
    }

    public static void startSelfBondage(LivingEntity entity, PlayerRestraintPart part) {
        ItemStack stack = entity.getMainHandItem();
        var result = SelfBondageUtils.entityCanBindSelf(entity, stack, part);

        if (!result.canBind()) {
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(result.message(), true);
            }
            return;
        }

        UUID uuid = entity.getUUID();
        START_TIME_MAP.put(uuid, entity.level().getGameTime());
        CURRENT_PART_MAP.put(uuid, part);

        SelfBondageUtils.updateData(entity, true, 0.0f);
    }

    public static void stopSelfBondage(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        START_TIME_MAP.remove(uuid);
        CURRENT_PART_MAP.remove(uuid);
        SelfBondageUtils.clearData(entity);
    }



    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handlePlayerInteraction(event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handlePlayerInteraction(event.getEntity());
    }


    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handlePlayerInteraction(event.getEntity());
    }


    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handlePlayerInteraction(event.getEntity());
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        handlePlayerInteraction(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (SelfBondageUtils.isSelfBondaging(player)) {
            stopSelfBondage(player);
        }
    }

    private static void handlePlayerInteraction(Player player) {
        if (player != null && !player.level().isClientSide() && SelfBondageUtils.isSelfBondaging(player)) {
            stopSelfBondage(player);
        }
    }


    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        if (SelfBondageUtils.isSelfBondaging(entity) && event.getOriginalDamage() > 0) {
            stopSelfBondage(entity);
        }
    }
}