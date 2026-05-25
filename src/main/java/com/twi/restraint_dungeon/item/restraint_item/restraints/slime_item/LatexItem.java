package com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindArms;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

public class LatexItem extends RestraintItem {

    public static final RestraintDefaults LATEX_DEFAULTS = new RestraintDefaults(
            400,
            20.0,
            0.25,
            0.25,
            0.1
    );

    public LatexItem(Properties properties) {
        super(properties.stacksTo(1), LATEX_DEFAULTS);

        this.setCanEquipPartList(List.of(
                PlayerRestraintPart.restraint_blindfold.toString(),
                PlayerRestraintPart.restraint_gag.toString(),
                PlayerRestraintPart.restraint_body_bind.toString(),
                PlayerRestraintPart.restraint_arms_bind.toString(),
                PlayerRestraintPart.restraint_hands_bind.toString(),
                PlayerRestraintPart.restraint_legs_bind.toString()
        ));

        // 默认连接逻辑
        Map<String, List<String>> connectMap = new HashMap<>();
        connectMap.put(PlayerRestraintPart.restraint_blindfold.toString(), List.of(PlayerRestraintPart.restraint_gag.toString()));
        connectMap.put(PlayerRestraintPart.restraint_gag.toString(), List.of(PlayerRestraintPart.restraint_body_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_body_bind.toString(), List.of(PlayerRestraintPart.restraint_arms_bind.toString(), PlayerRestraintPart.restraint_legs_bind.toString()));
        this.setConnectPartMap(connectMap);

        this.setCanBeLocked(false);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.latex").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void renderBlindfoldOverlay(UUID playerUUID, GuiGraphics guiGraphics, ItemStack stack) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/models/blindfold_overlay/slime/" + itemName + ".png");

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
    }

    @Override
    public Component canUseKidnap(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        return Component.translatable("item.restraint_dungeon.latex.cant_use_kidnap").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Component canBeReleaseBySelf(LivingEntity entity, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        if (entity instanceof Player player) {
            if (!isBeenBindArms(player) && !isBeenBindHands(player)) {
                ItemStack mainHand = player.getMainHandItem();
                if (mainHand.getItem() instanceof SwordItem || mainHand.getItem() instanceof ShearsItem) {
                    return null;
                }
            }
        }
        return Component.translatable("item.restraint_dungeon.latex.cant_release").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Component canBeReleased(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        return Component.translatable("item.restraint_dungeon.latex.cant_release").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean shouldGagAndBlindfoldRenderOffset(){
        return false;
    }
}