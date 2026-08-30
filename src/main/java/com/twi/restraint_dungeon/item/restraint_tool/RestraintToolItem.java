package com.twi.restraint_dungeon.item.restraint_tool;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils.*;

public class RestraintToolItem extends Item implements GeoItem {

    public enum RestraintToolDropRule {
        DEFAULT,      // 遵循原版gamerule（keepInventory）
        ALWAYS_DROP,  // 强制掉落
        ALWAYS_KEEP,  // 死亡后保留在拘束栈中
        DESTROY       // 死亡时销毁
    }

    public RestraintToolItem(Properties properties) {
        super(properties);
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }


    /**
     * 每 Tick 执行的逻辑
     */
    public void onEquipTick(LivingEntity entity, ItemStack stack, int index) {

    }

    /**
     * 装备时触发
     */
    public void onEquip(LivingEntity entity, ItemStack stack, int index) {

    }

    /**
     * 卸下时触发
     */
    public void onUnequip(LivingEntity entity, ItemStack stack, int index) {

    }

    /**
     * 是否允许装备
     */
    public boolean canEquip(LivingEntity entity) {
        return true;
    }

    /**
     * 是否允许卸下
     */
    public boolean canUnequip(LivingEntity entity, int index) {
        return true;
    }

    /**
     * 死亡掉落规则
     */
    public RestraintToolDropRule getDropRule(LivingEntity entity, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return RestraintToolDropRule.ALWAYS_DROP;

    }

    /**
     * 单个实体所能配备该小玩具的最大值
     */
    public int getMaxUsage(LivingEntity entity,ItemStack stack){
        return 1;
    }


    @OnlyIn(Dist.CLIENT)
    public Screen getConfigurationScreen(LivingEntity entity, ItemStack stack, int index) {
        return null;
    }

    /**
     * Item的文本提示信息
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND) {
            if (level.isClientSide()) {
                Screen config = this.getConfigurationScreen(player, stack, -1);
                if (config instanceof Screen screen) {
                    Minecraft.getInstance().setScreen(screen);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack,
                                                           @NotNull Player player,
                                                           @NotNull LivingEntity target,
                                                           @NotNull InteractionHand hand) {

        if (target instanceof Player targetPlayer) {

            if (player.level().isClientSide()) {
                return InteractionResult.sidedSuccess(true);
            }

            if (isToolsFull(targetPlayer)) {
                player.displayClientMessage(
                        Component.translatable("item." + MODID + ".restraint_tool.part_full").withStyle(ChatFormatting.DARK_RED),
                        true
                );
                return InteractionResult.FAIL;
            } else if (isMaxToolsEquip(target, stack, false)) {
                player.displayClientMessage(
                        Component.translatable("item." + MODID + ".restraint_tool.get_max_usage").withStyle(ChatFormatting.DARK_RED),
                        true
                );
                return InteractionResult.FAIL;
            } else {
                ItemStack toolToEquip = stack.copyWithCount(1);

                RestraintToolsUtils.addRestraintTool(targetPlayer, toolToEquip);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                player.swing(hand, true);


                player.displayClientMessage(
                        Component.translatable("item." + MODID + ".restraint_tool.equip_success").withStyle(ChatFormatting.GREEN),
                        true
                );

                targetPlayer.displayClientMessage(
                        Component.translatable("item." + MODID + ".restraint_tool.equipped_tool").withStyle(ChatFormatting.DARK_RED),
                        true
                );

                return InteractionResult.SUCCESS;
            }
        }else if(target instanceof BaseNPCEntity npc){
            if (player.level().isClientSide()) {
                return InteractionResult.sidedSuccess(true);
            }

            if (isToolsFull(npc)) {
                player.displayClientMessage(
                        Component.translatable("item." + MODID + ".restraint_tool.part_full").withStyle(ChatFormatting.DARK_RED),
                        true
                );
                return InteractionResult.FAIL;
            } else if (isMaxToolsEquip(target, stack, false)) {
                player.displayClientMessage(
                        Component.translatable("item." + MODID + ".restraint_tool.get_max_usage").withStyle(ChatFormatting.DARK_RED),
                        true
                );
                return InteractionResult.FAIL;
            } else {
                ItemStack toolToEquip = stack.copyWithCount(1);

                RestraintToolsUtils.addRestraintTool(npc, toolToEquip);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                player.swing(hand, true);


                player.displayClientMessage(
                        Component.translatable("item." + MODID + ".restraint_tool.equip_success").withStyle(ChatFormatting.GREEN),
                        true
                );

                return InteractionResult.SUCCESS;
            }
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    /**
     * 获取当前小玩具在对应部位的贴图
     * @param entity 目标实体
     * @param stack 小玩具ItemStack
     */
    public ResourceLocation getItemIconResourceLocation(LivingEntity entity,ItemStack stack) {

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());


