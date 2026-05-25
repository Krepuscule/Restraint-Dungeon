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
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.canBeUnlock;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getPartLastRestraint;

public class RestraintKeyItem extends Item {

    public RestraintKeyItem(Properties properties) {
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
        return performUnlockLogic(stack, player, target);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,
                                                           Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhandStack = (hand == InteractionHand.MAIN_HAND) ? player.getOffhandItem() : player.getMainHandItem();

        if (offhandStack.getItem() instanceof RestraintLockItem lockItem) {
            if (!level.isClientSide) {
                handlePairingFromKey(player, offhandStack, stack, lockItem, this);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        InteractionResult result = performUnlockLogic(stack, player, player);
        return new InteractionResultHolder<>(result, stack);
    }

    private void handlePairingFromKey(Player player, ItemStack lockStack, ItemStack keyStack, RestraintLockItem lockItem, RestraintKeyItem keyItem) {
        if (lockItem.getPairingID(lockStack) != null ) {
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.message.lock.has_been_paired").withStyle(ChatFormatting.RED), true);
        } else if(keyItem.getPairingID(keyStack) != null){
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.message.key.has_been_paired").withStyle(ChatFormatting.RED), true);
        }else {
            UUID newID = UUID.randomUUID();
            lockItem.setPairingID(lockStack, newID);
            keyItem.setPairingID(keyStack, newID);
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.message.lock.pair_success").withStyle(ChatFormatting.AQUA), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private InteractionResult performUnlockLogic(ItemStack stack, Player operator, LivingEntity target) {
        if (operator.level().isClientSide) return InteractionResult.CONSUME;

        PlayerRestraintPart bodyPart = getTargetPart(operator);
        if (bodyPart == null) return InteractionResult.FAIL;

        ItemStack targetRestraint = getPartLastRestraint(target,bodyPart);

        if (canBeUnlock(target,stack, bodyPart,targetRestraint) != null) {
            operator.displayClientMessage(Objects.requireNonNull(canBeUnlock(target,stack, bodyPart,targetRestraint)), true);
        } else {
            if(targetRestraint.getItem() instanceof RestraintItem restraintItem){
                ItemStack lockStack = restraintItem.getLockType(target,targetRestraint);


                // 检查钥匙 ID 是否匹配
                UUID keyID = this.getPairingID(stack);
                UUID lockID = lockStack.get(ModDataComponents.LOCK_PAIRING_ID);

                if (keyID != null && keyID.equals(lockID)) {
                    restraintItem.setLockType(target,targetRestraint, ItemStack.EMPTY);

                    operator.displayClientMessage(Component.translatable("item.restraint_dungeon.message.key.success.pre").withStyle(ChatFormatting.GREEN)
                            .append(target.getName())
                            .append(Component.translatable("item.restraint_dungeon.message.key.success.end")), true);

                    target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 1.0F, 1.2F);

                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.FAIL;

    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        // 复用 LockItem 的渲染逻辑，或调用公共工具类
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
            tooltip.add(Component.translatable("item.restraint_dungeon.key.tooltips.pairing").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(magicSig.toString()).withStyle(magicStyle)));
        } else {
            tooltip.add(Component.translatable("item.restraint_dungeon.key.tooltips.unpairing").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
