package com.twi.restraint_dungeon.item.restraint_tool.tools.vibrator;

import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager.performUpdate;
import static com.twi.restraint_dungeon.item.ModDataComponents.*;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;

public class VibratorItem extends RestraintToolItem {

    public VibratorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVibeLevel(ItemStack stack) {
        return stack.getOrDefault(VIBE_LEVEL, 0);
    }

    public void setVibeLevel(ItemStack stack, int level, LivingEntity entity) {
        int clampedLevel = Math.clamp(level, 0, 3);

         stack.set(VIBE_LEVEL, clampedLevel);


        if (clampedLevel == 0) {
            stack.remove(ACTIVATE_TIME.get());
        } else {
            if (!stack.has(ACTIVATE_TIME.get())) {
                stack.set(ACTIVATE_TIME.get(), entity.level().getGameTime());
            }
        }
    }

    public boolean isActivated(ItemStack stack) {
        return stack.getOrDefault(VIBE_ACTIVATE.get(), false);
    }

    public void setActivated(ItemStack stack, boolean activate, LivingEntity entity) {
        stack.set(VIBE_ACTIVATE.get(), activate);

        if (!activate || getVibeLevel(stack) == 0) {
            stack.remove(ACTIVATE_TIME.get());
        } else {
            if (!stack.has(ACTIVATE_TIME.get())) {
                stack.set(ACTIVATE_TIME.get(), entity.level().getGameTime());
            }
        }
    }


    public int getIntervalByLevel(int vibeLevel) {
        return switch (vibeLevel) {
            case 1 -> 600;  // 30s
            case 2 -> 300;  // 15s
            case 3 -> 100;  // 5s
            default -> 0;
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Screen getConfigurationScreen(LivingEntity entity, ItemStack stack, int index) {
        if (entity.level().isClientSide()) {
            return new VibratorScreen(
                    Component.translatable("gui." + MODID + ".vibrator.config"),
                    entity.getId(),
                    index
            );
        }
        return null;
    }

    @Override
    public int getMaxUsage(LivingEntity entity,ItemStack stack){
        return 2;
    }

    @Override
    public void onEquipTick(LivingEntity entity, ItemStack stack, int index) {
        if (entity.level().isClientSide()) return;

        int currentLevel = getVibeLevel(stack);
        boolean activated = isActivated(stack);

        if (currentLevel == 0 || !activated) {
            if (stack.has(ACTIVATE_TIME.get())) {
                stack.remove(ACTIVATE_TIME.get());
            }
            return;
        }

        if (!stack.has(ACTIVATE_TIME.get())) {
            stack.set(ACTIVATE_TIME.get(), entity.level().getGameTime());
            return;
        }

        long startTime = stack.getOrDefault(ACTIVATE_TIME.get(), 0L);
        long currentTime = entity.level().getGameTime();
        int targetInterval = getIntervalByLevel(currentLevel);

        if (currentTime - startTime >= targetInterval) {
            performUpdate(entity, Math.max(getThrillLevel(entity), 5));
            stack.set(ACTIVATE_TIME.get(), currentTime);
        }
    }

    @Override
    public void onEquip(LivingEntity entity, ItemStack stack, int index){
        super.onEquip(entity, stack, index);
        performUpdate(entity, 10);
    }

    @Override
    public void onUnequip(LivingEntity entity, ItemStack stack, int index) {
        super.onUnequip(entity, stack, index);
        stack.remove(ACTIVATE_TIME.get());
        stack.set(VIBE_ACTIVATE.get(), false);
        performUpdate(entity, 10);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int currentLevel = getVibeLevel(stack);

        ChatFormatting color = switch (currentLevel) {
            case 1 -> ChatFormatting.GREEN;
            case 2 -> ChatFormatting.YELLOW;
            case 3 -> ChatFormatting.RED;
            default -> ChatFormatting.GRAY;
        };

        if(isActivated(stack)){
            tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.vibrator_activate").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.vibrator.prefix").withStyle(ChatFormatting.GOLD)
                    .append(String.valueOf(currentLevel))
                    .withStyle(color));
        }else{
            tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.vibrator_deactivate").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getTextureResourceLocation(LivingEntity entity, ItemStack stack,int index, boolean isSlim) {

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemName = itemKey.getPath();

        return ResourceLocation.fromNamespaceAndPath(MODID,
                "textures/models/restraint_tools/" + itemName + "/" + itemName + "_" + index + ".png");

    }


}
