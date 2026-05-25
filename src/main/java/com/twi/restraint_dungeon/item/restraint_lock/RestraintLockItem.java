package com.twi.restraint_dungeon.item.restraint_lock;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.ModDataComponents;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getTargetPart;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.canBeLock;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getPartLastRestraint;

public class RestraintLockItem extends Item {

    public RestraintLockItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Nullable
    public UUID getPairingID(ItemStack stack) {
        return stack.get(ModDataComponents.LOCK_PAIRING_ID);
    }

    public void setPairingID(ItemStack stack, UUID id) {
        stack.set(ModDataComponents.LOCK_PAIRING_ID, id);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack,
                                                           @NotNull Player player,
                                                           @NotNull LivingEntity target,
                                                           @NotNull InteractionHand hand) {
        return performLockLogic(stack, player, target);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,
                                                           Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhandStack = (hand == InteractionHand.MAIN_HAND) ? player.getOffhandItem() : player.getMainHandItem();

        // 检查另一只手是否是钥匙进行配对
        if (offhandStack.getItem() instanceof RestraintKeyItem keyItem) {
            if (!level.isClientSide) {
                handlePairing(player, stack, offhandStack, (RestraintLockItem) stack.getItem(), keyItem);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    private void handlePairing(Player player, ItemStack lockStack, ItemStack keyStack, RestraintLockItem lockItem, RestraintKeyItem keyItem) {
        if (lockItem.getPairingID(lockStack) != null) {
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.message.lock.has_been_paired").withStyle(ChatFormatting.RED), true);
            return;
        } else if (keyItem.getPairingID(keyStack) != null) {
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.message.key.has_been_paired").withStyle(ChatFormatting.RED), true);
            return;
        }else{
            UUID newID = UUID.randomUUID();
            lockItem.setPairingID(lockStack, newID);
            keyItem.setPairingID(keyStack, newID);

            player.displayClientMessage(Component.translatable("item.restraint_dungeon.message.lock.pair_success").withStyle(ChatFormatting.AQUA), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private InteractionResult performLockLogic(ItemStack stack, Player operator, LivingEntity target) {
        if (operator.level().isClientSide) return InteractionResult.CONSUME;

        UUID lockID = getPairingID(stack);
        if (lockID == null) {
            operator.displayClientMessage(Component.translatable("item.restraint_dungeon.message.lock.no_pair").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        PlayerRestraintPart bodyPart = getTargetPart(operator);
        if (bodyPart == null) return InteractionResult.FAIL;

        ItemStack targetRestraint = getPartLastRestraint(target,bodyPart);

        if (canBeLock(target,bodyPart,targetRestraint) != null) {
            operator.displayClientMessage(Objects.requireNonNull(canBeLock(target, bodyPart, targetRestraint)), true);
        }else{
            if(targetRestraint.getItem() instanceof RestraintItem restraintItem){
                restraintItem.setLockType(target,targetRestraint, stack);
                stack.shrink(1);


                Component targetName = (operator == target) ? Component.translatable("item.restraint_dungeon.message.lock.target_self") : target.getName();
                operator.displayClientMessage(Component.translatable("item.restraint_dungeon.message.lock.success.pre").withStyle(ChatFormatting.YELLOW)
                        .append(targetName)
                        .append(Component.translatable("item.restraint_dungeon.message.lock.success.end")), true);
                target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 1.0F, 1.2F);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        UUID id = getPairingID(stack);
        if (id != null) {
            String rawSig = id.toString().substring(0, 8).toUpperCase();
            StringBuilder magicSig = new StringBuilder();
            for (char c : rawSig.toCharArray()) {
                if (Character.isDigit(c)) magicSig.append((char) ('G' + (c - '0')));
                else magicSig.append(c);
            }

            long time = System.currentTimeMillis() / 50;
            int red = (int) (Math.sin(time * 0.1) * 63 + 192);
            int color = (red << 16) | (100 << 8) | 255;

            Style magicStyle = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt")).withColor(TextColor.fromRgb(color));
            tooltip.add(Component.translatable("item.restraint_dungeon.lock.tooltips.pairing").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(magicSig.toString()).withStyle(magicStyle)));
        } else {
            tooltip.add(Component.translatable("item.restraint_dungeon.lock.tooltips.unpairing").withStyle(ChatFormatting.DARK_RED));
        }
    }

    public void onLockTick(){

    }

    public double onStrengthStruggle(Player player, RestraintLockItem lock, double itemStrengthIndex) {
        return itemStrengthIndex / 2.0;
    }

    public double onLooseStruggle(Player player, RestraintLockItem lock, double itemLooseIndex) {
        return itemLooseIndex / 2.0;
    }

    public double onUnlockStruggle(Player player, RestraintLockItem lock, double itemLockIndex) {
        return 0;
    }
}
