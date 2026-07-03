package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.interact_event;

import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.block.addon_block.placed_sword.PlacedSwordBlock;
import com.twi.restraint_dungeon.block.addon_block.placed_sword.PlacedSwordBlockEntity;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

@EventBusSubscriber(modid = MODID)
public class SwordInsertEvent implements IInteractHandler {
    private static final SwordInsertEvent INSTANCE = new SwordInsertEvent();

    public static SwordInsertEvent getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isTarget(BlockState state) { return state.is(Blocks.CRACKED_STONE_BRICKS); }

    @Override
    public boolean canDo(Player player) {
        return !isBeenBindHands(player) && player.getMainHandItem().getItem() instanceof SwordItem;
    }

    @Override
    public double getInteractRange(Player player, BlockState state) { return 5.5D; }

    @Override
    public int getInteractTime(Player player, BlockState state) { return 100; }

    @Override
    public void onComplete(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        BlockState targetState = player.level().getBlockState(pos);
        if (!isTarget(targetState)) return;

        ItemStack swordStack = player.getItemInHand(hand);
        if (!(swordStack.getItem() instanceof SwordItem)) return;

        InteractTargetSelector.TargetResult result = InteractTargetSelector.getTarget(player, getInteractRange(player, targetState) + 0.5D);
        InteractTargetSelector.TargetContext context = (result != null) ? result.block() : null;

        if (context != null && context.pos().equals(pos) && context.face().getAxis().isHorizontal()) {
            Direction face = context.face();
            BlockPos swordPos = pos.relative(face);

            if (player.level().getBlockState(swordPos).isAir()) {
                player.level().setBlock(swordPos, ModBlocks.PLACED_SWORD.get().defaultBlockState()
                        .setValue(PlacedSwordBlock.FACING, face), 3);

                if (player.level().getBlockEntity(swordPos) instanceof PlacedSwordBlockEntity be) {
                    be.setSword(swordStack.copy(), player.getUUID());
                    playInsertSound(player, swordPos, swordStack);
                    if (!player.getAbilities().instabuild) swordStack.shrink(1);
                }
                player.swing(hand, true);
                InteractingProgressManager.setCooldown(player.getUUID(), 20);
            }
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
            ItemStack stack = p.getItemInHand(event.getHand());
            if (stack.getItem() instanceof SwordItem && target.face().getAxis().isHorizontal()) {
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

                InteractingProgressManager.startOrUpdate(p, target.pos(), null, event.getHand(), time, range, (player) -> {
                    if (!player.level().isClientSide) INSTANCE.onComplete(player, target.pos(), event.getHand());
                });
            }
        }
    }

    private static void playInsertSound(ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItem sword)) return;
        Tier tier = sword.getTier();
        SoundEvent sound;
        if(tier == Tiers.WOOD) sound = SoundEvents.WOOD_PLACE;
        else if(tier == Tiers.STONE) sound = SoundEvents.STONE_PLACE;
        else if(tier == Tiers.IRON || tier == Tiers.GOLD || tier == Tiers.DIAMOND) sound = SoundEvents.METAL_PLACE;
        else if(tier == Tiers.NETHERITE) sound = SoundEvents.NETHERITE_BLOCK_PLACE;
        else sound = SoundEvents.METAL_PLACE;

        player.level().playSound(null, pos, sound, SoundSource.BLOCKS, 0.6f, 1.0f);
        player.level().playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.5f, 1.2f);
    }
}