package com.twi.restraint_dungeon.network;

import com.twi.restraint_dungeon.network.payload.player_action.ActionExecutePayload;
import com.twi.restraint_dungeon.network.payload.player_animator.PlayerAnimationSequencePayload;
import com.twi.restraint_dungeon.network.payload.player_kidnap.KidnapInterruptStrugglePayload;
import com.twi.restraint_dungeon.network.payload.player_kidnap.PlayerKidnapActionPayload;
import com.twi.restraint_dungeon.network.payload.player_release.PlayerReleaseActionPayload;
import com.twi.restraint_dungeon.network.payload.player_restraint.*;
import com.twi.restraint_dungeon.network.payload.player_struggle.*;
import com.twi.restraint_dungeon.network.payload.restraints_packet.SyncMiRaiTechControllerGUIPacket;
import com.twi.restraint_dungeon.network.payload.restraints_packet.SyncMiRaiTechSuitPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class ModPayLoad {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);

        /* ----------------------------------------- 拘束能力相关 ---------------------------------------*/

        registrar.playToServer(
                PlayerRestraintPartPayload.TYPE,
                PlayerRestraintPartPayload.STREAM_CODEC,
                PlayerRestraintPartPayload::handle
        );

        registrar.playToServer(
                IsChangingPositionPayload.TYPE,
                IsChangingPositionPayload.STREAM_CODEC,
                IsChangingPositionPayload::handle
        );

        registrar.playToServer(
                RestraintPositionPayload.TYPE,
                RestraintPositionPayload.STREAM_CODEC,
                RestraintPositionPayload::handle
        );

        registrar.playToServer(
                PlayerSetTargetPositionPayload.TYPE,
                PlayerSetTargetPositionPayload.STREAM_CODEC,
                PlayerSetTargetPositionPayload::handle
        );

        registrar.playToClient(
                PlayerInteractProgressPayload.TYPE,
                PlayerInteractProgressPayload.STREAM_CODEC,
                PlayerInteractProgressPayload::handle
        );

        registrar.playToServer(
                PlayerStartBackInteractPayload.TYPE,
                PlayerStartBackInteractPayload.STREAM_CODEC,
                PlayerStartBackInteractPayload::handle
        );

        registrar.playToServer(
                PlayerRestraintMovePayload.TYPE,
                PlayerRestraintMovePayload.STREAM_CODEC,
                PlayerRestraintMovePayload::handle
        );

        registrar.playToClient(
                ClientSyncRestraintMoveStagePayload.TYPE,
                ClientSyncRestraintMoveStagePayload.STREAM_CODEC,
                ClientSyncRestraintMoveStagePayload::handle
        );

        registrar.playToServer(
                RestraintMoveAdvanceStagePayload.TYPE,
                RestraintMoveAdvanceStagePayload.STREAM_CODEC,
                RestraintMoveAdvanceStagePayload::handle
        );

        registrar.playToServer(
                RestraintMoveSyncBodyYawPayload.TYPE,
                RestraintMoveSyncBodyYawPayload.STREAM_CODEC,
                RestraintMoveSyncBodyYawPayload::handle
        );

        registrar.playToServer(
                ServerUpdateRenderOffsetPayload.TYPE,
                ServerUpdateRenderOffsetPayload.STREAM_CODEC,
                ServerUpdateRenderOffsetPayload::handle
        );

        registrar.playToClient(
                ClientRenderOffsetSyncPayload.TYPE,
                ClientRenderOffsetSyncPayload.STREAM_CODEC,
                ClientRenderOffsetSyncPayload::handle
        );

        /* ----------------------------------------- 挣扎能力相关 ---------------------------------------*/

        registrar.playToServer(
                PlayerIsStrugglingPayload.TYPE,
                PlayerIsStrugglingPayload.STREAM_CODEC,
                PlayerIsStrugglingPayload::handle
        );

        registrar.playToServer(
                PlayerStruggleModePayload.TYPE,
                PlayerStruggleModePayload.STREAM_CODEC,
                PlayerStruggleModePayload::handle
        );

        registrar.playToServer(
                PlayerStruggleProgressPayload.TYPE,
                PlayerStruggleProgressPayload.STREAM_CODEC,
                PlayerStruggleProgressPayload::handle
        );

        registrar.playToServer(
                StruggleOutOfIndexRestraintPayload.TYPE,
                StruggleOutOfIndexRestraintPayload.STREAM_CODEC,
                StruggleOutOfIndexRestraintPayload::handle
        );

        registrar.playToServer(
                StruggleOutOfRestraintPayload.TYPE,
                StruggleOutOfRestraintPayload.STREAM_CODEC,
                StruggleOutOfRestraintPayload::handle
        );

        /* ----------------------------------------- 绑架能力相关 ---------------------------------------*/

        registrar.playToServer(
                PlayerKidnapActionPayload.TYPE,
                PlayerKidnapActionPayload.STREAM_CODEC,
                PlayerKidnapActionPayload::handle
        );


        registrar.playToClient(
                KidnapInterruptStrugglePayload.TYPE,
                KidnapInterruptStrugglePayload.STREAM_CODEC,
                KidnapInterruptStrugglePayload::handle
        );

        /* ----------------------------------------- 释放能力相关 ---------------------------------------*/

        registrar.playToServer(
                PlayerReleaseActionPayload.TYPE,
                PlayerReleaseActionPayload.STREAM_CODEC,
                PlayerReleaseActionPayload::handle
        );

        /* ----------------------------------------- 动作能力相关 ---------------------------------------*/

        registrar.playToServer(
                ActionExecutePayload.TYPE,
                ActionExecutePayload.STREAM_CODEC,
                ActionExecutePayload::handle
        );

        /* ----------------------------------------- 动画同步相关 ---------------------------------------*/

        registrar.playToClient(
                PlayerAnimationSequencePayload.TYPE,
                PlayerAnimationSequencePayload.STREAM_CODEC,
                PlayerAnimationSequencePayload::handle
        );

        /* ----------------------------------------- 拘束具实现相关 ---------------------------------------*/

        registrar.playToServer(
                SyncMiRaiTechSuitPacket.TYPE,
                SyncMiRaiTechSuitPacket.STREAM_CODEC,
                SyncMiRaiTechSuitPacket::handle
        );

        registrar.playToClient(
                SyncMiRaiTechControllerGUIPacket.TYPE,
                SyncMiRaiTechControllerGUIPacket.STREAM_CODEC,
                SyncMiRaiTechControllerGUIPacket::handle
        );
    }
}