package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.interact_event;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.IInteractHandler;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractTargetSelector;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractingProgressManager;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerStartBackInteractPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
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
public class LecternInteractEvent implements IInteractHandler {
    private static final LecternInteractEvent INSTANCE = new LecternInteractEvent();

    public static LecternInteractEvent getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isTarget(BlockState state) {
        return state.is(Blocks.LECTERN);
    }

    @Override
    public boolean canDo(Player player) {
        return isBeenBindArms(player) && !isBeenBindHands(player);
    }

    @Override
    public double getInteractRange(Player player, BlockState state) {
        return 2.0D;
    }

    @Override
    public int getInteractTime(Player player, BlockState state) {
        return (state.hasProperty(LecternBlock.HAS_BOOK) && state.getValue(LecternBlock.HAS_BOOK)) ? 80 : 60;
    }

    @Override
    public void onComplete(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        BlockState state = player.level().getBlockState(pos);
        if (!isTarget(state)) return;

        boolean hasBook = state.getValue(LecternBlock.HAS_BOOK);
        ItemStack handItem = player.getItemInHand(hand);

        if (hasBook && handItem.isEmpty()) {
            state.useWithoutItem(player.level(), player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        } else if (!hasBook && (handItem.is(Items.WRITABLE_BOOK) || handItem.is(Items.WRITTEN_BOOK))) {
            LecternBlock.tryPlaceBook(player, player.level(), pos, state, handItem);
            player.level().playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        player.swing(hand, true);
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

            BlockState state = target.state();
            InteractionHand hand = event.getHand();
            ItemStack handItem = p.getItemInHand(hand);
            boolean hasBook = state.getValue(LecternBlock.HAS_BOOK);

            boolean canRead = handItem.isEmpty() && hasBook;
            boolean canPlace = (handItem.is(Items.WRITABLE_BOOK) || handItem.is(Items.WRITTEN_BOOK)) && !hasBook;

            if (canRead || canPlace) {
                double range = INSTANCE.getInteractRange(p, state);
                int time = INSTANCE.getInteractTime(p, state);

                if (p.level().isClientSide) {
                    if (InteractTargetSelector.isBackMode(p) || event instanceof PlayerInteractEvent.RightClickEmpty) {
                        PacketDistributor.sendToServer(new PlayerStartBackInteractPayload(target.pos(), hand));
                    }
                }

                if (event instanceof PlayerInteractEvent.RightClickBlock rb) {
                    rb.setCanceled(true);
                    rb.setCancellationResult(InteractionResult.SUCCESS);
                } else if (event instanceof PlayerInteractEvent.RightClickItem ri) {
                    ri.setCanceled(true);
                    ri.setCancellationResult(InteractionResult.SUCCESS);
                }

                InteractingProgressManager.startOrUpdate(p, target.pos(), null, hand, time, range, (player) -> {});
            }
        }
    }
}