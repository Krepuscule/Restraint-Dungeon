package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.interact_event;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.IInteractHandler;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractTargetSelector;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractingProgressManager;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerStartBackInteractPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
public class RedstoneInteractEvent implements IInteractHandler {
    private static final RedstoneInteractEvent INSTANCE = new RedstoneInteractEvent();

    public static RedstoneInteractEvent getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isTarget(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.IRON_DOOR || block == Blocks.IRON_TRAPDOOR) return false;
        return block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock
                || (block instanceof ButtonBlock && !state.getValue(BlockStateProperties.POWERED))
                || block instanceof LeverBlock;
    }

    @Override
    public boolean canDo(Player player) {
        return isBeenBindArms(player) && !isBeenBindHands(player) && player.getMainHandItem().isEmpty();
    }

    @Override
    public double getInteractRange(Player player, BlockState state) { return 2.0D; }

    @Override
    public int getInteractTime(Player player, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof ButtonBlock) return 10;
        if (block instanceof LeverBlock) return 20;
        return 60;
    }

    @Override
    public void onComplete(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        BlockState state = player.level().getBlockState(pos);
        if (!isTarget(state)) return;

        InteractionResult result = state.useWithoutItem(player.level(), player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        if (result.consumesAction()) {
            player.swing(hand, true);
            playRedstoneSound(player, pos);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) { handle(event); }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractItem(PlayerInteractEvent.RightClickItem event) { handle(event); }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractEmpty(PlayerInteractEvent.RightClickEmpty event) { handle(event); }

    private static void handle(PlayerInteractEvent event) {
        Player p = event.getEntity();
        if (InteractingProgressManager.isInCooldown(p.getUUID())) return;

        InteractTargetSelector.TargetResult result = InteractTargetSelector.getTarget(p, INSTANCE.getInteractRange(p, null) + 0.5D);
        InteractTargetSelector.TargetContext target = (result != null) ? result.block() : null;

        if (target != null && INSTANCE.isTarget(target.state())) {
            if (!INSTANCE.canDo(p)) return;

            double range = INSTANCE.getInteractRange(p, target.state());
            int time = INSTANCE.getInteractTime(p, target.state());

            if (p.level().isClientSide) {
                if (InteractTargetSelector.isBackMode(p) || event instanceof PlayerInteractEvent.RightClickEmpty) {
                    PacketDistributor.sendToServer(new PlayerStartBackInteractPayload(target.pos(), event.getHand()));
                }
            }

            if (event instanceof PlayerInteractEvent.RightClickBlock rb) {
                rb.setCanceled(true);
                rb.setCancellationResult(InteractionResult.SUCCESS);
            } else if (event instanceof PlayerInteractEvent.RightClickItem ri) {
                ri.setCanceled(true);
                ri.setCancellationResult(InteractionResult.SUCCESS);
            }

            InteractingProgressManager.startOrUpdate(p, target.pos(), null, event.getHand(), time, range, (player) -> {});
        }
    }

    private static void playRedstoneSound(ServerPlayer player, BlockPos pos) {
        BlockState newState = player.level().getBlockState(pos);
        Block block = newState.getBlock();
        boolean isOpen = false;

        if (newState.hasProperty(BlockStateProperties.OPEN)) isOpen = newState.getValue(BlockStateProperties.OPEN);
        else if (newState.hasProperty(BlockStateProperties.POWERED)) isOpen = newState.getValue(BlockStateProperties.POWERED);

        SoundEvent sound = null;
        float pitch = isOpen ? 1.0F : 0.8F;

        if (block instanceof DoorBlock) sound = isOpen ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE;
        else if (block instanceof TrapDoorBlock) sound = isOpen ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE;
        else if (block instanceof FenceGateBlock) sound = isOpen ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE;
        else if (block instanceof ButtonBlock) {
            sound = newState.is(BlockTags.WOODEN_BUTTONS) ?
                (isOpen ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF) : 
                (isOpen ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF);
        } else if (block instanceof LeverBlock) { sound = SoundEvents.LEVER_CLICK; pitch = isOpen ? 0.6F : 0.5F; }

        if (sound != null) player.level().playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, pitch);
    }
}