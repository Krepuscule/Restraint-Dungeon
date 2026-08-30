package com.twi.restraint_dungeon.event.mod_event.restraint;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.client.gui.RestraintInfoMenu;
import com.twi.restraint_dungeon.client.hud.struggle_hud.StruggleHUDManager;
import com.twi.restraint_dungeon.client.keybind.ModKeyBinds;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.custom_event.RestraintEquipEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintPositionChangeEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintUpdateEvent;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.event.system_handler_event.LivingEntityServerTaskScheduler;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.network.payload.player_restraint.PositionClientRefreshPayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.InterruptStrugglePayload;
import com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import com.twi.restraint_dungeon.utils.mod_utils.self_bondage.SelfBondageUtils;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils.isDoingAction;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.kidnap.KidnapUtils.clearKidnapData;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.calPlayerThrillLevel;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.updateThrillLevel;
import static com.twi.restraint_dungeon.utils.mod_utils.release.ReleaseUtils.clearReleaseData;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.GagUtils.createGagChatMessage;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.*;

@EventBusSubscriber(modid = MODID)
public class RestraintEvent {

    /**
     * 拘束状态下的使用指令白名单
     */
    private static final List<String> COMMAND_WHITE_LIST = Arrays.asList(
            "me",
            "msg",
            "kill",
            "restraint"
    );

    /**
     * 监听玩家的拘束具变化并更新属性值
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRestraintChange(RestraintUpdateEvent event) {

        LivingEntity entity = event.getEntity();
        PlayerRestraintPart part = event.getPart();
        ItemStack oldStack = event.getOldStack();
        ItemStack newStack = event.getNewStack();
        if (!(entity instanceof ServerPlayer) && !(entity instanceof BaseNPCEntity)) return;

        if(getIsStruggling(entity)) {
            if(entity instanceof ServerPlayer player){
                PacketDistributor.sendToPlayer(player, new InterruptStrugglePayload());
            }
        }

        if(part == PlayerRestraintPart.restraint_connection
                && newStack.getItem() instanceof RestraintItem restraintItem && oldStack.isEmpty()
                && restraintItem.getConnectBindPreviousPosition(entity) != null
                && !getFirstConnectBind(entity).isEmpty()){

            RestraintPosition pos = getRestraintPosition(entity);

            NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Pre(entity,pos, RestraintPosition.CONNECTING,newStack));
            updateRestraintPosition(entity,RestraintPosition.CONNECTING);
            NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Post(entity,pos, RestraintPosition.CONNECTING,newStack));
        }else if(part == PlayerRestraintPart.restraint_connection
                && oldStack.getItem() instanceof RestraintItem restraintItem && newStack.isEmpty()
                && restraintItem.getConnectBindPreviousPosition(entity) != null
                && getFirstConnectBind(entity).isEmpty()){

            RestraintPosition pos = restraintItem.getConnectBindPreviousPosition(entity);

            NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Pre(entity,RestraintPosition.CONNECTING,pos,oldStack));
            updateRestraintPosition(entity,restraintItem.getConnectBindPreviousPosition(entity));
            NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Post(entity,RestraintPosition.CONNECTING,pos,oldStack));
        }

        updateThrillValue(entity);

        updatePoseByRestraint(entity);

        if(entity instanceof ServerPlayer player){
            refreshPlayerNameTag(player);
        }


    }

    /**
     * 重新计算并更新敏感等级
     */
    private static void updateThrillValue(LivingEntity entity) {
        int newThrill = calPlayerThrillLevel(entity);
        updateThrillLevel(entity, newThrill);
    }

