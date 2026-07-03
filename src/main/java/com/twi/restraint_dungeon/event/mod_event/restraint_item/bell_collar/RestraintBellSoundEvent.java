package com.twi.restraint_dungeon.event.mod_event.restraint_item.bell_collar;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.client.sound.ModSounds;
import com.twi.restraint_dungeon.item.restraint_item.restraints.BellCollarItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;

@EventBusSubscriber(modid = MODID)
public class RestraintBellSoundEvent {

    @SubscribeEvent
    public static void onEntityFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        float fallDistance = event.getDistance();

        if (fallDistance > 0.75F) {
            boolean hasBell = false;

            for (ItemStack stack : getAllPartRestraint(entity, PlayerRestraintPart.restraint_collar)) {

                if (stack.getItem() instanceof BellCollarItem) {
                    hasBell = true;
                    break;
                }
            }



            if (!hasBell) return;

            ServerLevel level = (ServerLevel) entity.level();
            BlockPos pos = entity.blockPosition();

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.BELL_SWING, SoundSource.PLAYERS, 0.8F, 0.8F);


            level.gameEvent(
                    GameEvent.ENTITY_PLACE,
                    entity.position(), 
                    new GameEvent.Context(entity, level.getBlockState(pos))
            );
        }
    }
}