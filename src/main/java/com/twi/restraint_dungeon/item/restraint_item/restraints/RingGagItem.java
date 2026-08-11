package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.GagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;

public class RingGagItem extends RestraintItem {
    public static final RestraintDefaults RING_GAG_DEFAULTS = new RestraintDefaults(
            100,
            20.0,
            0.25,
            0.75,
            1.25
    );

    private final List<RestraintCapability.PlayerRestraintPart> canEquipPartList = List.of(
            RestraintCapability.PlayerRestraintPart.restraint_gag
    );

    public RingGagItem(Properties properties) {
        super(properties.stacksTo(1), RING_GAG_DEFAULTS);
        this.setCanEquipPartList(canEquipPartList);
        this.setConnectPartMap(new HashMap<>());
        this.setCanBeLocked(true);
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
    public boolean canStuffedGag(LivingEntity entity, ItemStack gagStack) {
        return false;
    }

    @Override
    public boolean canBlockedGag(LivingEntity entity, ItemStack gagStack) {
        return false;
    }

    @Override
    public Component translateGagMessage(LivingEntity entity, Component message){
        List<ItemStack> gags = getAllRestraintsByPart(entity, RestraintCapability.PlayerRestraintPart.restraint_gag);
        for(ItemStack gag : gags){
            if(gag.getItem() instanceof RestraintItem ri && !(gag.getItem() instanceof RingGagItem)){
                return ri.translateGagMessage(entity, message);
            }
        }

        String originMessage = message.getString();
        MutableComponent resultComponent = Component.empty();

        Random rand = new Random();
        for(int i = 0; i < originMessage.length(); i++){
            String sub = originMessage.substring(i, i + 1);

            if(GagUtils.GAG_MESSAGE_IGNORE_LIST.contains(sub)){
                resultComponent.append(Component.literal(sub));
                continue;
            }

            int num = rand.nextInt(10);
            String translationKey;

            if(num < 3){
                translationKey = "item." + MODID + ".ring_gag.gag_message.00";
            }else if(num < 6){
                translationKey = "item." + MODID + ".ring_gag.gag_message.01";
            }else if(num < 9){
                translationKey = "item." + MODID + ".ring_gag.gag_message.02";
            }else{
                translationKey = "item." + MODID + ".ring_gag.gag_message.03";
            }

            resultComponent.append(Component.translatable(translationKey));
        }

        if(getPleasantValue(entity) >= 75){
            resultComponent.append(Component.literal("~ ♥♥♥"));
        }else if(getPleasantValue(entity) >= 50){
            resultComponent.append(Component.literal("~ ♥"));
        }

        return resultComponent.withStyle(ChatFormatting.WHITE);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.ring_gag").withStyle(ChatFormatting.GRAY));
    }
}
