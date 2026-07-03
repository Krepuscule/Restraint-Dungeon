package com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class MiraiTechMittenItem extends RestraintItem {

    public static final RestraintDefaults MIRAI_MITTEN_DEFAULTS = new RestraintDefaults(
            500,
            50.0,
            0.25,
            0.25,
            0.25
    );

    private final List<PlayerRestraintPart> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_hands_bind
    );

    public MiraiTechMittenItem(Properties properties) {
        super(properties.stacksTo(1), MIRAI_MITTEN_DEFAULTS);
        this.setCanEquipPartList(canEquipPartList);
        this.setConnectPartMap(new HashMap<>());
        this.setCanBeLocked(true);
    }

    private ItemStack getMiraiTechSuit(LivingEntity entity) {
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
        ItemStack suitStack = getMiraiTechSuit(entity);
        if (suitStack == null || !(suitStack.getItem() instanceof MiraiTechSuitItem suit)) return false;
        return suit.isModuleActive(suitStack, 3);
    }

    @Override
    public double onStrengthStruggle(UUID playerUUID, double ItemStrengthIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearHookStrugglingState(player)) {
                return ItemStrengthIndex * 2.0;
            }
        }
        return super.onStrengthStruggle(playerUUID, ItemStrengthIndex);
    }
    @Override
    public double onLooseStruggle(UUID playerUUID,double ItemLooseIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearHookStrugglingState(player)) {
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
            if(isNearHookStrugglingState(player)) {
                return ItemLockIndex * 2.0;
            }
        }
        return super.onLooseStruggle(playerUUID, ItemLockIndex);
    }

    @Override
    public ResourceLocation getTextureResourceLocation(LivingEntity entity, String bodyPart, ItemStack stack,int index,boolean isSlim) {
        ItemStack suitStack = getMiraiTechSuit(entity);
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        if (suitStack != null && suitStack.getItem() instanceof MiraiTechSuitItem suit) {
            boolean aroused = suit.isArousedMode(suitStack);
            boolean locked = suit.isModuleLocked(suitStack, 3);

            String suffix = (aroused ? "_aroused" : "") + (locked ? "_lock" : "_unlock");
            if(isSlim){
                return ResourceLocation.fromNamespaceAndPath(MODID,
                        "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/slim/" + itemName + suffix + ".png");
            }else{
                return ResourceLocation.fromNamespaceAndPath(MODID,
                        "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/wide/" + itemName + suffix + ".png");
            }
        }

        return ResourceLocation.fromNamespaceAndPath(MODID, 
                "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/wide/" + itemName + "_unlock.png");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.mirai_tech_mitten").withStyle(ChatFormatting.GRAY));
    }
}