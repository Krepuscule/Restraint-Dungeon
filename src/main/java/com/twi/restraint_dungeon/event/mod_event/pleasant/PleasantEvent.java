package com.twi.restraint_dungeon.event.mod_event.pleasant;

import com.twi.restraint_dungeon.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.updatePleasantAttributes;

@EventBusSubscriber(modid = MODID)
public class PleasantEvent {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !player.isAlive()) return;

        updatePleasantAttributes(player, getPleasantValue(player));
    }


    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity.level().isClientSide) return;

        Entity.RemovalReason reason = entity.getRemovalReason();

        if (reason == null) return;

        if (reason == Entity.RemovalReason.KILLED ||
                reason == Entity.RemovalReason.DISCARDED ||
                reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {

            UUID uuid = entity.getUUID();

            PleasantValueManager.clearData(uuid);

            PleasantValueManager.clearData(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        handleClimaxEnd(event.getEntity(), event.getEffectInstance());
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        handleClimaxEnd(event.getEntity(), event.getEffectInstance());
    }


    private static void handleClimaxEnd(LivingEntity entity, MobEffectInstance expiredInstance) {
        if (entity == null || entity.level().isClientSide()) return;

        if (expiredInstance != null && expiredInstance.is(ModEffects.CLIMAX)) {
            if (entity.getServer() != null) {
                entity.getServer().execute(() -> {
                    if (entity.isAlive()) {
                        entity.addEffect(new MobEffectInstance(
                                ModEffects.CALM,
                                1200,
                                0,
                                false,
                                false
                        ));
                    }
                });
            }
        }
    }
}