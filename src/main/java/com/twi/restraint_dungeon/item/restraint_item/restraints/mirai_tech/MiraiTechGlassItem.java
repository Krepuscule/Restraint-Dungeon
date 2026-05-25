package com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.hasRestraint;

public class MiraiTechGlassItem extends RestraintItem {

    public static final RestraintDefaults MIRAI_GLASS_DEFAULTS = new RestraintDefaults(
            500,    // maxResistance
            50.0,   // thrillValue
            0.25,   // strengthIndex
            0.25,   // looseIndex
            0.25    // lockIndex
    );

    private final List<String> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_blindfold.toString()
    );

    public MiraiTechGlassItem(Properties properties) {
        super(properties.stacksTo(1), MIRAI_GLASS_DEFAULTS);
        this.setCanEquipPartList(canEquipPartList);
        this.setConnectPartMap(new HashMap<>());
        this.setCanBeLocked(true);
    }

    private ItemStack getMiRaiTechSuit(LivingEntity entity) {
        if (entity == null) return null;
        List<ItemStack> stacks = getAllPartRestraint(entity, PlayerRestraintPart.restraint_body_bind);
        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof MiraiTechSuitItem) return stack;
        }
        return null;
    }

    @Override
    public Component canUseKidnap(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        if(hasRestraint(target,bodyPart,stack,false)){
            return Component.translatable("item.restraint_dungeon.cant_use_kidnap.mirai_tech").withStyle(ChatFormatting.DARK_RED);
        }
        return null;
    }

    @Override
    public boolean canBindCurrentPart(LivingEntity entity) {
        ItemStack suitStack = getMiRaiTechSuit(entity);
        if (suitStack == null || !(suitStack.getItem() instanceof MiraiTechSuitItem suit)) return false;
        
        // 检查 Suit 模块是否激活（Glass 索引通常为 0）
        return suit.isModuleActive(suitStack, 0);
    }

    @Override
    public ResourceLocation getTextureResourceLocation(LivingEntity entity, String bodyPart, ItemStack stack) {
        ItemStack suitStack = getMiRaiTechSuit(entity);
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        if (suitStack != null && suitStack.getItem() instanceof MiraiTechSuitItem suit) {
            boolean aroused = suit.isArousedMode(suitStack);
            boolean active = suit.isModuleActive(suitStack, 0);
            boolean locked = suit.isModuleLocked(suitStack, 0);

            String state = active ? "_activate" : "_deactivate";
            // 逻辑兼容：只要 active 或者是强制锁定状态就使用 lock 贴图
            String lock = (locked || active) ? "_lock" : "_unlock";

            return ResourceLocation.fromNamespaceAndPath(MODID, 
                "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/" + 
                itemName + (aroused ? "_aroused" : "") + state + lock + ".png");
        }

        return ResourceLocation.fromNamespaceAndPath(MODID, 
            "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/" + itemName + "_deactivate_unlock.png");
    }

    @Override
    public void renderBlindfoldOverlay(UUID playerUUID, GuiGraphics guiGraphics, ItemStack stack) {
        Player player = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getPlayerByUUID(playerUUID) : null;
        if (player == null) return;

        ItemStack suitStack = getMiRaiTechSuit(player);
        if (suitStack != null && suitStack.getItem() instanceof MiraiTechSuitItem suit) {
            String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            boolean aroused = suit.isArousedMode(suitStack);
            // 只有激活且锁定时才显示特殊 Overlay 逻辑（或根据你的需要调整）
            boolean isLocked = suit.isModuleActive(suitStack, 0) && suit.isModuleLocked(suitStack, 0);
            
            String path = String.format("textures/models/blindfold_overlay/mirai_tech/%s%s%s.png",
                    itemName, aroused ? "_aroused" : "", isLocked ? "_lock" : "_unlock");
            
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, path);

            int width = guiGraphics.guiWidth();
            int height = guiGraphics.guiHeight();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(texture, 0, 0, 0, 0, width, height, width, height);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.mirai_tech_glass").withStyle(ChatFormatting.GRAY));
    }
}