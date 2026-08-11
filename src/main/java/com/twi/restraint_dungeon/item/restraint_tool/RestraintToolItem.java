package com.twi.restraint_dungeon.item.restraint_tool;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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

    /**
     * 小玩具渲染实现
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends Player, M extends PlayerModel<T>> void renderRestraintToolLayer(
            M innerModel, M parentModel, T player, int index, ItemStack stack,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        boolean isSlim = false;

        if(player instanceof AbstractClientPlayer clientPlayer){
            isSlim = clientPlayer.getSkin().model() == PlayerSkin.Model.SLIM;
        }

        this.applyToolsVisibility(innerModel, parentModel, player);

        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);

        ResourceLocation texture = this.getTextureResourceLocation(player, stack,index,isSlim);
        VertexConsumer baseBuffer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(texture));


        innerModel.renderToBuffer(poseStack, baseBuffer, packedLight, overlay);
    }

    /**
     * 控制具体的模型部位渲染
     * 子类可以重写此方法来实现特殊的渲染需求
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends Player, M extends PlayerModel<T>> void applyToolsVisibility(
            M child, M parent,  T player) {

        child.setAllVisible(false);
        child.head.visible = true;
        child.body.visible = true;
        child.leftArm.visible = true;
        child.rightArm.visible = true;
        child.leftLeg.visible = true;
        child.rightLeg.visible = true;


        if (shouldRenderSencondLayer(child, parent, player)) {
            setSecondLayerVisibility(child, parent, player);
        }
    }

    /**
     * 总体控制开关，判断其是否应该渲染二层皮肤部分
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends Player, M extends PlayerModel<T>> boolean shouldRenderSencondLayer(M child, M parent,  T player){
        return false;
    }

    /**
     * 控制具体的模型二层皮肤部位渲染，通常不渲染
     * 子类可以重写此方法来实现特殊的渲染需求
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends Player, M extends PlayerModel<T>> void setSecondLayerVisibility(
            M child, M parent, T player) {

        // 同步二层皮肤的旋转与位置 (对齐骨骼)
        child.jacket.copyFrom(parent.body);
        child.leftSleeve.copyFrom(parent.leftArm);
        child.rightSleeve.copyFrom(parent.rightArm);
        child.leftPants.copyFrom(parent.leftLeg);
        child.rightPants.copyFrom(parent.rightLeg);


        child.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
        child.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
        child.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        child.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        child.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        child.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);

    }
}
