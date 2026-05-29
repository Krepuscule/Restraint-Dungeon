package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ArmBinderItem extends RestraintItem {

    public static final RestraintDefaults ARM_BINDER_DEFAULTS = new RestraintDefaults(
            350,
            60.0,
            0.1,
            0.1,
            1.5
    );

    public ArmBinderItem(Properties properties) {
        super(properties.stacksTo(1), ARM_BINDER_DEFAULTS);

        Map<PlayerRestraintPart, List<PlayerRestraintPart>> boundMap = new HashMap<>();
        boundMap.put(PlayerRestraintPart.restraint_arms_bind, List.of(PlayerRestraintPart.restraint_hands_bind));
        this.setBoundPartMap(boundMap);

        this.setCanEquipPartList(List.of(PlayerRestraintPart.restraint_arms_bind.toString()));

        Map<String, List<String>> connectMap = new HashMap<>();
        connectMap.put(PlayerRestraintPart.restraint_arms_bind.toString(), List.of(PlayerRestraintPart.restraint_hands_bind.toString()));
        this.setConnectPartMap(connectMap);

        this.setCanBeLocked(true);
    }

    @Override
    public <T extends Player, M extends PlayerModel<T>> void applyRestraintVisibility(
            M child, M parent,PlayerRestraintPart part, T player) {

        child.body.visible = parent.body.visible;
        child.leftArm.visible = parent.leftArm.visible;
        child.rightArm.visible = parent.rightArm.visible;

        if(shouldRenderSencondLayer(child,parent,part,player)){
            setSecondLayerVisibility(child,parent,part,player);
        }
    }

    @Override
    public <T extends Player, M extends PlayerModel<T>> boolean shouldRenderSencondLayer(M child, M parent,PlayerRestraintPart part, T player){
        return false;
    }

    @Override
    public <T extends Player, M extends PlayerModel<T>> void setSecondLayerVisibility(
            M child, M parent,PlayerRestraintPart part, T player) {

        child.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
        child.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        child.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);

        child.jacket.copyFrom(parent.body);
        child.leftSleeve.copyFrom(parent.leftArm);
        child.rightSleeve.copyFrom(parent.rightArm);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.arm_binder").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean canBlockConnectPart(LivingEntity entity) {
        return true;
    }
}