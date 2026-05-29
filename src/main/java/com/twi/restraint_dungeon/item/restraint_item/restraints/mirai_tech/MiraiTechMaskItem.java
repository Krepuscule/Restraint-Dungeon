package com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.hasRestraint;

public class MiraiTechMaskItem extends RestraintItem {

    public static final RestraintDefaults MIRAI_MASK_DEFAULTS = new RestraintDefaults(
            500,
            50.0,
            0.25,
            0.25,
            0.25
    );

    private final List<String> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_gag.toString()
    );

    public MiraiTechMaskItem(Properties properties) {
        super(properties.stacksTo(1), MIRAI_MASK_DEFAULTS);
        this.setCanEquipPartList(canEquipPartList);
        this.setConnectPartMap(new HashMap<>());
        this.setCanBeLocked(true);
    }

    private ItemStack getMiRaiTechSuit(LivingEntity entity) {
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

        return suit.isModuleActive(suitStack, 1);
    }

    @Override
    public ResourceLocation getTextureResourceLocation(LivingEntity entity, String bodyPart, ItemStack stack,boolean isSlim) {
        ItemStack suitStack = getMiRaiTechSuit(entity);
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        if (suitStack != null && suitStack.getItem() instanceof MiraiTechSuitItem suit) {
            boolean aroused = suit.isArousedMode(suitStack);
            boolean locked = suit.isModuleLocked(suitStack, 1);

            String suffix = (aroused ? "_aroused" : "") + (locked ? "_lock" : "_unlock");
            return ResourceLocation.fromNamespaceAndPath(MODID,
                    "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/" + itemName + suffix + ".png");
        }

        return ResourceLocation.fromNamespaceAndPath(MODID, 
                "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/" + itemName + "_unlock.png");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.mirai_tech_mask").withStyle(ChatFormatting.GRAY));
    }
}