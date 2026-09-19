package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

public class VividChainsItem extends RestraintItem {
    public static final RestraintDefaults VIVID_CHAINS_DEFAULTS = new RestraintDefaults(
            400,
            15.0,
            0.5,
            0.75,
            1.25
    );

    public VividChainsItem(Properties properties) {
        super(properties.stacksTo(1), VIVID_CHAINS_DEFAULTS);

        this.setCanEquipPartList(List.of(
                PlayerRestraintPart.restraint_blindfold,
                PlayerRestraintPart.restraint_gag,
                PlayerRestraintPart.restraint_arms_bind,
                PlayerRestraintPart.restraint_body_bind,
                PlayerRestraintPart.restraint_legs_bind
        ));

        Map<String, List<String>> connectMap = new HashMap<>();
        connectMap.put(PlayerRestraintPart.restraint_body_bind.toString(),
                List.of(PlayerRestraintPart.restraint_arms_bind.toString(), PlayerRestraintPart.restraint_legs_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_arms_bind.toString(),
                List.of(PlayerRestraintPart.restraint_body_bind.toString()));
        connectMap.put(PlayerRestraintPart.restraint_legs_bind.toString(),
                List.of(PlayerRestraintPart.restraint_body_bind.toString()));

        this.setConnectPartMap(connectMap);

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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {

        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.vivid_chains")
                .withStyle(ChatFormatting.GRAY));
    }
}
