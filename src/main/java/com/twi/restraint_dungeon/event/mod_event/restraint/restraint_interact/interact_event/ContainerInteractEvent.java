package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.interact_event;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.IInteractHandler;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractTargetSelector;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractingProgressManager;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerStartBackInteractPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindArms;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

@EventBusSubscriber(modid = MODID)
public class ContainerInteractEvent implements IInteractHandler {

    private static final ContainerInteractEvent INSTANCE = new ContainerInteractEvent();

    public static ContainerInteractEvent getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isTarget(BlockState state) {
        Block block = state.getBlock();
        return block instanceof ChestBlock
                || block instanceof BarrelBlock
                || block instanceof EnderChestBlock
                || block instanceof ShulkerBoxBlock;
    }

    @Override
    public boolean canDo(Player player) {
        return isBeenBindArms(player) && !isBeenBindHands(player) && player.getMainHandItem().isEmpty();
    }

    @Override
    public double getInteractRange(Player player, BlockState state) {
        return 2.0D;
    }

    @Override
    public int getInteractTime(Player player, BlockState state) {
        return 120;
    }

    @Override
    public void onComplete(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        BlockState state = player.level().getBlockState(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

        InteractionResult result = state.useWithoutItem(player.level(), player, hit);
        if (result.consumesAction()) {
            player.swing(hand, true);
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        handle(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractItem(PlayerInteractEvent.RightClickItem event) {
        handle(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractEmpty(PlayerInteractEvent.RightClickEmpty event) {
        handle(event);
    }

    private static void handle(PlayerInteractEvent event) {
        Player p = event.getEntity();
        if (InteractingProgressManager.isInCooldown(p.getUUID())) return;

        // 获取目标判定
        InteractTargetSelector.TargetResult result = InteractTargetSelector.getTarget(p,
                INSTANCE.getInteractRange(p, null) + 0.5D);

        InteractTargetSelector.TargetContext target = (result != null) ? result.block() : null;

        if (target != null && INSTANCE.isTarget(target.state())) {
            // 权限检查
            if (!INSTANCE.canDo(p)) return;

            // 获取动态参数
            double range = INSTANCE.getInteractRange(p, target.state());
            int time = INSTANCE.getInteractTime(p, target.state());

            // 客户端逻辑处理
            if (p.level().isClientSide) {
                if (InteractTargetSelector.isBackMode(p) || event instanceof PlayerInteractEvent.RightClickEmpty) {
                    PacketDistributor.sendToServer(new PlayerStartBackInteractPayload(target.pos(), event.getHand()));
                }
            }


            if (event instanceof PlayerInteractEvent.RightClickBlock rb) {
                rb.setCanceled(true);
                rb.setCancellationResult(InteractionResult.SUCCESS);
            }

            else if (event instanceof PlayerInteractEvent.RightClickItem ri) {
                ri.setCanceled(true);
                ri.setCancellationResult(InteractionResult.SUCCESS);
            }

            else if (event instanceof PlayerInteractEvent.RightClickEmpty) {

                if (p.level().isClientSide && InteractTargetSelector.isBackMode(p)) {
                    PacketDistributor.sendToServer(new PlayerStartBackInteractPayload(target.pos(), event.getHand()));
                }
            }

            if (p.level().isClientSide && InteractTargetSelector.isBackMode(p)) {
                if (!(event instanceof PlayerInteractEvent.RightClickEmpty)) {
                    PacketDistributor.sendToServer(new PlayerStartBackInteractPayload(target.pos(), event.getHand()));
                }
            }

            InteractingProgressManager.startOrUpdate(p, target.pos(), null, event.getHand(), time, range, (player) -> {

            });
        }
    }
}