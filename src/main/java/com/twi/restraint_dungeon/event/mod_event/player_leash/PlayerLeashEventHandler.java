package com.twi.restraint_dungeon.event.mod_event.player_leash;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerLeashData;
import com.twi.restraint_dungeon.utils.mod_utils.leash.PlayerLeashUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.server.command.ModIdArgument;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber
public class PlayerLeashEventHandler {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player targetPlayer)) return;

        Player leasher = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack handItem = event.getItemStack();
        Level level = targetPlayer.level();

        if (hand == InteractionHand.OFF_HAND &&
                leasher.getItemInHand(InteractionHand.MAIN_HAND).is(Items.LEAD)) {
            return;
        }

        PlayerLeashData targetData = targetPlayer.getData(ModAttachments.PLAYER_LEASH.get());

        if (handItem.is(Items.LEAD) && !targetData.hasLeash()) {
            Component denyMessage = PlayerLeashUtils.canBeLeashed(targetPlayer, leasher);
            if (denyMessage != null) {
                if (level.isClientSide) {
                    leasher.displayClientMessage(denyMessage, true);
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                event.setCanceled(true);
                return;
            }

            if (!level.isClientSide) {
                ServerLevel serverLevel = (ServerLevel) level;
                targetData.setLeasherUUID(leasher.getUUID());
                targetPlayer.setData(ModAttachments.PLAYER_LEASH.get(), targetData);

                if (!leasher.getAbilities().instabuild) {
                    handItem.shrink(1);
                }
                serverLevel.getChunkSource().broadcast(targetPlayer, new ClientboundSetEntityLinkPacket(targetPlayer, leasher));
            }

            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
            return;
        }

        if (targetData.hasLeash() && leasher.getUUID().equals(targetData.getLeasherUUID())) {
            if (handItem.isEmpty()) {
                if (!level.isClientSide) {
                    PlayerLeashUtils.dropLeash(targetPlayer, true, true);
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        PlayerLeashData data = player.getData(ModAttachments.PLAYER_LEASH.get());
        if (!data.hasLeash()) return;

        Entity leasher = PlayerLeashUtils.getLeashHolder(player);

        Component denyMessage = PlayerLeashUtils.canBeLeashed(player, leasher);
        if (denyMessage != null) {
            if (leasher instanceof Player masterPlayer) {
                masterPlayer.displayClientMessage(
                        Component.translatable("event." + MODID + ".leash.connect_abort")
                                .withStyle(ChatFormatting.DARK_RED)
                        , true);
            } else {
                player.displayClientMessage(
                        Component.translatable("event." + MODID + ".leash.release_from_connect")
                        .withStyle(ChatFormatting.GREEN), true);
            }
            PlayerLeashUtils.dropLeash(player, true, true);
            return;
        }

        if (leasher == null || leasher.level() != player.level() || !leasher.isAlive() || !player.isAlive()) {
            PlayerLeashUtils.dropLeash(player, true, true);
            return;
        }

        float distance = player.distanceTo(leasher);

        if (distance > 10.0F) {
            PlayerLeashUtils.dropLeash(player, true, true);
            return;
        }

        if (distance > 6.0F) {
            double dx = (leasher.getX() - player.getX()) / (double) distance;
            double dy = (leasher.getY() - player.getY()) / (double) distance;
            double dz = (leasher.getZ() - player.getZ()) / (double) distance;

            player.setDeltaMovement(player.getDeltaMovement().add(
                    Math.copySign(dx * dx * 0.4, dx),
                    Math.copySign(dy * dy * 0.4, dy),
                    Math.copySign(dz * dz * 0.4, dz)
            ));

            player.hurtMarked = true;
            player.checkSlowFallDistance();
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.FENCES)) return;

        Player leashedPlayer = null;
        for (Player p : level.players()) {
            PlayerLeashData d = p.getData(ModAttachments.PLAYER_LEASH.get());
            if (player.getUUID().equals(d.getLeasherUUID())) {
                leashedPlayer = p;
                break;
            }
        }

        if (leashedPlayer != null) {
            Component denyMessage = PlayerLeashUtils.canBeLeashed(leashedPlayer, null);
            if (denyMessage != null) {
                if (level.isClientSide) {
                    player.displayClientMessage(denyMessage, true);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                return;
            }

            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                PlayerLeashData data = leashedPlayer.getData(ModAttachments.PLAYER_LEASH.get());
                data.setLeasherUUID(null);
                data.setFencePos(pos.immutable());
                leashedPlayer.setData(ModAttachments.PLAYER_LEASH.get(), data);

                LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(serverLevel, pos);
                knot.playPlacementSound();

                serverLevel.getChunkSource().broadcast(leashedPlayer, new ClientboundSetEntityLinkPacket(leashedPlayer, knot));
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LeashFenceKnotEntity knot)) return;

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = knot.getPos();

        Player holdingPlayer = null;
        for (Player p : level.players()) {
            PlayerLeashData d = p.getData(ModAttachments.PLAYER_LEASH.get());
            if (player.getUUID().equals(d.getLeasherUUID())) {
                holdingPlayer = p;
                break;
            }
        }

        if (holdingPlayer != null) {
            Component denyMessage = PlayerLeashUtils.canBeLeashed(holdingPlayer, knot);
            if (denyMessage != null) {
                if (level.isClientSide) {
                    player.displayClientMessage(denyMessage, true);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                return;
            }

            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                PlayerLeashData data = holdingPlayer.getData(ModAttachments.PLAYER_LEASH.get());
                data.setLeasherUUID(null);
                data.setFencePos(pos.immutable());
                holdingPlayer.setData(ModAttachments.PLAYER_LEASH.get(), data);

                serverLevel.getChunkSource().broadcast(holdingPlayer, new ClientboundSetEntityLinkPacket(holdingPlayer, knot));
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            return;
        }

        if (event.getHand() == InteractionHand.MAIN_HAND && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            Player trappedPlayer = getPlayerLeashedToPos(level, pos);

            if (trappedPlayer != null) {
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    transferLeashToPlayer(serverLevel, trappedPlayer, player);
                    checkAndRemoveKnot(serverLevel, knot, pos, trappedPlayer);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof LeashFenceKnotEntity knot)) return;

        Level level = knot.level();
        if (level.isClientSide() || !(level instanceof ServerLevel)) return;

        BlockPos pos = knot.getPos();

        for (Player p : level.players()) {
            PlayerLeashData d = p.getData(ModAttachments.PLAYER_LEASH.get());
            if (pos.equals(d.getFencePos())) {
                PlayerLeashUtils.dropLeash(p, true, true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (PlayerLeashUtils.isLeashed(player)) {
            PlayerLeashUtils.dropLeash(player, true, true);
        }

        AABB searchBox = player.getBoundingBox().inflate(32.0);
        for (Player target : player.level().getEntitiesOfClass(Player.class, searchBox)) {
            PlayerLeashData data = target.getData(ModAttachments.PLAYER_LEASH.get());
            if (player.getUUID().equals(data.getLeasherUUID())) {
                PlayerLeashUtils.dropLeash(target, true, true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (PlayerLeashUtils.isLeashed(player)) {
                PlayerLeashUtils.dropLeash(player, true, true);
            }
        }
    }

    private static Player getPlayerLeashedToPos(Level level, BlockPos pos) {
        for (Player p : level.players()) {
            PlayerLeashData d = p.getData(ModAttachments.PLAYER_LEASH.get());
            if (pos.equals(d.getFencePos())) {
                return p;
            }
        }
        return null;
    }

    private static void transferLeashToPlayer(ServerLevel serverLevel, Player trappedPlayer, Player player) {
        PlayerLeashData data = trappedPlayer.getData(ModAttachments.PLAYER_LEASH.get());
        data.setFencePos(null);
        data.setLeasherUUID(player.getUUID());
        trappedPlayer.setData(ModAttachments.PLAYER_LEASH.get(), data);

        serverLevel.getChunkSource().broadcast(trappedPlayer, new ClientboundSetEntityLinkPacket(trappedPlayer, player));
    }

    private static void checkAndRemoveKnot(ServerLevel serverLevel, LeashFenceKnotEntity knot, BlockPos pos, Player excludePlayer) {
        boolean hasOtherPlayers = false;
        for (Player p : serverLevel.players()) {
            if (p != excludePlayer && pos.equals(p.getData(ModAttachments.PLAYER_LEASH.get()).getFencePos())) {
                hasOtherPlayers = true;
                break;
            }
        }

        List<Leashable> vanillaLeashedInArea = LeadItem.leashableInArea(serverLevel, pos, (leashable) -> {
            return leashable.getLeashHolder() == knot;
        });

        if (!hasOtherPlayers && vanillaLeashedInArea.isEmpty()) {
            knot.dropItem(null);
            knot.discard();
            serverLevel.getChunkSource().broadcast(knot, new ClientboundRemoveEntitiesPacket(knot.getId()));
        }
    }
}