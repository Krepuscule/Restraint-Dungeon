package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move;

import com.twi.restraint_dungeon.event.custom_event.PlayerRestraintMoveEvent;
import com.twi.restraint_dungeon.network.payload.player_restraint.ClientSyncRestraintMoveStagePayload;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class RestraintMoveManager {

    private static final Map<String, PlayerRestraintMove> MOVES = new HashMap<>();
    private static final Map<UUID, PlayerRestraintMove> ACTIVE_PLAYER_MOVES = new HashMap<>();

    public static void register(PlayerRestraintMove move) {
        MOVES.put(move.getMoveTypeId(), move);
    }

    public static Map<String, PlayerRestraintMove> getRegisteredMoves() {
        return Collections.unmodifiableMap(MOVES);
    }

    public static boolean isPlayerRestraintMoving(Player player) {
        PlayerRestraintMove activeMove = ACTIVE_PLAYER_MOVES.get(player.getUUID());
        return activeMove != null && activeMove.getCurrentStage() != PlayerRestraintMove.Stage.NONE;
    }

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerRestraintMove activeMove = ACTIVE_PLAYER_MOVES.get(player.getUUID());
        if (activeMove != null && activeMove.getCurrentStage() != PlayerRestraintMove.Stage.NONE) {

            activeMove.updateTick(player);

            if (activeMove.getCurrentStage() == PlayerRestraintMove.Stage.NONE) {
                ACTIVE_PLAYER_MOVES.remove(player.getUUID());
            }
        }
    }

    public static void handleClientAdvanceStageRequest(Player player, String moveTypeId) {
        if (player.level().isClientSide) return;
        PlayerRestraintMove activeMove = ACTIVE_PLAYER_MOVES.get(player.getUUID());

        if (activeMove != null && activeMove.getMoveTypeId().equals(moveTypeId)) {
            if (activeMove.getCurrentStage() == PlayerRestraintMove.Stage.MID) {
                activeMove.forceAdvance(player);

                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer,
                            new ClientSyncRestraintMoveStagePayload(moveTypeId, "END", activeMove.getCurrentDirection()));
                }
            }
        }
    }


    public static void handleClientSyncYawRequest(Player player, float clientYaw) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;

        PlayerRestraintMove activeMove = ACTIVE_PLAYER_MOVES.get(serverPlayer.getUUID());
        if (activeMove != null) {

            activeMove.setTargetYaw(clientYaw);
            serverPlayer.setYRot(clientYaw);
            serverPlayer.yRotO = clientYaw;
            serverPlayer.setYHeadRot(clientYaw);

            byte byteYaw = (byte) (clientYaw * 256.0F / 360.0F);
            byte byteXRot = (byte) (serverPlayer.getXRot() * 256.0F / 360.0F);


            ClientboundMoveEntityPacket.Rot bodyRotPacket =
                    new net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Rot(
                            serverPlayer.getId(),
                            byteYaw,
                            byteXRot,
                            serverPlayer.onGround()
                    );

            serverPlayer.serverLevel().getChunkSource().broadcast(serverPlayer, bodyRotPacket);
        }
    }

    public static void handleServerInputPacket(Player player, String direction) {
        if (player.level().isClientSide || isPlayerRestraintMoving(player)) return;

        for (PlayerRestraintMove move : MOVES.values()) {
            if (!move.canMove(player)) continue;

            boolean valid = switch (direction) {
                case "W" -> move.canMoveForward(player);
                case "S" -> move.canMoveBackward(player);
                case "A" -> move.canTurnLeft(player);
                case "D" -> move.canTurnRight(player);
                default -> false;
            };

            if (valid) {
                try {
                    PlayerRestraintMove playerInstance = move.getClass().getDeclaredConstructor().newInstance();
                    playerInstance.start(player, direction);
                    ACTIVE_PLAYER_MOVES.put(player.getUUID(), playerInstance);
                } catch (Exception e) { e.printStackTrace(); }
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onMoveMidStarted(PlayerRestraintMoveEvent.Mid event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            String moveId = event.getMove().getMoveTypeId();
            String dir = event.getDirection();
            PacketDistributor.sendToPlayer(serverPlayer,
                    new ClientSyncRestraintMoveStagePayload(moveId, "MID", dir));
        }
    }

    public static void forceResetPlayerMove(UUID playerUUID) {
        PlayerRestraintMove activeMove = ACTIVE_PLAYER_MOVES.remove(playerUUID);
        if (activeMove != null) {
            activeMove.reset();
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        forceResetPlayerMove(event.getOriginal().getUUID());
        forceResetPlayerMove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        forceResetPlayerMove(event.getEntity().getUUID());
    }


    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        forceResetPlayerMove(event.getEntity().getUUID());
    }
}
