package com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.DataComponentsUtils;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public class MerchantSlimeItem extends RestraintItem {

    public static final RestraintDefaults MERCHANT_SLIME_DEFAULTS = new RestraintDefaults(
            200,    // maxResistance
            50.0,   // thrillValue
            0.5,    // strengthIndex
            0.5,    // looseIndex
            0.1     // lockIndex
    );

    public MerchantSlimeItem(Properties properties) {
        super(properties.stacksTo(1), MERCHANT_SLIME_DEFAULTS);

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
        connectMap.put(PlayerRestraintPart.restraint_gag.toString(), List.of(PlayerRestraintPart.restraint_body_bind.toString(), PlayerRestraintPart.restraint_blindfold.toString()));
        connectMap.put(PlayerRestraintPart.restraint_body_bind.toString(), List.of(PlayerRestraintPart.restraint_arms_bind.toString(), PlayerRestraintPart.restraint_gag.toString(), PlayerRestraintPart.restraint_legs_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_arms_bind.toString(), List.of(PlayerRestraintPart.restraint_hands_bind.toString(), PlayerRestraintPart.restraint_body_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_hands_bind.toString(), List.of(PlayerRestraintPart.restraint_arms_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_legs_bind.toString(), List.of(PlayerRestraintPart.restraint_body_bind.toString()));
        this.setConnectPartMap(connectMap);

        this.setCanBeLocked(false);
    }

    @Override
    public RestraintDropRule getDropRule(LivingEntity entity, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return RestraintDropRule.DESTROY;

    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.merchant_slime").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onEquip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (!entity.level().isClientSide) {
            DataComponentsUtils.updateEquipTime(entity,stack,entity.level().getGameTime());
            DataComponentsUtils.updateActivateTime(entity,stack,entity.level().getGameTime());
        }
    }

    @Override
    public void onUnequip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (!entity.level().isClientSide) {
           DataComponentsUtils.removeEquipTime(entity,stack);
           DataComponentsUtils.removeActivateTime(entity, stack);
        }
    }

    @Override
    public void onRestraintTick(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (entity == null || entity.level().isClientSide) return;

        long currentTime = entity.level().getGameTime();
        long equipTime = DataComponentsUtils.getEquipTime(stack);
        if(!entity.isInWaterRainOrBubble()){
            DataComponentsUtils.updateActivateTime(entity,stack,DataComponentsUtils.getActivateTime(stack) + 1);
        }

        long diff = currentTime - equipTime;
        long activateTime = DataComponentsUtils.getActivateTime(stack);

        if (activateTime > 0 && activateTime % slimeExpandTime(entity,stack,part,index ) == 0 && !entity.isInWaterRainOrBubble()) {
            handleSlimeExpansion(entity,part);
        }
        if (diff > 0 && diff % growToLatexTime(entity,stack,part,index) == 0) {
            boolean success = replaceRestraintItem(entity, part, new ItemStack(ModRestraintItems.LATEX.get()), index);
            if (success && entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("item.restraint_dungeon.slime.grow_to_latex").withStyle(ChatFormatting.DARK_RED), true);
            }
        }
    }

    private void handleSlimeExpansion(LivingEntity entity,PlayerRestraintPart part) {
        // 增殖优先级逻辑
        PlayerRestraintPart targetPart = null;
        if(part == PlayerRestraintPart.restraint_blindfold) {
            if(checkCanExpand(entity,PlayerRestraintPart.restraint_gag)) {
                targetPart = PlayerRestraintPart.restraint_gag;
            }
        }else if(part == PlayerRestraintPart.restraint_gag){
            if(checkCanExpand(entity,PlayerRestraintPart.restraint_body_bind)) {
                targetPart = PlayerRestraintPart.restraint_body_bind;
            }else if(checkCanExpand(entity,PlayerRestraintPart.restraint_blindfold)) {
                targetPart = PlayerRestraintPart.restraint_blindfold;
            }
        }else if(part == PlayerRestraintPart.restraint_body_bind) {
            if(checkCanExpand(entity,PlayerRestraintPart.restraint_arms_bind)) {
                targetPart = PlayerRestraintPart.restraint_arms_bind;
            }else if(checkCanExpand(entity,PlayerRestraintPart.restraint_legs_bind)) {
                targetPart = PlayerRestraintPart.restraint_legs_bind;
            }else if(checkCanExpand(entity,PlayerRestraintPart.restraint_gag)) {
                targetPart = PlayerRestraintPart.restraint_gag;
            }
        }else if(part == PlayerRestraintPart.restraint_arms_bind) {
            if(checkCanExpand(entity,PlayerRestraintPart.restraint_hands_bind)) {
                targetPart = PlayerRestraintPart.restraint_hands_bind;
            }else if(checkCanExpand(entity,PlayerRestraintPart.restraint_body_bind)) {
                targetPart = PlayerRestraintPart.restraint_body_bind;
            }
        }else if(part == PlayerRestraintPart.restraint_hands_bind) {
            if(checkCanExpand(entity,PlayerRestraintPart.restraint_arms_bind)) {
                targetPart = PlayerRestraintPart.restraint_arms_bind;
            }
        }else if(part == PlayerRestraintPart.restraint_legs_bind) {
            if(checkCanExpand(entity,PlayerRestraintPart.restraint_body_bind)) {
                targetPart = PlayerRestraintPart.restraint_body_bind;
            }
        }

        if (targetPart != null) {
            boolean success = addRestraintItem(entity, targetPart, new ItemStack(ModRestraintItems.MERCHANT_SLIME.get()));
            if (success && entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("item.restraint_dungeon.merchant_slime.expanding").withStyle(ChatFormatting.DARK_RED), true);
            }
        }
    }

    private boolean checkCanExpand(LivingEntity entity, PlayerRestraintPart part) {
        List<ItemStack> existing = getAllPartRestraint(entity, part);
        if (existing.isEmpty()) return true;
        for (ItemStack s : existing) {
            Item item = s.getItem();
            if (item instanceof MerchantSlimeItem || item instanceof SlimeItem || item instanceof LatexItem) {
                return false;
            }
        }
        return true;
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
    public double onStrengthStruggle(UUID playerUUID, double itemStrengthIndex) {
        Player player = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getPlayerByUUID(playerUUID) : null;
        if (player != null && player.isInWaterRainOrBubble()) {
            return itemStrengthIndex * 2.0;
        }
        return itemStrengthIndex;
    }

    @Override
    public boolean dropRestraintWhenStruggleOff(LivingEntity actionEntity, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        return false;
    }

    @Override
    public boolean dropRestraintWhenRelease(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        return false;
    }

    @Override
    public void onReleaseOff(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        if (addRestraintItem(actionEntity, PlayerRestraintPart.restraint_hands_bind, stack.copy())) {
            if (actionEntity instanceof Player actionPlayer) {
                actionPlayer.displayClientMessage(Component.translatable("item.restraint_dungeon.slime.expand_to_releaser").withStyle(ChatFormatting.DARK_RED), true);
            }
        }
    }

    @Override
    public Component canUseKidnap(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index) {
        return Component.translatable("item.restraint_dungeon.slime.cant_use_kidnap").withStyle(ChatFormatting.YELLOW);
    }

    private int slimeExpandTime(LivingEntity entity,ItemStack stack,PlayerRestraintPart bodyPart,int index) {
        return 1800;
    }

    private int growToLatexTime(LivingEntity entity,ItemStack stack,PlayerRestraintPart bodyPart,int index) {
        return 3600;
    }

    @Override
    public boolean shouldGagAndBlindfoldRenderOffset(PlayerRestraintPart part){
        return false;
    }
}