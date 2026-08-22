package com.twi.restraint_dungeon.item.restraint_item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.attributes.ModAttributes;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.ArmsPose;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.LegsPose;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.DataComponentsUtils;
import com.twi.restraint_dungeon.item.ModDataComponents;
import com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.ReleaseDropType;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.GagUtils;
import com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.StruggleDropType;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.item.DataComponentsUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.DEFAULT_BIND_DISTANCE;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.DEFAULT_BIND_TIME;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.PleasantUtils.getPleasantValue;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.DEFAULT_RELEASE_DISTANCE;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.DEFAULT_RELEASE_TIME;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.GagUtils.defaultGagLimitedChatRange;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.GagUtils.defaultHeavyGagLimitedChatRange;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRenderOffset;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenHeavyGag;

public class RestraintItem extends Item implements GeoItem {

    /*------------------------------------------------- 默认常量 ---------------------------------------------------------*/

    public enum RestraintDropRule {
        DEFAULT,      // 遵循原版gamerule（keepInventory）
        ALWAYS_DROP,  // 强制掉落
        ALWAYS_KEEP,  // 死亡后保留在拘束栈中
        DESTROY       // 死亡时销毁
    }

    /*------------------------------------------------- 属性值定义 ---------------------------------------------------------*/
    private final int defaultMaxResistance;
    private final double defaultThrillValue;
    private final double defaultStrengthIndex;
    private final double defaultLooseIndex;
    private final double defaultLockIndex;

    private List<PlayerRestraintPart> canEquipPartList;
    private Map<PlayerRestraintPart, List<PlayerRestraintPart>> boundPartMap; // 记录装备在指定部位时同样会束缚的其他部位的List
    private Map<String, List<String>> connectPartMap; // 记录装备在指定部位时连接束缚的部位
    private boolean canBeLocked;

    public RestraintItem(Properties properties, RestraintDefaults defaults) {
        super(properties);
        this.defaultMaxResistance = defaults.maxResistance;
        this.defaultThrillValue = defaults.thrillValue;
        this.defaultStrengthIndex = defaults.strengthIndex;
        this.defaultLooseIndex = defaults.looseIndex;
        this.defaultLockIndex = defaults.lockIndex;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

//    @Override
//    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
//        consumer.accept(new GeoRenderProvider() {
//            private GeoArmorRenderer<?> renderer;
//
//            @Override
//            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack,
//                                                                                 @Nullable EquipmentSlot equipmentSlot,
//                                                                                 @Nullable HumanoidModel<T> original) {
//                if (this.renderer == null) {
//                    this.renderer = new RestraintArmorRenderer();
//                }
//                return this.renderer;
//            }
//        });
//    }

    /**
     * 该拘束具是否可附魔
     */
    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return true;
    }

