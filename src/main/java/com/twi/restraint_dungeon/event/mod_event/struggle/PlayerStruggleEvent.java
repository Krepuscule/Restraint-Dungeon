package com.twi.restraint_dungeon.event.mod_event.struggle;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import com.twi.restraint_dungeon.client.hud.self_bondage_hud.SelfBondageMenu;
import com.twi.restraint_dungeon.client.hud.struggle_hud.PlayerStruggleModeSelectMenu;
import com.twi.restraint_dungeon.client.hud.struggle_hud.StruggleHUDManager;
import com.twi.restraint_dungeon.client.keybind.ModKeyBinds;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.network.payload.player_struggle.InterruptStrugglePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.RestraintMoveManager.isPlayerRestraintMoving;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.isKidnappingActive;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.isReleaseActive;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getTargetPart;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.isChangingPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.*;

@EventBusSubscriber(modid = MODID)
public class PlayerStruggleEvent {

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Post event) {
        if(event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof ServerPlayer player && getIsStruggling(player)) {
            if (event.getOriginalDamage() > 0) {
                PacketDistributor.sendToPlayer(player,new InterruptStrugglePayload());
            }
        }
    }


    /**
     * 开启挣扎方式选择菜单（默认为R键）
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;


        if (ModKeyBinds.STRUGGLE_MODE_SELECT_MENU.consumeClick()) {
            if(!isBeenBindArms(mc.player) && !isBeenBindHands(mc.player)
                    && (mc.player.getMainHandItem().getItem() instanceof RestraintItem
                    || mc.player.getMainHandItem().getItem() instanceof RestraintToolItem)){
                mc.setScreen(new SelfBondageMenu());
            }else if (!getAllRestraint(mc.player).isEmpty()
                    && !isKidnappingActive(mc.player)
                    && !isReleaseActive(mc.player)
                    && !isChangingPosition(mc.player)
                    && !isDoingAction(mc.player)
                    && !isPlayerRestraintMoving(mc.player)){
                mc.setScreen(new PlayerStruggleModeSelectMenu());
            }else{
                if(getAllRestraint(mc.player).isEmpty()){
                    mc.player.displayClientMessage(Component.translatable("hud." + MODID + ".struggle.no_restraint")
                                    .withStyle(ChatFormatting.YELLOW)
                            ,true);
                }else{
                    mc.player.displayClientMessage(Component.translatable("hud." + MODID + ".cant_struggle")
                                    .withStyle(ChatFormatting.YELLOW)
                            ,true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerStruggleTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if((entity instanceof Player player)){
            if (!getIsStruggling(player)) return;

            ItemStack stack = getPlayerStrugglingItem(player);
            PlayerRestraintPart bodyPart = getTargetPart(player);
            int index = getPlayerStrugglingItemIndex(player);
            if(stack.getItem() instanceof RestraintItem restraintItem) {
                if(getPlayerStruggleMode(player) == StruggleMode.STRENGTH){
                    restraintItem.strengthStruggleTick(player,stack,bodyPart,index);
                }else if(getPlayerStruggleMode(player) == StruggleMode.LOOSE){
                    restraintItem.looseStruggleTick(player,stack,bodyPart,index);
                }else if(getPlayerStruggleMode(player) == StruggleMode.UNLOCK){
                    restraintItem.unlockStruggleTick(player,stack,bodyPart,index);
                }
            }
        }
        // TODO: 完成NPC功能后补充
//        else if(entity instanceof BaseNPCEntity npc){
//
//        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onStruggling_MovementInputUpdate(MovementInputUpdateEvent event) {
        if (StruggleHUDManager.isActive()) {
            Input input = event.getInput();

            // 1. 禁用位移输入
            input.forwardImpulse = 0.0f;
            input.leftImpulse = 0.0f;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
        }
    }
}