    /**
     * 刷新玩家的显示名称（同步给所有追踪该玩家的客户端）
     */
    private static void refreshPlayerNameTag(ServerPlayer player) {
        player.refreshDisplayName();

        player.server.getPlayerList().broadcastAll(
                new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                        player
                )
        );

    }

    /**
     * 拘束状态下禁用攻击能力
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRestraint_DisableAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (isBeenBindArms(player) || isBeenBindHands(player)) {
            if (!event.isCanceled()) event.setCanceled(true);
        }
    }

    /**
     * 堵嘴聊天信息处理
     */
    @SubscribeEvent
    public static void onGag_ChatMessage(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (isBeenGag(player)) {
            Component gagContent = createGagChatMessage(event.getMessage(), player);
            event.setMessage(gagContent);
        }
    }

    /**
     * 使用拘束具蒙眼时的OverLay渲染
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlindfoldOverlay(RenderGuiEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (isBeenBlindfold(player)) {
            if(isRidingRestraintDevice(player)){
                if(getRestraintDevice(player) instanceof RestraintDevice device){
                    RestraintDeviceUtils.DeviceContext context = getRestraintDeviceContext(player);

                    if(context != null && device.canBlindfold(context.state(),context.pos())){
                        device.renderDeviceBlindfold(player,device,context.state(),context.pos(),event.getGuiGraphics());
                    }
                }
            }
            renderFullScreenOverlay(player, event.getGuiGraphics());
        }
    }

    /**
     * 蒙眼隐藏特定 GUI 元素 (血条、物品栏等)
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onBlindfoldGuiHide(RenderGuiLayerEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (isBeenBlindfold(player)) {
            ItemStack blindfold = getFirstBlindfold(player);
            if (blindfold.getItem() instanceof RestraintItem item) {
                if (item.hideGuiOverlayList(player.getUUID()).contains(event.getName())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * 束缚手臂时隐藏第一人称手部渲染
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (isBeenBindArms(player) || isDoingAction(player)) {
            event.setCanceled(true);
        }
    }

    /**
     * 玩家显示项圈名称
     */
    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        Player player = event.getEntity();
        ItemStack collarStack = getFirstCollar(player);

        if (!collarStack.isEmpty() && collarStack.getItem() instanceof RestraintItem restraintItem) {
            MutableComponent finalName = restraintItem.getAdditionCollarName(player, collarStack, event.getDisplayname());
            event.setDisplayname(finalName);
        }
    }

    /**
     * 渲染玩家头顶名字标签
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.refreshDisplayName();
        }
    }

    /**
     * 指令拦截
     */
    @SubscribeEvent
    public static void onRestraintCommand(CommandEvent event) {
        if (event.getParseResults().getContext().getSource().getEntity() instanceof Player player) {
            if (isBeenBindArms(player) || isBeenBindLegs(player)) {
                String commandName = event.getParseResults().getContext().getNodes().get(0).getNode().getName();
                if (!COMMAND_WHITE_LIST.contains(commandName.toLowerCase()) && !player.isCreative()) {
                    event.setCanceled(true);
                    player.displayClientMessage(
                            Component.translatable("event.restraint_dungeon.restraint.cant_use_command")
                                    .withStyle(ChatFormatting.DARK_RED), true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (isBeenBindArms(player) || isBeenBindHands(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (isBeenBindArms(player) || isBeenBindHands(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        if (isBeenBindArms(player) || isBeenBindHands(player)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {

        if (isBeenBindArms(event.getEntity()) || isBeenBindHands(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (isBeenBindArms(player) || isBeenBindHands(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isBeenBindArms(event.getEntity()) || isBeenBindHands(event.getEntity())) {
            if (!event.isCanceled()) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * 开启拘束信息菜单（默认为H键）
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (ModKeyBinds.RESTRAINT_MENU.consumeClick()) {
            LivingEntity target = mc.player;

            if (mc.player.isShiftKeyDown() && mc.hitResult instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof LivingEntity living) {
                    target = living;
                }
            }

            if(target == mc.player){
                mc.setScreen(new RestraintInfoMenu(target,true,false));
            }else if(target instanceof Player player ){
                mc.setScreen(new RestraintInfoMenu(target,false,false));
            }else if(target instanceof BaseNPCEntity npc){
                mc.setScreen(new RestraintInfoMenu(target,false,true));
            }

        }
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();

        if (!player.level().isClientSide) {
            clearRestraintAttachments(player);
            var cap = player.getData(ModAttachments.RESTRAINT_STACK);
            player.setData(ModAttachments.RESTRAINT_STACK, cap);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player,
                    new PositionClientRefreshPayload(player.getId())
            );
        }
    }

    /**
     * 玩家离线时，清除相关中间状态信息
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide) {
            clearRestraintAttachments(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if(event.getEntity() instanceof Player player){
            clearRestraintAttachments(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var oldPlayer = event.getOriginal();
        var newPlayer = event.getEntity();

        if (event.isWasDeath()) {
            var oldCap = oldPlayer.getData(ModAttachments.RESTRAINT_STACK);
            newPlayer.setData(ModAttachments.RESTRAINT_STACK, oldCap);
            if(getRestraintPosition(oldPlayer) == RestraintPosition.CONNECTING
                    && !getFirstConnectBind(newPlayer).isEmpty()){
                updateRestraintPosition(newPlayer,RestraintPosition.CONNECTING);
            }
        } else {

        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        var player = event.getEntity();

        if (!player.level().isClientSide()) {
            RestraintStackUtils.rebalanceAllRestraintsAfterDeath(player);
            updateThrillValue(player);

            updatePoseByRestraint(player);


            refreshPlayerNameTag((ServerPlayer) player);

        }
    }

    @SubscribeEvent
    public static void onStartTrack(PlayerEvent.StartTracking event){
        Player player = event.getEntity();
        Entity target = event.getTarget();
        if(target instanceof LivingEntity living){
            if(getRestraintPosition(living) != RestraintPosition.STANDING){
                PacketDistributor.sendToPlayer(
                        (ServerPlayer) player,
                        new PositionClientRefreshPayload(living.getId())
                );
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof Player player) {

            if (player instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.connection == null) {
                    return;
                }
            }

            RestraintPosition position = getRestraintPosition(player);

            if(position == RestraintPosition.KNEELING){
                event.setNewSize(EntityDimensions.scalable(0.6F, 1.5F));
            }
            else if(position == RestraintPosition.SITTING){
                event.setNewSize(EntityDimensions.scalable(0.6F, 1.2F));
            }
            else if(position == RestraintPosition.LYING_UP){
                event.setNewSize(EntityDimensions.scalable(1.25F, 0.5F));
            }
            else if(position == RestraintPosition.LYING_LEFT){
                event.setNewSize(EntityDimensions.scalable(1.25F, 0.5F));
            }
            else if(position == RestraintPosition.LYING_RIGHT){
                event.setNewSize(EntityDimensions.scalable(1.25F, 0.5F));
            }
            else if(position == RestraintPosition.LYING_DOWN){
                event.setNewSize(EntityDimensions.scalable(1.25F, 0.5F));
            }

            else if(position == RestraintPosition.CONNECTING){
                ItemStack stack = getFirstConnectBind(player);
                if(stack.getItem() instanceof RestraintItem ri){
                    List<Float> list = ri.getConnectBindEntityDimensions(player,stack);
                    event.setNewSize(EntityDimensions.scalable(list.getFirst(),list.getLast()));
                }
            }
            else if(position == RestraintPosition.CARRIED){
                CarryType type = getCarryType(getCarryState(player));
                if(isCarrier(player)){
                    LivingEntity entity = getCarriedPassenger(player);
                    List<Float> list = type.getCarrierEntityDimensions(player,entity);
                    event.setNewSize(EntityDimensions.scalable(list.getFirst(),list.getLast()));
                }else{
                    Player carrier = getCarrier(player);
                    List<Float> list = type.getPassengerEntityDimensions(carrier,player);
                    event.setNewSize(EntityDimensions.scalable(list.getFirst(),list.getLast()));
                }
            }

        }
    }

    @SubscribeEvent
    public static void onPositionChange(RestraintPositionChangeEvent.Post event){
        LivingEntity entity = event.getEntity();

        if (!entity.level().isClientSide()) {
            entity.refreshDimensions();

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    entity,
                    new PositionClientRefreshPayload(entity.getId())
            );
        }
    }

    private static void clearRestraintAttachments(Player player) {

        // 拘束装置相关
        if(isRidingRestraintDevice(player)){
            DeviceContext deviceContext = getRestraintDeviceContext(player);
            if (deviceContext != null && deviceContext.device() != null) {
                deviceContext.device().forceDismount(player.level(), deviceContext.pos(), player);
            }
        }


        // 拘束属性相关
        setChangingPosition(player, false);
        if(getRestraintPosition(player) == RestraintPosition.CARRIED
                || getRestraintPosition(player) == RestraintPosition.RIDING){
            updateRestraintPosition(player,RestraintPosition.STANDING);
        }

        // 挣扎属性相关
        updateStruggleMode(player, StruggleMode.NONE);
        updateIsStruggling(player, false);
        updateStruggleProgress(player,0.0f);

        // 绑架属性相关
        clearKidnapData(player);
        SelfBondageUtils.clearData(player);

        // 释放属性相关
        clearReleaseData(player);

        // 抱起玩家属性相关
        clearCarryData(player);

        // 动作属性相关
        PlayerActionUtils.reset(player);
    }
}