    /**
     * 返回该拘束具可用的附魔类型
     */
    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack,
                                       @NotNull Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.BINDING_CURSE) || enchantment.is(Enchantments.VANISHING_CURSE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    public int getMaxResistance(ItemStack stack) {
        return DataComponentsUtils.getMaxResistance(stack, defaultMaxResistance);
    }

    public double getThrillValue(ItemStack stack) {
        return DataComponentsUtils.getThrillValue(stack,defaultThrillValue);
    }

    public double getStrengthIndex(ItemStack stack) {
        return DataComponentsUtils.getStrengthIndex(stack,defaultStrengthIndex);
    }

    public double getLooseIndex(ItemStack stack) {
        return DataComponentsUtils.getLooseIndex(stack,defaultLooseIndex);
    }

    public double getLockIndex(ItemStack stack) {
        return DataComponentsUtils.getLockIndex(stack,defaultLockIndex);
    }

    public ItemStack getLockType(LivingEntity entity, ItemStack stack) {
        return DataComponentsUtils.getLockItem(entity, stack);
    }

    public void setMaxResistance(LivingEntity entity,ItemStack stack, int value) {
        updateMaxResistance(entity, stack, value);
    }

    public void setThrillValue(LivingEntity entity,ItemStack stack, double value) {
        updateThrillValue(entity, stack, value);
    }

    public void setStrengthIndex(LivingEntity entity,ItemStack stack, double value) {
        updateStrengthIndex(entity, stack, value);
    }

    public void setLooseIndex(LivingEntity entity,ItemStack stack, double value) {
        updateLooseIndex(entity, stack, value);
    }

    public void setLockIndex(LivingEntity entity,ItemStack stack, double value) {

        updateLockIndex(entity,stack,value);
    }

    public void setLockType(LivingEntity entity,ItemStack stack, ItemStack lockItem) {
        updateLockItem(entity, stack, lockItem);
    }

    public void reset(LivingEntity entity,ItemStack stack) {
        stack.remove(ModDataComponents.MAX_RESISTANCE);
        stack.remove(ModDataComponents.THRILL_VALUE);
        stack.remove(ModDataComponents.STRENGTH_INDEX);
        stack.remove(ModDataComponents.LOOSE_INDEX);
        stack.remove(ModDataComponents.LOCK_INDEX);
        stack.remove(ModDataComponents.LOCK_ITEM);

        var cap = entity.getData(ModAttachments.RESTRAINT_STACK);
        entity.setData(ModAttachments.RESTRAINT_STACK, cap);
    }

    public void setCanBeLocked(boolean canBeLocked) { this.canBeLocked = canBeLocked; }
    public boolean isCanBeLocked() { return canBeLocked; }

    public void setCanEquipPartList(List<PlayerRestraintPart> list) { this.canEquipPartList = list; }
    public List<PlayerRestraintPart> getCanEquipPartList() { return canEquipPartList; }

    public void setBoundPartMap(Map<PlayerRestraintPart, List<PlayerRestraintPart>> map) { this.boundPartMap = map; }
    public Map<PlayerRestraintPart, List<PlayerRestraintPart>> getBoundPartMap() { return boundPartMap; }

    public void setConnectPartMap(Map<String, List<String>> map) { this.connectPartMap = map; }
    public Map<String, List<String>> getConnectPartMap() { return connectPartMap; }

    // 默认数据封装
    public record RestraintDefaults(int maxResistance, double thrillValue, double strengthIndex, double looseIndex, double lockIndex) {}


    /**
     * Item的文本提示信息
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (this.canEquipPartList != null && !this.canEquipPartList.isEmpty()) {
            MutableComponent partsComponent = Component.empty();

            for (int i = 0; i < this.canEquipPartList.size(); i++) {
                String partName = this.canEquipPartList.get(i).name();

                String langKey = "part." + MODID + "." + partName;

                MutableComponent partComponent = Component.translatable(langKey)
                        .withStyle(ChatFormatting.GOLD);

                partsComponent.append(partComponent);

                if (i < this.canEquipPartList.size() - 1) {
                    partsComponent.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                }
            }

            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("tooltip.restraint_dungeon.can_equip_tips")).withStyle(ChatFormatting.WHITE)
                    .append(partsComponent));
        } else {
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("tooltip.restraint_dungeon.no_can_equip_part")
                            .withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    /**
     * 每 Tick 执行的逻辑
     */
    public void onRestraintTick(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {

    }

    /**
     * 装备时触发
     */
    public void onEquip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {

    }

    /**
     * 卸下时触发
     */
    public void onUnequip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {

    }

    /**
     * 是否允许装备
     */
    public boolean canEquip(LivingEntity entity, PlayerRestraintPart part) {
        return true;
    }

    /**
     * 是否允许卸下
     */
    public boolean canUnequip(LivingEntity entity, PlayerRestraintPart part, int index) {
        return true;
    }

    /**
     * 死亡掉落规则
     */
    public RestraintDropRule getDropRule(LivingEntity entity, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return RestraintDropRule.ALWAYS_DROP;

    }

    /**
     * 是否影响与末影人的直视
     */
    @Override
    public boolean isEnderMask(@NotNull ItemStack stack, @NotNull Player player, @NotNull EnderMan endermanEntity) {
        var blindfolds = RestraintStackUtils.getAllRestraintsByPart(player, PlayerRestraintPart.restraint_blindfold);

        if (blindfolds.contains(stack)) {
            return this.canBindCurrentPart(player);
        }

        return false;
    }

    /* ---------------------------------------------- 堵嘴独特功能部分 --------------------------------------------------- */

    /**
     * 若使用在堵嘴物栏位，该拘束具是否为塞嘴型拘束具
     * @param entity 目标实体,
     * @param gagStack 该堵嘴物的ItemStack
     */
    public boolean canStuffedGag(LivingEntity entity, ItemStack gagStack) {
        return false;
    }

    /**
     * 若使用在堵嘴物栏位，该拘束具是否为封嘴型拘束具
     * @param entity 目标实体,
     * @param gagStack 该堵嘴物的ItemStack
     */
    public boolean canBlockedGag(LivingEntity entity, ItemStack gagStack) {
        return true;
    }

    /**
     * 当该拘束具为最下层的堵嘴拘束具时，其对玩家发送文本的转译
     * @param entity 发送者实体,
     * @param message 玩家发送的原始信息
     */
    public Component translateGagMessage(LivingEntity entity, Component message){

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
                translationKey = "item." + MODID + ".gag_message.00";
            }else if(num < 6){
                translationKey = "item." + MODID + ".gag_message.01";
            }else if(num < 9){
                translationKey = "item." + MODID + ".gag_message.02";
            }else{
                translationKey = "item." + MODID + ".gag_message.03";
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

    /**
     * 当该拘束具为最下层的堵嘴拘束具时，发送文本的传播距离（同时需要满足指定的接收玩家列表）
     * @param entity 发送者实体
     */
    public int getGagMessageSpreadRange(LivingEntity entity){
        if(isBeenHeavyGag(entity)){
            return defaultHeavyGagLimitedChatRange;
        }else {
            return defaultGagLimitedChatRange;
        }
    }

    /* ---------------------------------------------- 项圈独特功能部分 --------------------------------------------------- */

    /**
     * 当该拘束具为最下层的项圈拘束具时，修改玩家在聊天栏/列表中的显示名称
     * @param entity 目标实体
     * @param collarStack 拘束具ItemStack
     */
    public MutableComponent getAdditionCollarName(LivingEntity entity, ItemStack collarStack) {
        if (collarStack.has(DataComponents.CUSTOM_NAME)) {
            Component titleName = collarStack.getHoverName();

            MutableComponent prefix = Component.literal("* ")
                    .append(titleName)
                    .append(" * ")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));

            return Component.empty()
                    .append(prefix)
                    .append(entity.getName())
                    .append(Component.literal(" "));
        }

        return entity.getName().copy();
    }

    /**
     * 当该拘束具为最下层的项圈拘束具时，修改玩家头顶的 NameTag
     * @param entity 目标玩家
     * @param collarStack 拘束具ItemStack
     */
    public MutableComponent getAdditionCollarNameTag(LivingEntity entity, ItemStack collarStack) {
        if (collarStack.has(DataComponents.CUSTOM_NAME)) {
            MutableComponent prefix = Component.literal("* ")
                    .append(collarStack.getHoverName())
                    .append(" * ")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

            return Component.empty()
                    .append(prefix)
                    .append(entity.getName());
        }

        return entity.getName().copy();
    }

    /* ---------------------------------------------- 连接其它部位束缚部分 --------------------------------------------------- */

    /**
     * 该拘束具是否阻碍其当前和连接部位添加/移除束缚
     * @param entity 目标实体
     */
    public boolean canBlockConnectPart(LivingEntity entity){
        return false;
    }

    /**
     * 该拘束具是否受当前部位内部的拘束具阻碍而无法装备
     * @param entity 目标玩家
     */
    public boolean canBeBlockByInner(LivingEntity entity){
        return false;
    }

    /**
     * 该拘束具是否受当前和连接部位阻碍添加/移除束缚
     * @param entity 目标玩家
     */
    public boolean canBeBlockByConnect(LivingEntity entity){
        return false;
    }

    /* ---------------------------------------------- 拘束具自身属性 --------------------------------------------------- */

    /**
     * 该拘束具是否激活束缚当前部位
     * @param entity 目标玩家
     */
    public boolean canBindCurrentPart(LivingEntity entity){
        return true;
    }

    /**
     * 当该拘束具为最下层的手臂拘束具时，玩家的手臂拘束姿势
     * @param entity 目标实体
     */
    public ArmsPose setBindArmsPose(LivingEntity entity){
        if(this.canBindCurrentPart(entity)){
            return ArmsPose.CROSS_BEHIND_BACK;
        }
        return ArmsPose.NONE;
    }

    /**
     * 当该拘束具为最下层的双腿拘束具时，玩家的双腿拘束姿势
     * @param entity 目标实体
     */
    public LegsPose setBindLegsPose(LivingEntity entity){
        if(this.canBindCurrentPart(entity)){
            return LegsPose.LEGS_TOGETHER;
        }
        return LegsPose.NONE;
    }

    /* ---------------------------------------------- 连接拘束部位属性 --------------------------------------------------- */

    /**
     * 判断是否按照默认方式读取该拘束具的贴图（如果你不知道这是什么，请勿使用）
     * @param entity 当前实体
     */
    public boolean shouldRenderConnectionBind(LivingEntity entity){
        return false;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，玩家在切换到连接状态前所需要的姿势,若为Null则该连接拘束具不会限制姿势切换
     * @param entity 当前实体
     */
    public RestraintPosition getConnectBindPreviousPosition(LivingEntity entity){
        return null;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，玩家的拘束动画名（返回动画.json文件的文件名）
     * @param entity 当前实体
     */
    public String getConnectBindAnimation(LivingEntity entity){
        return null;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，玩家的切换状态拘束动画名（返回动画.json文件的文件名）
     * @param entity 当前实体
     */
    public String getConnectBindTranslateAnimation(LivingEntity entity){
        return null;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，玩家的解开拘束动画名（返回动画.json文件的文件名）
     * @param entity 当前实体
     */
    public String getConnectBindReleaseAnimation(LivingEntity entity){
        return null;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，玩家的挣扎状态拘束动画名（返回动画.json文件的文件名）
     * @param entity 当前实体
     */
    public String getConnectBindStrugglingAnimation(LivingEntity entity){
        return null;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，第一视角的偏移参数
     * @param player 当前玩家
     * @param stack 连接部位的拘束具
     */
    public Vector3f getConnectBindViewOffset(Player player, ItemStack stack){
        return null;
    }

    /**
     * 当该拘束具为最下层的连接拘束具时，第一视角的旋转参数
     * @param player 当前玩家,
     * @param stack 连接部位的拘束具
     */
    public Vector3f getConnectBindViewRotation(Player player, ItemStack stack){
        return null;
    }

    /**
     * 判断该拘束具能否绑定到连接束缚部位,若能则返回Null，若不能则返回原因
     * @param actionEntity 当前实体,
     * @param target 目标生物,
     * @param stack 将要捆绑到连接部位的拘束具
     */
    public Component canConnectBind(LivingEntity actionEntity,LivingEntity target,ItemStack stack){
        return null;
    }

    /**
     * 判断该连接束缚部位是否可以挣扎,若能则返回Null，若不能则返回原因
     * @param entity 当前实体,
     * @param stack 将要挣扎的连接部位拘束具
     */
    public Component canConnectStruggle(LivingEntity entity,ItemStack stack){
        return null;
    }

    /**
     * 判断该连接束缚部位是否可以自行挣扎释放,若能则返回Null，若不能则返回原因
     * @param entity 当前实体
     * @param stack 将要挣扎释放的连接部位拘束具
     */
    public Component canConnectReleaseBySelf(LivingEntity entity,ItemStack stack){
        return null;
    }

    /**
     * 判断该连接束缚部位是否可以释放,若能则返回Null，若不能则返回原因
     * @param actionEntity 动作实体,
     * @param target 目标生物,
     * @param stack 释放下来的连接部位的拘束具
     */
    public Component canConnectRelease(LivingEntity actionEntity,LivingEntity target,ItemStack stack){
        return null;
    }

    /* ---------------------------------------------- 挣扎属性相关 --------------------------------------------------- */

    /**
     * 该拘束具是否允许自行挣脱,若能则返回Null，若不能则返回原因
     * @param entity 当前挣脱束缚的实体,
     * @param stack  即将挣扎的物品
     */
    public Component canBeStruggle(LivingEntity entity,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        return null;
    }

    /**
     * 该拘束具是否允许自行释放,若能则返回Null，若不能则返回原因
     * @param entity 当前挣脱束缚的目标实体,
     * @param stack  即将挣扎释放的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public Component canBeReleaseBySelf(LivingEntity entity,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        return null;
    }


    /**
     * 自行挣脱带锁的拘束具时，是否清除该物品的锁具信息
     * @param entity 当前挣脱束缚的实体,
     * @param stack  即将挣扎的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public boolean cleanLockWhenStruggleOufOfBind(LivingEntity entity,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        return true;
    }

    /**
     * 自行挣脱拘束具时，是否掉落挣脱的拘束具
     * @param entity 当前挣脱束缚的实体,
     * @param stack  即将挣扎的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public boolean dropRestraintWhenStruggleOff(LivingEntity entity,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        return !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP);
    }

    /**
     * 自行挣脱拘束具时，若掉落拘束具，应将拘束具置于何处
     * @param entity 当前挣脱束缚的实体,
     * @param stack  即将挣扎的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public StruggleDropType dropStruggleItemDirection(LivingEntity entity, ItemStack stack, PlayerRestraintPart bodyPart, int index){
        return StruggleDropType.DROP_TO_ENTITY;
    }

    /**
     * 当玩家正在进行蛮力挣扎时，每Tick执行的功能
     * @param player 当前挣脱束缚的玩家,
     * @param stack  即将挣扎的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public void strengthStruggleTick(Player player,ItemStack stack,PlayerRestraintPart bodyPart,int index){

    }

    /**
     * 当玩家正在进行松动束缚时，每Tick执行的功能
     * @param player 当前挣脱束缚的玩家,
     * @param stack  即将挣扎的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public void looseStruggleTick(Player player,ItemStack stack,PlayerRestraintPart bodyPart,int index){

    }
    /**
     * 当玩家正在进行打开锁扣时，每Tick执行的功能
     * @param player 当前挣脱束缚的玩家,
     * @param stack  即将挣扎的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public void unlockStruggleTick(Player player,ItemStack stack,PlayerRestraintPart bodyPart,int index){

    }

    /**
     * 当玩家开始蛮力挣扎时，拘束具的蛮力挣扎Index值设定 (在客户端执行）
     * @param playerUUID 目标玩家的UUID,
     * @param ItemStrengthIndex 拘束具的蛮力挣扎修正值
     */
    public double onStrengthStruggle(UUID playerUUID, double ItemStrengthIndex){
        return ItemStrengthIndex;
    }

    /**
     * 当玩家开始松动束缚时，拘束具的蛮力挣扎Index值设定 (在客户端执行）
     * @param playerUUID 目标玩家的UUID,
     * @param ItemLooseIndex 拘束具的松动束缚修正值
     */
    public double onLooseStruggle(UUID playerUUID,double ItemLooseIndex){
        return ItemLooseIndex;
    }

    /**
     * 当玩家开始打开锁扣时，拘束具的蛮力挣扎Index值设定 (在客户端执行）
     * @param playerUUID 目标玩家的UUID,
     * @param ItemLockIndex 拘束具的打开锁扣修正值
     */
    public double onUnlockStruggle(UUID playerUUID,double ItemLockIndex){
        return ItemLockIndex;
    }

    /**
     * 自行挣脱拘束具后触发的功能
     * @param entity 当前挣脱束缚的实体,
     * @param stack  即将挣扎的物品,
     * @param bodyPart 选定的目标部位,
     * @param index 物品所在栏位的索引
     */
    public void onStruggleOff(LivingEntity entity,ItemStack stack,PlayerRestraintPart bodyPart,int index){

    }

    /* ---------------------------------------------- 捆绑拘束具属性部分 --------------------------------------------------- */

    /**
     * 该拘束具是否允许用于捆绑他人,若能则返回Null，若不能则返回原因
     * @param actionEntity 执行束缚动作的动作实体,
     * @param target 被绑的目标实体,
     * @param stack 绑在目标身上的ItemStack,
     * @param bodyPart 指定的目标部位,
     * @param index 绑定目标部位栏位的索引
     */
    public Component canUseKidnap(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        return null;
    }

    /**
     * 手持该拘束具绑架其他玩家时，所需的最大距离
     * @param entity 执行束缚动作的动作实体
     * @param target 被绑的目标实体
     * @param stack 使用的拘束具
     */
    public double getKidnapDistance(LivingEntity entity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart){

        return DEFAULT_BIND_DISTANCE;
    }

    /**
     * 手持该拘束具绑架其他玩家时，所需的时间
     * @param actionEntity 执行束缚动作的动作实体
     * @param target 被绑的目标实体
     * @param stack 使用的拘束具
     */
    public long getKidnapTime(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart){

        if(actionEntity == null || actionEntity.getAttribute(ModAttributes.RESTRAINT_STRENGTH) == null) return DEFAULT_BIND_TIME;


        return (long) (DEFAULT_BIND_TIME *
                Objects.requireNonNull(actionEntity.getAttribute(ModAttributes.RESTRAINT_STRENGTH)).getValue());
    }

    /**
     * 手持该物品束缚目标玩家时，是否要减少动作玩家手中的拘束具数量
     * @param actionEntity 执行束缚动作的动作实体,
     * @param target 被绑的目标实体,
     * @param stack 绑在目标身上的ItemStack,
     * @param bodyPart 指定的目标部位,
     * @param index 绑定目标部位栏位的索引
     */
    public boolean reduceStackWhenKidnapFinish(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        if(actionEntity instanceof Player actionPlayer){
            return !actionPlayer.isCreative();
        }

        return true;
    }

    /**
     * 当该物品束缚在目标玩家身上时触发的功能
     * @param actionEntity 执行束缚动作的动作实体,
     * @param target 被绑的目标实体,
     * @param stack 绑在目标身上的ItemStack,
     * @param bodyPart 指定的目标部位,
     * @param index 绑定目标部位栏位的索引
     */
    public void onKidnapToTarget(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){

    }

    /* ---------------------------------------------- 释放拘束具属性部分 --------------------------------------------------- */

    /**
     * 该拘束具是否允许被释放开,若能则返回Null，若不能则返回原因
     * @param actionEntity 当前释放目标的实体,
     * @param target 被释放的目标实体,
     * @param stack 释放的拘束具物品,
     * @param bodyPart 释放的部位,
     * @param index 物品所在部位的索引序号
     */
    public Component canBeReleased(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index){
        return null;
    }

    /**
     * 空手释放其他玩家身上的该束缚时，所需的最大距离
     * @param actionEntity 当前释放目标的实体,
     * @param target 被释放的目标实体,
     * @param stack 释放的拘束具物品,
     * @param bodyPart 释放的部位,
     */
    public double getReleaseDistance(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart){
        return DEFAULT_RELEASE_DISTANCE;
    }

    /**
     * 空手释放其他玩家身上的该束缚时，所需的时间
     * @param actionEntity 当前释放目标的实体,
     * @param target 被释放的目标实体,
     * @param stack 释放的拘束具物品,
     * @param bodyPart 释放的部位,
     */
    public long getReleaseTime(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart){
        if(actionEntity == null || actionEntity.getAttribute(ModAttributes.RESTRAINT_STRENGTH) == null) return DEFAULT_RELEASE_TIME;


        return (long) (DEFAULT_RELEASE_TIME /
                Objects.requireNonNull(actionEntity.getAttribute(ModAttributes.RESTRAINT_STRENGTH)).getValue());
    }

    /**
     * 其他玩家释放目标玩家身上的该束缚时，是否掉落挣脱的拘束具
     * @param actionEntity 当前正在释放目标的实体
     * @param target 被释放的目标生物,
     * @param stack 释放的拘束具物品,
     * @param bodyPart 释放的部位,
     * @param index 物品所在部位的索引序号
     */
    public boolean dropRestraintWhenRelease(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        return !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP);
    }

    /**
     * 其他玩家释放目标玩家身上的该拘束具时，若允许该物品掉落，如何掉落该物品
     * @param actionEntity 当前正在释放目标的实体,
     * @param target 被释放的目标生物,
     * @param stack 释放的拘束具物品,
     * @param bodyPart 释放的部位,
     * @param index 物品所在部位的索引序号
     */
    public ReleaseDropType dropReleaseItemDirection(LivingEntity actionEntity, LivingEntity target, ItemStack stack, PlayerRestraintPart bodyPart, int index){
        return ReleaseDropType.DROP_TO_ACTION_ENTITY;
    }

    /**
     * 当其他玩家为目标玩家解除该拘束具时触发的功能
     * @param actionEntity 当前正在释放目标的实体,
     * @param target 被释放的目标生物,
     * @param stack 释放的拘束具物品,
     * @param bodyPart 释放的部位,
     * @param index 物品所在部位的索引序号
     */
    public void onReleaseOff(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){

    }

    /* ---------------------------------------------- 渲染与GUI部分 --------------------------------------------------- */

    /**
     * 获取当前拘束具在对应部位的贴图
     * @param entity 目标实体
     * @param stack 拘束具ItemStack
     */
    public ResourceLocation getItemIconResourceLocation(LivingEntity entity,ItemStack stack) {

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());


        return ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(),
                "textures/item/restraints/" + itemKey.getPath() + ".png");

    }

    /**
     * 获取当前拘束具在对应部位的贴图
     * @param entity 目标实体
     * @param bodyPart 指定目标部位
     * @param stack 拘束具ItemStack
     */
    public ResourceLocation getTextureResourceLocation(LivingEntity entity, String bodyPart, ItemStack stack,int index,boolean isSlim) {

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemName = itemKey.getPath();

        if(Objects.equals(bodyPart, PlayerRestraintPart.restraint_arms_bind.toString())
            || Objects.equals(bodyPart, PlayerRestraintPart.restraint_hands_bind.toString())){
            if(isSlim){
                return ResourceLocation.fromNamespaceAndPath(MODID,
                        "textures/models/restraints/" + itemName + "/" + bodyPart + "/slim/" + itemName + ".png");
            }else{
                return ResourceLocation.fromNamespaceAndPath(MODID,
                        "textures/models/restraints/" + itemName + "/" + bodyPart + "/wide/" + itemName + ".png");
            }
        }else{
            return ResourceLocation.fromNamespaceAndPath(MODID,
                    "textures/models/restraints/" + itemName + "/" + bodyPart + "/" + itemName + ".png");
        }
    }

    public boolean shouldRestraintDraw(ItemStack stack,PlayerRestraintPart part,int index){
        return true;
    }

    public record restraintRenderData(
            PoseStack stack,
            RenderType renderType,
            VertexConsumer baseBuffer,
            int packedLight,
            int packedOverlay
    ){}

    /**
     * 拘束具渲染
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends HumanoidModel<T>> restraintRenderData renderRestraintLayer( T entity, PlayerRestraintPart part, int index, ItemStack stack,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,int packedOverlay) {

        //TODO:后续解决连接拘束具的渲染问题
        if(part == PlayerRestraintPart.restraint_connection && !shouldRenderConnectionBind(entity)) return null;

        if(!shouldRestraintDraw(stack,part,index)) return null;

        boolean isSlim = false;

        if(entity instanceof AbstractClientPlayer clientPlayer){
            isSlim = clientPlayer.getSkin().model() == PlayerSkin.Model.SLIM;
        }else if (entity instanceof BaseNPCEntity npc) {
            isSlim = npc.isSlimModel();
        }

        ResourceLocation texture = this.getTextureResourceLocation(entity, part.toString(), stack,index,isSlim);
        RenderType type = RenderType.armorCutoutNoCull(texture);
        VertexConsumer baseBuffer = bufferSource.getBuffer(type);


        if (entity instanceof Player player && shouldGagAndBlindfoldRenderOffset(part)
                && (part == PlayerRestraintPart.restraint_gag || part == PlayerRestraintPart.restraint_blindfold)) {


            float pixelAdjustment = getRenderOffset(player,part);

            final float vOffset = -pixelAdjustment / 64.0F;

            baseBuffer = getRenderOffsetVertexConsumer(baseBuffer,vOffset);

        }else if(entity instanceof BaseNPCEntity npc && shouldGagAndBlindfoldRenderOffset(part)
                && (part == PlayerRestraintPart.restraint_gag || part == PlayerRestraintPart.restraint_blindfold)){

            float vOffset;
            if(part == PlayerRestraintPart.restraint_blindfold){
                vOffset = npc.getNPCBlindfoldOffset();
            }else{
                vOffset = npc.getNPCGagOffset();
            }

            baseBuffer = getRenderOffsetVertexConsumer(baseBuffer,-vOffset / 64.0F);
        }

        return new restraintRenderData(poseStack,type,baseBuffer,packedLight,packedOverlay);
    }

    public <T extends LivingEntity> void setLayerInflation(HumanoidModel<T> model, BakedGeoModel geoModel,ItemStack stack,PlayerRestraintPart part,int index) {
        float base = 1.0F;
        if(model == null && geoModel != null){
            base = 1.05F;
        }
        float scale = base + (index * 0.005F);

        if (model == null && geoModel != null) {
            geoModel.getBone("head").get().setScaleX(scale);
            geoModel.getBone("head").get().setScaleY(scale);
            geoModel.getBone("head").get().setScaleZ(scale);

            geoModel.getBone("torso").get().setScaleX(scale);
            geoModel.getBone("torso").get().setScaleY(scale);
            geoModel.getBone("torso").get().setScaleZ(scale);

            geoModel.getBone("left_arm").get().setScaleX(scale);
            geoModel.getBone("left_arm").get().setScaleY(scale);
            geoModel.getBone("left_arm").get().setScaleZ(scale);

            geoModel.getBone("left_arm_bend").get().setScaleX(scale);
            geoModel.getBone("left_arm_bend").get().setScaleY(scale);
            geoModel.getBone("left_arm_bend").get().setScaleZ(scale);

            geoModel.getBone("right_arm").get().setScaleX(scale);
            geoModel.getBone("right_arm").get().setScaleY(scale);
            geoModel.getBone("right_arm").get().setScaleZ(scale);

            geoModel.getBone("right_arm_bend").get().setScaleX(scale);
            geoModel.getBone("right_arm_bend").get().setScaleY(scale);
            geoModel.getBone("right_arm_bend").get().setScaleZ(scale);

            geoModel.getBone("left_leg").get().setScaleX(scale);
            geoModel.getBone("left_leg").get().setScaleY(scale);
            geoModel.getBone("left_leg").get().setScaleZ(scale);

            geoModel.getBone("left_leg_bend").get().setScaleX(scale);
            geoModel.getBone("left_leg_bend").get().setScaleY(scale);
            geoModel.getBone("left_leg_bend").get().setScaleZ(scale);

            geoModel.getBone("right_leg").get().setScaleX(scale);
            geoModel.getBone("right_leg").get().setScaleY(scale);
            geoModel.getBone("right_leg").get().setScaleZ(scale);

            geoModel.getBone("right_leg_bend").get().setScaleX(scale);
            geoModel.getBone("right_leg_bend").get().setScaleY(scale);
            geoModel.getBone("right_leg_bend").get().setScaleZ(scale);

        }else if(model != null && geoModel == null){
            model.head.xScale = scale; model.head.yScale = scale; model.head.zScale = scale;
            model.body.xScale = scale; model.body.yScale = scale; model.body.zScale = scale;
            model.rightArm.xScale = scale; model.rightArm.yScale = scale; model.rightArm.zScale = scale;
            model.leftArm.xScale = scale; model.leftArm.yScale = scale; model.leftArm.zScale = scale;
            model.rightLeg.xScale = scale; model.rightLeg.yScale = scale; model.rightLeg.zScale = scale;
            model.leftLeg.xScale = scale; model.leftLeg.yScale = scale; model.leftLeg.zScale = scale;

            if (model instanceof PlayerModel<T> playerModel) {
                playerModel.hat.xScale = scale; playerModel.hat.yScale = scale; playerModel.hat.zScale = scale;
                playerModel.jacket.xScale = scale; playerModel.jacket.yScale = scale; playerModel.jacket.zScale = scale;
                playerModel.rightSleeve.xScale = scale; playerModel.rightSleeve.yScale = scale; playerModel.rightSleeve.zScale = scale;
                playerModel.leftSleeve.xScale = scale; playerModel.leftSleeve.yScale = scale; playerModel.leftSleeve.zScale = scale;
                playerModel.rightPants.xScale = scale; playerModel.rightPants.yScale = scale; playerModel.rightPants.zScale = scale;
                playerModel.leftPants.xScale = scale; playerModel.leftPants.yScale = scale; playerModel.leftPants.zScale = scale;
            }
        }
    }

    /**
     * 拘束具渲染的UV调整，主要针对眼罩和口塞的位置，通过直接移动UV的放置调整其上下位置，若无特殊需求不建议修改
     */
    @OnlyIn(Dist.CLIENT)
    public VertexConsumer getRenderOffsetVertexConsumer(VertexConsumer baseBuffer,float Offset) {

        float vOffset = -Mth.clamp(Offset,-4.0F,4.0f);

        return new VertexConsumer() {
            @Override
            public @NotNull VertexConsumer addVertex(float x, float y, float z) {
                baseBuffer.addVertex(x, y, z);
                return this;
            }

            @Override
            public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
                baseBuffer.setColor(r, g, b, a);
                return this;
            }

            @Override
            public @NotNull VertexConsumer setUv(float u, float v) {
                baseBuffer.setUv(u, v + vOffset);
                return this;
            }

            @Override
            public @NotNull VertexConsumer setUv1(int u, int v) {
                baseBuffer.setUv1(u, v);
                return this;
            }

            @Override
            public @NotNull VertexConsumer setUv2(int u, int v) {
                baseBuffer.setUv2(u, v);
                return this;
            }

            @Override
            public @NotNull VertexConsumer setNormal(float x, float y, float z) {
                baseBuffer.setNormal(x, y, z);
                return this;
            }
        };
    }

    /**
     * 控制该拘束具是否应该应用玩家自身定义的眼罩和口塞拘束具偏移
     */
    public boolean shouldGagAndBlindfoldRenderOffset(PlayerRestraintPart part){
        return true;
    }

    /**
     * 控制具体的模型部位渲染
     * 子类可以重写此方法来实现特殊的渲染需求
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends HumanoidModel<T>> void applyRestraintVisibility(
            M child, M parent,BakedGeoModel geoModel,ItemStack stack, PlayerRestraintPart part,int index, T entity) {


        if(child == null && parent == null && geoModel != null){

            if (part == PlayerRestraintPart.restraint_blindfold || part == PlayerRestraintPart.restraint_gag) {
                geoModel.getBone("head").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_collar) {
                geoModel.getBone("head").get().setHidden(false);
                geoModel.getBone("torso").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_body_bind) {
                geoModel.getBone("torso").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_arms_bind || part == PlayerRestraintPart.restraint_hands_bind) {
                geoModel.getBone("left_arm").get().setHidden(false);
                geoModel.getBone("right_arm").get().setHidden(false);
                geoModel.getBone("left_arm_bend").get().setHidden(false);
                geoModel.getBone("right_arm_bend").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_legs_bind) {
                geoModel.getBone("left_leg").get().setHidden(false);
                geoModel.getBone("right_leg").get().setHidden(false);
                geoModel.getBone("left_leg_bend").get().setHidden(false);
                geoModel.getBone("right_leg_bend").get().setHidden(false);
            } else {
                geoModel.getBone("head").get().setHidden(false);
                geoModel.getBone("torso").get().setHidden(false);
                geoModel.getBone("left_arm").get().setHidden(false);
                geoModel.getBone("right_arm").get().setHidden(false);
                geoModel.getBone("left_leg").get().setHidden(false);
                geoModel.getBone("right_leg").get().setHidden(false);
            }
            if (shouldRenderSecondLayer(child, parent,geoModel, part, entity)) {
                setSecondLayerVisibility(child, parent,geoModel, part, entity);
            }
        }else if (child != null && parent != null && geoModel == null){
            child.setAllVisible(false);

            if (part == PlayerRestraintPart.restraint_blindfold || part == PlayerRestraintPart.restraint_gag) {
                child.head.visible = parent.head.visible;
            } else if (part == PlayerRestraintPart.restraint_collar) {
                child.head.visible = parent.head.visible;
                child.body.visible = parent.body.visible;
            } else if (part == PlayerRestraintPart.restraint_body_bind) {
                child.body.visible = parent.body.visible;
            } else if (part == PlayerRestraintPart.restraint_arms_bind || part == PlayerRestraintPart.restraint_hands_bind) {
                child.leftArm.visible = parent.leftArm.visible;
                child.rightArm.visible = parent.rightArm.visible;
            } else if (part == PlayerRestraintPart.restraint_legs_bind) {
                child.leftLeg.visible = parent.leftLeg.visible;
                child.rightLeg.visible = parent.rightLeg.visible;
            } else {
                child.head.visible = parent.head.visible;
                child.body.visible = parent.body.visible;
                child.leftArm.visible = parent.leftArm.visible;
                child.rightArm.visible = parent.rightArm.visible;
                child.leftLeg.visible = parent.leftLeg.visible;
                child.rightLeg.visible = parent.rightLeg.visible;
            }

            if (shouldRenderSecondLayer(child, parent,geoModel, part, entity)) {
                setSecondLayerVisibility(child, parent,geoModel, part, entity);
            }
        }


    }


    /**
     * 总体控制开关，判断其是否应该渲染二层皮肤部分
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends HumanoidModel<T>> boolean shouldRenderSecondLayer(
            M child, M parent,BakedGeoModel geoModel, PlayerRestraintPart part, T entity) {
        return false;
    }

    /**
     * 控制具体的模型二层皮肤部位渲染，通常不渲染
     * 子类可以重写此方法来实现特殊的渲染需求
     */
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends HumanoidModel<T>> void setSecondLayerVisibility(
            M child, M parent,BakedGeoModel geoModel, PlayerRestraintPart part, T entity) {

        if(child == null && parent == null && geoModel != null){
            if (part == PlayerRestraintPart.restraint_blindfold || part == PlayerRestraintPart.restraint_gag) {
                geoModel.getBone("headwear").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_collar) {
                geoModel.getBone("headwear").get().setHidden(false);
                geoModel.getBone("jacket").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_body_bind) {
                geoModel.getBone("jacket").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_arms_bind || part == PlayerRestraintPart.restraint_hands_bind) {
                geoModel.getBone("left_arm_layer").get().setHidden(false);
                geoModel.getBone("left_arm_bend_layer").get().setHidden(false);
                geoModel.getBone("right_arm_layer").get().setHidden(false);
                geoModel.getBone("right_arm_bend_layer").get().setHidden(false);
            } else if (part == PlayerRestraintPart.restraint_legs_bind) {
                geoModel.getBone("left_leg_layer").get().setHidden(false);
                geoModel.getBone("left_leg_bend_layer").get().setHidden(false);
                geoModel.getBone("right_leg_layer").get().setHidden(false);
                geoModel.getBone("right_leg_bend_layer").get().setHidden(false);
            } else {
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
            }

        } else if (child != null && parent != null && geoModel == null) {
            if (child instanceof PlayerModel<?> playerChild && parent instanceof PlayerModel<?> playerParent) {

                playerChild.jacket.copyFrom(playerParent.body);
                playerChild.leftSleeve.copyFrom(playerParent.leftArm);
                playerChild.rightSleeve.copyFrom(playerParent.rightArm);
                playerChild.leftPants.copyFrom(playerParent.leftLeg);
                playerChild.rightPants.copyFrom(playerParent.rightLeg);

                if(entity instanceof Player player){
                    if (part == PlayerRestraintPart.restraint_blindfold || part == PlayerRestraintPart.restraint_gag) {
                        playerChild.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
                    } else if (part == PlayerRestraintPart.restraint_collar) {
                        playerChild.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
                        playerChild.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
                    } else if (part == PlayerRestraintPart.restraint_body_bind) {
                        playerChild.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
                    } else if (part == PlayerRestraintPart.restraint_arms_bind || part == PlayerRestraintPart.restraint_hands_bind) {
                        playerChild.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
                        playerChild.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
                    } else if (part == PlayerRestraintPart.restraint_legs_bind) {
                        playerChild.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
                        playerChild.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
                    } else {
                        playerChild.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
                        playerChild.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
                        playerChild.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
                        playerChild.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
                        playerChild.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
                        playerChild.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
                    }
                }else{
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

    /**
     * 当该拘束具为眼罩拘束具时，拘束具的眼罩效果在玩家视角下的渲染
     * @param playerUUID 目标玩家的UUID,
     * @param guiGraphics 目标玩家渲染的GuiGraphics,
     * @param stack 该拘束具的ItemStack
     */
    @OnlyIn(Dist.CLIENT)
    public void renderBlindfoldOverlay(UUID playerUUID, GuiGraphics guiGraphics, ItemStack stack) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID,
                "textures/models/blindfold_overlay/" + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".png");

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        float alpha = 1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        guiGraphics.blit(texture, 0, 0, 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    /**
     * 获取该眼罩需要隐藏的 GUI 图层列表
     */
    public List<ResourceLocation> hideGuiOverlayList(UUID playerUUID) {
        List<ResourceLocation> list = new ArrayList<>();

        list.add(VanillaGuiLayers.CAMERA_OVERLAYS);
        list.add(VanillaGuiLayers.BOSS_OVERLAY);
        list.add(VanillaGuiLayers.SLEEP_OVERLAY);

        list.add(VanillaGuiLayers.CROSSHAIR);
        list.add(VanillaGuiLayers.HOTBAR);
        list.add(VanillaGuiLayers.SELECTED_ITEM_NAME);
        list.add(VanillaGuiLayers.EXPERIENCE_BAR);
        list.add(VanillaGuiLayers.EXPERIENCE_LEVEL);
        list.add(VanillaGuiLayers.JUMP_METER);
        list.add(VanillaGuiLayers.VEHICLE_HEALTH);
        return list;
    }
}
