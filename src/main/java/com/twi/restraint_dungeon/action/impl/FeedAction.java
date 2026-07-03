package com.twi.restraint_dungeon.action.impl;

import com.twi.restraint_dungeon.action.type.CarryingAction;
import com.twi.restraint_dungeon.event.mod_event.player_carry.type.CarryHug;
import com.twi.restraint_dungeon.event.mod_event.player_carry.type.CarryShoulder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.getIsStruggling;

public class FeedAction extends CarryingAction {

    @Override
    public String getActionId() {
        return "FEED";
    }

    @Override
    public int getAnimTicks() {
        return 20;
    }

    @Override
    public boolean renderMainHandItem(Player actionPlayer) {
        return isCarrier(actionPlayer);
    }

    @Override
    public @Nullable Component canUse(Player carrier, HitResult result) {
        if(super.canUse(carrier, result) != null){
            return super.canUse(carrier,result);
        }

        if(!(getCurrentCarryType(carrier) instanceof CarryHug || getCurrentCarryType(carrier) instanceof CarryShoulder)){
            return Component.translatable("action." + MODID + ".fail_feed.incorrect_carry_type")
                    .withStyle(ChatFormatting.RED);
        }

        if (!canBeFed(carrier.getMainHandItem())) {
            return Component.translatable("action." + MODID + ".fail_feed.food_not_in_main_hand")
                    .withStyle(ChatFormatting.RED);
        }

        return null;
    }

    @Override
    public void onStart(ServerPlayer carrier, LivingEntity target,HitResult hitResult) {
        playFeedingSound(target, carrier.getMainHandItem(), 0.5F);
    }

    @Override
    public void onTick(ServerPlayer carrier, LivingEntity target,HitResult result, int remaining) {
        // 每 4 tick 播放一次持续进食/饮用音效
        if (remaining % 4 == 0) {
            playFeedingSound(target, carrier.getMainHandItem(), 0.5F);
        }
    }

    @Override
    public void onFinish(ServerPlayer carrier, LivingEntity target,HitResult result) {
        ItemStack foodStack = carrier.getMainHandItem();

        if (canBeFed(foodStack)) {
            ItemStack toConsume = foodStack.copy();
            toConsume.setCount(1);
            ItemStack resultStack = toConsume.finishUsingItem(carrier.level(), target);

            if (!carrier.isCreative()) {
                foodStack.shrink(1);

                if (!ItemStack.matches(toConsume, resultStack)) {
                    if (foodStack.isEmpty()) {
                        carrier.setItemInHand(InteractionHand.MAIN_HAND, resultStack);
                    } else {
                        if (!carrier.getInventory().add(resultStack)) {
                            carrier.drop(resultStack, false);
                        }
                    }
                }
            }
            SoundEvent finishSound = foodStack.getEatingSound();
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    finishSound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private boolean canBeFed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.has(DataComponents.FOOD)) return true;

        UseAnim anim = stack.getUseAnimation();
        return anim == UseAnim.DRINK || anim == UseAnim.EAT;
    }

    private void playFeedingSound(LivingEntity target, ItemStack stack, float volume) {
        SoundEvent sound;
        if (stack.getUseAnimation() == UseAnim.DRINK || isSoup(stack)) {
            sound = stack.getDrinkingSound();
        } else {
            sound = stack.getEatingSound();
        }

        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                sound, SoundSource.PLAYERS, volume, 1.0F);
    }

    private boolean isSoup(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.MUSHROOM_STEW ||
                item == Items.RABBIT_STEW ||
                item == Items.BEETROOT_SOUP ||
                item == Items.SUSPICIOUS_STEW;
    }
}