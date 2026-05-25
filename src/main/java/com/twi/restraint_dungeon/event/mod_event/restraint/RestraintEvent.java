package com.twi.restraint_dungeon.event.mod_event.restraint;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import com.twi.restraint_dungeon.client.gui.RestraintInfoMenu;
import com.twi.restraint_dungeon.client.keybind.ModKeyBinds;
import com.twi.restraint_dungeon.event.custom_event.RestraintChangeEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintEquipEvent;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.mod_utils.action.PlayerActionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Arrays;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.clearCarryData;
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
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRestraintEquip(RestraintEquipEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof ServerPlayer player)) return;

        PlayerRestraintPart part = event.getPart();
        ItemStack stack = event.getStack();

        updateThrillValue(player);

        updatePoseByRestraint(player);

        refreshPlayerNameTag(player);

    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRestraintChange(RestraintChangeEvent event) {

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;

        updateThrillValue(player);

        updatePoseByRestraint(player);

        refreshPlayerNameTag(player);


    }

    /**
     * 重新计算并更新敏感等级
     */
    private static void updateThrillValue(ServerPlayer player) {
        int newThrill = calPlayerThrillLevel(player);
        updateThrillLevel(player, newThrill);
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
    @SubscribeEvent
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
        if (isBeenBindArms(player)) {
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
            MutableComponent finalName = restraintItem.getAdditionCollarName(player, collarStack);
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
            ItemStack collarStack = getFirstCollar(player);

            if (!collarStack.isEmpty() && collarStack.getItem() instanceof RestraintItem restraintItem) {
                MutableComponent finalName = restraintItem.getAdditionCollarNameTag(player, collarStack);
                event.setContent(finalName);
            }
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
            // 服务端逻辑拦截
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
        // 检查束缚状态
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
            LivingEntity target = mc.player; // 默认目标是自己

            // 如果按住 Shift 且指向了实体
            if (mc.player.isShiftKeyDown() && mc.hitResult instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof LivingEntity living) {
                    target = living;
                }
            }

            mc.setScreen(new RestraintInfoMenu(target));
        }
    }

    /**
     * 玩家离线时，清除相关中间状态信息
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide) {
            clearRestraintAttachments(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if(event.getEntity() instanceof Player player){
            clearRestraintAttachments(player);
        }
    }

    //TODO: 玩家碰撞箱修改
//    @SubscribeEvent
//    public static void onPlayerSize(EntityEvent.Size event) {
//        if (event.getEntity() instanceof Player player) {
//
//            if (isCarrier(player)) {
//                event.setNewSize(EntityDimensions.scalable(1.2F, 1.8F));
//            }
//
//        }
//    }

    private static void clearRestraintAttachments(Player player) {
        // 拘束属性相关
        setChangingPosition(player, false);

        // 挣扎属性相关
        updateStruggleMode(player, StruggleMode.NONE);
        updateIsStruggling(player, false);
        updateStruggleProgress(player,0.0f);

        // 拘束属性相关
        clearKidnapData(player);

        // 释放属性相关
        clearReleaseData(player);

        // 抱起玩家属性相关
        clearCarryData(player);

        // 动作属性相关
        PlayerActionUtils.reset(player);
    }
}