        return ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(),
                "textures/item/restraint_tools/" + itemKey.getPath() + ".png");

    }

    /**
     * 获取当前小玩具在对应部位的贴图
     * @param entity 目标实体
     * @param stack 小玩具ItemStack
     */
    public ResourceLocation getTextureResourceLocation(LivingEntity entity, ItemStack stack,int index,boolean isSlim) {

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemName = itemKey.getPath();

        return ResourceLocation.fromNamespaceAndPath(MODID,
                "textures/models/restraint_tools/" + itemName + "/" + itemName + ".png");

    }

    public record restraintToolRenderData(
            PoseStack stack,
            RenderType renderType,
            VertexConsumer baseBuffer,
            int packedLight,
            int packedOverlay
    ){}

    /**
     * 小玩具渲染实现
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends HumanoidModel<T>> restraintToolRenderData renderRestraintToolLayer(T entity,  int index, ItemStack stack,
                                                                                                                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        boolean isSlim = false;

        if(entity instanceof AbstractClientPlayer clientPlayer){
            isSlim = clientPlayer.getSkin().model() == PlayerSkin.Model.SLIM;
        }else if (entity instanceof BaseNPCEntity npc) {
            isSlim = npc.isSlimModel();
        }

        ResourceLocation texture = this.getTextureResourceLocation(entity,stack,index,isSlim);
        RenderType type = RenderType.armorCutoutNoCull(texture);
        VertexConsumer baseBuffer = bufferSource.getBuffer(type);


        return new restraintToolRenderData(poseStack,type,baseBuffer,packedLight,packedOverlay);
    }

    /**
     * 控制具体的模型部位渲染
     * 子类可以重写此方法来实现特殊的渲染需求
     */
    public <T extends LivingEntity, M extends HumanoidModel<T>> void applyToolsVisibility(
            M child, M parent, BakedGeoModel geoModel, ItemStack stack, int index, T entity) {

        if(child == null && parent == null && geoModel != null){
            geoModel.getBone("head").get().setHidden(false);
            geoModel.getBone("torso").get().setHidden(false);
            geoModel.getBone("left_arm").get().setHidden(false);
            geoModel.getBone("right_arm").get().setHidden(false);
            geoModel.getBone("left_leg").get().setHidden(false);
            geoModel.getBone("right_leg").get().setHidden(false);

            if (shouldRenderSecondLayer(child, parent,geoModel,entity)) {
                setSecondLayerVisibility(child, parent,geoModel,entity);
            }
        }else if (child != null && parent != null && geoModel == null){
            child.setAllVisible(false);


            child.head.visible = parent.head.visible;
            child.body.visible = parent.body.visible;
            child.leftArm.visible = parent.leftArm.visible;
            child.rightArm.visible = parent.rightArm.visible;
            child.leftLeg.visible = parent.leftLeg.visible;
            child.rightLeg.visible = parent.rightLeg.visible;


            if (shouldRenderSecondLayer(child, parent,geoModel, entity)) {
                setSecondLayerVisibility(child, parent,geoModel, entity);
            }
        }
    }

    /**
     * 总体控制开关，判断其是否应该渲染二层皮肤部分
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends HumanoidModel<T>> boolean shouldRenderSecondLayer(
            M child, M parent, BakedGeoModel geoModel, T entity) {
        return false;
    }

    /**
     * 控制具体的模型二层皮肤部位渲染，通常不渲染
     * 子类可以重写此方法来实现特殊的渲染需求
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends HumanoidModel<T>> void setSecondLayerVisibility(
            M child, M parent, BakedGeoModel geoModel,  T entity) {

        if(child == null && parent == null && geoModel != null){

            geoModel.getBone("headwear").get().setHidden(false);
            geoModel.getBone("jacket").get().setHidden(false);
            geoModel.getBone("left_arm_layer").get().setHidden(false);
            geoModel.getBone("left_arm_bend_layer").get().setHidden(false);
            geoModel.getBone("right_arm_layer").get().setHidden(false);
            geoModel.getBone("right_arm_bend_layer").get().setHidden(false);
            geoModel.getBone("left_leg_layer").get().setHidden(false);
            geoModel.getBone("left_leg_bend_layer").get().setHidden(false);
            geoModel.getBone("right_leg_layer").get().setHidden(false);
            geoModel.getBone("right_leg_bend_layer").get().setHidden(false);

        } else if (child != null && parent != null && geoModel == null) {
            if (child instanceof PlayerModel<?> playerChild && parent instanceof PlayerModel<?> playerParent) {

                playerChild.jacket.copyFrom(playerParent.body);
                playerChild.leftSleeve.copyFrom(playerParent.leftArm);
                playerChild.rightSleeve.copyFrom(playerParent.rightArm);
                playerChild.leftPants.copyFrom(playerParent.leftLeg);
                playerChild.rightPants.copyFrom(playerParent.rightLeg);

                if(entity instanceof Player player){

                    playerChild.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
                    playerChild.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
                    playerChild.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
                    playerChild.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
                    playerChild.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
                    playerChild.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);

                }else {
                    playerChild.hat.visible = true;
                    playerChild.jacket.visible = true;
                    playerChild.leftSleeve.visible = true;
                    playerChild.rightSleeve.visible = true;
                    playerChild.leftPants.visible = true;
                    playerChild.rightPants.visible = true;
                }

            }
        }

    }
}
