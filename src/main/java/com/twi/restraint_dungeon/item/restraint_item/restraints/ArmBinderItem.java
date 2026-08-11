package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
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
import software.bernie.geckolib.cache.object.BakedGeoModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;

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

        this.setCanEquipPartList(List.of(PlayerRestraintPart.restraint_arms_bind));

        Map<String, List<String>> connectMap = new HashMap<>();
        connectMap.put(PlayerRestraintPart.restraint_arms_bind.toString(), List.of(PlayerRestraintPart.restraint_hands_bind.toString()));
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
    public <T extends LivingEntity, M extends HumanoidModel<T>> void applyRestraintVisibility(
            M child, M parent, BakedGeoModel geoModel,ItemStack stack, PlayerRestraintPart part, int index, T entity){

        if(child == null){
            geoModel.getBone("torso").get().setHidden(false);
            geoModel.getBone("right_arm").get().setHidden(false);
            geoModel.getBone("right_arm_bend").get().setHidden(false);
            geoModel.getBone("left_arm").get().setHidden(false);
            geoModel.getBone("left_arm_bend").get().setHidden(false);
        }else{
            child.body.visible = parent.body.visible;
            child.leftArm.visible = parent.leftArm.visible;
            child.rightArm.visible = parent.rightArm.visible;

            if(shouldRenderSecondLayer(child,parent,geoModel,part,entity)){
                setSecondLayerVisibility(child,parent,geoModel,part,entity);
            }
        }
    }

    @Override
    public <T extends LivingEntity, M extends HumanoidModel<T>> boolean shouldRenderSecondLayer(
            M child, M parent,BakedGeoModel geoModel, PlayerRestraintPart part, T entity) {
        return false;
    }

    @Override
    public <T extends LivingEntity, M extends HumanoidModel<T>> void setSecondLayerVisibility(
            M child, M parent,BakedGeoModel geoModel, PlayerRestraintPart part, T entity){

        if (child instanceof PlayerModel<?> playerChild && parent instanceof PlayerModel<?> playerParent) {
            if(entity instanceof Player player){
                playerChild.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
                playerChild.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
                playerChild.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            }else{
                playerChild.jacket.visible = true;
                playerChild.leftSleeve.visible = true;
                playerChild.rightSleeve.visible = true;
            }

            playerChild.jacket.copyFrom(playerParent.body);
            playerChild.leftSleeve.copyFrom(playerParent.leftArm);
            playerChild.rightSleeve.copyFrom(playerParent.rightArm);
        }
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