package com.twi.restraint_dungeon.item.restraint_lock.lock;

import com.mojang.authlib.GameProfile;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintKeyItem;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintLockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;


public class CommonLockItem extends RestraintLockItem {

    public CommonLockItem() {
        super(new Properties());
    }

    public static void setOwnerData(@NotNull ItemStack stack, @NotNull UUID uuid, @NotNull String name) {

        if (stack.getItem() instanceof RestraintLockItem lockItem) {
            lockItem.setPairingID(stack, uuid);
        }

        stack.set(DataComponents.PROFILE, new ResolvableProfile(new GameProfile(uuid, name)));
    }

    @Override
    public boolean shouldReduceStackWhenUse(Player player,ItemStack stack){
        return false;
    }

    @Override
    public boolean shouldDropLockStackWhenUnlock(Player player,ItemStack lockStack,ItemStack keyStack){
        return false;
    }

    @Override
    public String getShowPairingPrefixOnDevice(ItemStack stack){
        if(getPairingID(stack) != null){
            return Component.translatable("item." + MODID + ".personal_common_lock.info_show_on_device").getString();
        }else{
            return null;
        }
    }

    @Override
    public boolean shouldPairingIDUseMagicFont(ItemStack stack){
        return false;
    }

    @Override
    public String getShowPairingInfoOnDevice(ItemStack stack){
        UUID id = getPairingID(stack);
        if (id != null) {
            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
            String ownerName;
            if (resolvableProfile != null) {
                ownerName = resolvableProfile.gameProfile().getName();
            } else {

                ownerName = "Player_" + id.toString().substring(0, 8);
            }
            return ownerName;

        }

        return null;
    }


    @Nullable
    public static UUID getOwnerUUID(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof RestraintLockItem lockItem) {
            return lockItem.getPairingID(stack);
        }
        return null;
    }

    @NotNull
    public static String getOwnerName(@NotNull ItemStack stack) {
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (profile != null) {
            return profile.gameProfile().getName();
        }

        UUID id = getOwnerUUID(stack);
        return id != null ? "Player_" + id.toString().substring(0, 8) : Component.translatable("item." + MODID + ".common_key.no_owner").toString();
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        super.onCraftedBy(stack, level, player);
        if (!level.isClientSide) {
            this.setOwnerData(stack, player.getUUID(),player.getName().getString());
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (this.getPairingID(stack) == null) {
                this.setOwnerData(stack, player.getUUID(),player.getName().getString());

                player.displayClientMessage(Component.translatable("item." + MODID + ".common_key.pair_success").withStyle(ChatFormatting.GREEN), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.5f);

                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        UUID id = getPairingID(stack);
        if (id != null) {
            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
            String ownerName;
            if (resolvableProfile != null) {
                ownerName = resolvableProfile.gameProfile().getName();
            } else {

                ownerName = "Player_" + id.toString().substring(0, 8);
            }

            long time = System.currentTimeMillis() / 50;
            int red = (int) (Math.sin(time * 0.1) * 63 + 192);
            int color = (red << 16) | (100 << 8) | 255;

            Style magicStyle = Style.EMPTY.withColor(TextColor.fromRgb(color));

            tooltip.add(Component.translatable("item." + MODID + ".common_key.current_owner").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(ownerName).withStyle(magicStyle)));
        } else {
            tooltip.add(Component.translatable("item." + MODID + ".common_key.no_owner").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.translatable("item." + MODID + ".common_key.use_to_bound").withStyle(ChatFormatting.DARK_RED));
        }
    }
}