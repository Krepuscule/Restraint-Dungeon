package com.twi.restraint_dungeon.event.mod_event.restraint_item.slime;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.LatexItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

@EventBusSubscriber(modid = MODID)
public class LatexEvent {
    @SubscribeEvent
    public static void onSwordCutLatex(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity targetEntity = event.getTarget();
        Player actionPlayer = event.getEntity();
        ItemStack cutterStack = event.getItemStack();
        Level level = event.getLevel();

        if (level.isClientSide) return;

        if ((targetEntity instanceof LivingEntity target)) {
            PlayerRestraintPart bodyPart = getEntityTargetPart(actionPlayer);

            boolean success = cutLatexByPlayer(actionPlayer,target, bodyPart, cutterStack,event);
            if (success) {
                if (cutterStack.isDamageableItem() && !actionPlayer.isCreative()) {
                    cutterStack.hurtAndBreak(1, (ServerLevel) level, null, item -> cutterStack.setCount(0));
                }
            }
        }
    }

    private static boolean cutLatexByPlayer(Player actionPlayer,
                                            LivingEntity target,
                                            PlayerRestraintPart bodyPart,
                                            ItemStack cutterStack,
                                            PlayerInteractEvent.EntityInteractSpecific event) {

        if(getAllPartRestraint(target,bodyPart).isEmpty()) return false;
        if(isBeenBindArms(actionPlayer) || isBeenBindHands(actionPlayer)) return false;

        List<ItemStack> stacks = getAllPartRestraint(target,bodyPart);

        if(stacks.isEmpty()
                || !(cutterStack.getItem() instanceof SwordItem)
                || !(getPartLastRestraint(target,bodyPart).getItem() instanceof LatexItem)) return false;

       target.level().playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.PLAYERS,
                0.8F,
                1.0F
        );

        actionPlayer.swing(event.getHand(), true);

        ItemStack latex = getPartLastRestraint(target,bodyPart).copy();
        removeRestraintItem(target, bodyPart);

        if(!actionPlayer.getInventory().add(latex)){
            actionPlayer.drop(latex, false);
        }
        return true;
    }
}
