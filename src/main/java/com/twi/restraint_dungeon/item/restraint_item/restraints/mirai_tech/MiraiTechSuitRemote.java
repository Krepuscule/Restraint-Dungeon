package com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech;

import com.twi.restraint_dungeon.item.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class MiraiTechSuitRemote extends Item {
    public MiraiTechSuitRemote(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    public UUID getPairingID(ItemStack stack) {
        return stack.get(ModDataComponents.RESTRAINT_PAIRING_ID.get());
    }

    public void setPairingID(ItemStack stack, UUID id) {
        stack.set(ModDataComponents.RESTRAINT_PAIRING_ID.get(), id);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        UUID pairID = getPairingID(stack);

        if (pairID == null) {
            ItemStack offhand = (hand == InteractionHand.MAIN_HAND) ? player.getOffhandItem() : player.getMainHandItem();
            if (offhand.getItem() instanceof MiraiTechSuitItem suitItem) {
                if (!level.isClientSide) {
                    handlePairing(player, stack, offhand, suitItem);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        } else {
            if (level.isClientSide) {
                openGUI(stack, pairID);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    private void handlePairing(Player player, ItemStack controller, ItemStack suitStack, MiraiTechSuitItem suitItem) {
        if (suitItem.getPairingID(suitStack) != null) {
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.mirai_tech_suit.pairing_failed")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        } else if (this.getPairingID(controller) != null) {
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.mirai_tech_suit_remote.pairing_failed")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        UUID newID = UUID.randomUUID();
        this.setPairingID(controller, newID);
        suitItem.setPairingID(suitStack, newID);

        player.displayClientMessage(Component.translatable("item.restraint_dungeon.mirai_tech_suit.pairing_success")
                .withStyle(ChatFormatting.GREEN), true);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    private void openGUI(ItemStack stack, UUID uuid) {
        Minecraft.getInstance().setScreen(new MiraiTechRemoteGUI(stack, uuid));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {

        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.mirai_tech_suit_remote").withStyle(ChatFormatting.GRAY));

        UUID id = getPairingID(stack);
        if (id != null) {
            String rawSig = id.toString().substring(0, 8).toUpperCase();
            StringBuilder magicSig = new StringBuilder();
            for (char c : rawSig.toCharArray()) {
                if (Character.isDigit(c)) magicSig.append((char) ('G' + (c - '0')));
                else if (c != '-') magicSig.append(c);
            }

            long time = (FMLEnvironment.dist == Dist.CLIENT) ? System.currentTimeMillis() / 50 : 0;
            int wave = (int) (Math.sin(time * 0.1) * 52 + 203);
            int r = 80;
            int g = wave;
            int b = 255;
            int color = (r << 16) | (g << 8) | b;

            Style magicStyle = Style.EMPTY
                    .withFont(ResourceLocation.withDefaultNamespace("alt"))
                    .withColor(TextColor.fromRgb(color));

            tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.mirai_tech_suit_remote.pairing")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(magicSig)).withStyle(magicStyle)));
        } else {
            tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.mirai_tech_suit_remote.unpairing")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }
}
