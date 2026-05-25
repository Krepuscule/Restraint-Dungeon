package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.interact_event;

import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.block.addon_block.placed_sword.PlacedSwordBlockEntity;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.IInteractHandler;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractTargetSelector;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractingProgressManager;
import com.twi.restraint_dungeon.network.payload.player_restraint.PlayerStartBackInteractPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

@EventBusSubscriber(modid = MODID)
public class SwordTakeEvent implements IInteractHandler {
    private static final SwordTakeEvent INSTANCE = new SwordTakeEvent();

    public static SwordTakeEvent getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isTarget(BlockState state) { return state.is(ModBlocks.PLACED_SWORD.get()); }

    @Override
    public boolean canDo(Player player) { return !isBeenBindHands(player) && player.getMainHandItem().isEmpty(); }

    @Override
    public double getInteractRange(Player player, BlockState state) { return 4.5D; }

    @Override
    public int getInteractTime(Player player, BlockState state) { return 60; }

    @Override
    public void onComplete(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        Level level = player.level();
        if (!isTarget(level.getBlockState(pos))) return;

        if (level.getBlockEntity(pos) instanceof PlacedSwordBlockEntity be) {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.8F, 1.5F);
            player.swing(hand, true);
            InteractingProgressManager.setCooldown(player.getUUID(), 15);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) { handle(event); }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractEmpty(PlayerInteractEvent.RightClickEmpty event) { handle(event); }

    private static void handle(PlayerInteractEvent event) {
        Player p = event.getEntity();
        if (InteractingProgressManager.isInCooldown(p.getUUID())) return;
        if (!INSTANCE.canDo(p)) return;

        InteractTargetSelector.TargetResult result = InteractTargetSelector.getTarget(p, INSTANCE.getInteractRange(p, null) + 0.5D);
        InteractTargetSelector.TargetContext target = (result != null) ? result.block() : null;

        if (target != null && INSTANCE.isTarget(target.state())) {
            InteractionHand hand = event.getHand();
            double range = INSTANCE.getInteractRange(p, target.state());

            if (p.level().isClientSide) {
                if (InteractTargetSelector.isBackMode(p) || event instanceof PlayerInteractEvent.RightClickEmpty) {
                    PacketDistributor.sendToServer(new PlayerStartBackInteractPayload(target.pos(), hand));
                }
            }

            if (event instanceof PlayerInteractEvent.RightClickBlock rb) {
                rb.setCanceled(true);
                rb.setCancellationResult(InteractionResult.SUCCESS);
            }

            InteractingProgressManager.startOrUpdate(p, target.pos(), null, hand, INSTANCE.getInteractTime(p, target.state()), range, (player) -> {
                if (!player.level().isClientSide) INSTANCE.onComplete(player, target.pos(), hand);
            });
        }
    }
}