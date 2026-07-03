package com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearCutStrugglingState;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class LatexItem extends RestraintItem {

    public static final RestraintDefaults LATEX_DEFAULTS = new RestraintDefaults(
            400,
            20.0,
            0.1,
            0.1,
            0.1
    );

    public LatexItem(Properties properties) {
        super(properties.stacksTo(1), LATEX_DEFAULTS);

        this.setCanEquipPartList(List.of(
                PlayerRestraintPart.restraint_blindfold,
                PlayerRestraintPart.restraint_gag,
                PlayerRestraintPart.restraint_body_bind,
                PlayerRestraintPart.restraint_arms_bind,
                PlayerRestraintPart.restraint_hands_bind,
                PlayerRestraintPart.restraint_legs_bind
        ));


        Map<String, List<String>> connectMap = new HashMap<>();
        connectMap.put(PlayerRestraintPart.restraint_blindfold.toString(), List.of(PlayerRestraintPart.restraint_gag.toString()));
        connectMap.put(PlayerRestraintPart.restraint_gag.toString(), List.of(PlayerRestraintPart.restraint_body_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_body_bind.toString(), List.of(PlayerRestraintPart.restraint_arms_bind.toString(), PlayerRestraintPart.restraint_legs_bind.toString()));
        this.setConnectPartMap(connectMap);

        this.setCanBeLocked(false);
    }

    @Override
    public double onStrengthStruggle(UUID playerUUID, double ItemStrengthIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearCutStrugglingState(player)) {
                return ItemStrengthIndex * 10.0;
            }
        }
        return super.onStrengthStruggle(playerUUID, ItemStrengthIndex);
    }
    @Override
    public double onLooseStruggle(UUID playerUUID,double ItemLooseIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearCutStrugglingState(player)) {
                return ItemLooseIndex * 2.0;
            }
        }
        return super.onLooseStruggle(playerUUID, ItemLooseIndex);
    }
    @Override
    public double onUnlockStruggle(UUID playerUUID,double ItemLockIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearCutStrugglingState(player)) {
                return ItemLockIndex * 2.0;
            }
        }
        return super.onLooseStruggle(playerUUID, ItemLockIndex);
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
    public boolean shouldGagAndBlindfoldRenderOffset(PlayerRestraintPart part){
        return false;
    }
}