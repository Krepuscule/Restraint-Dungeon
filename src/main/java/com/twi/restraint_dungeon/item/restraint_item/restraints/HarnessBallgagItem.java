package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class HarnessBallgagItem extends RestraintItem {

    public static final RestraintDefaults HARNESS_BALL_GAG_DEFAULTS = new RestraintDefaults(
            350,
            30.0,
            0.1,
            0.25,
            1.25
    );

    private final List<PlayerRestraintPart> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_gag
    );

    public HarnessBallgagItem(Properties properties) {
        super(properties.stacksTo(1), HARNESS_BALL_GAG_DEFAULTS);
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
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.harness_ball_gag").withStyle(ChatFormatting.GRAY));
    }
}