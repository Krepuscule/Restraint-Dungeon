package com.twi.restraint_dungeon.network;

import com.twi.restraint_dungeon.network.payload.npc.conversation.*;
import com.twi.restraint_dungeon.network.payload.player_action.ActionExecutePayload;
import com.twi.restraint_dungeon.network.payload.player_animator.PlayerAnimationSequencePayload;
import com.twi.restraint_dungeon.network.payload.player_animator.PlayerAnimationSequencePopPayload;
import com.twi.restraint_dungeon.network.payload.player_self_bondage.SelfBondageActionPayload;
import com.twi.restraint_dungeon.network.payload.player_struggle.InterruptStrugglePayload;
import com.twi.restraint_dungeon.network.payload.player_kidnap.PlayerKidnapActionPayload;
import com.twi.restraint_dungeon.network.payload.player_release.PlayerReleaseActionPayload;
import com.twi.restraint_dungeon.network.payload.player_restraint.*;
import com.twi.restraint_dungeon.network.payload.player_struggle.*;
import com.twi.restraint_dungeon.network.payload.restraints_packet.restraint_tool.RemoveRestraintToolPayload;
import com.twi.restraint_dungeon.network.payload.restraints_packet.restraint_tool.SyncVibeLevelPayload;
import com.twi.restraint_dungeon.network.payload.restraints_packet.restraints.SyncMiRaiTechControllerGUIPayload;
import com.twi.restraint_dungeon.network.payload.restraints_packet.restraints.SyncMiRaiTechSuitPayload;
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
                ServerUpdatePlayerOptionsPayload.TYPE,
                ServerUpdatePlayerOptionsPayload.STREAM_CODEC,
                ServerUpdatePlayerOptionsPayload::handle
        );

        registrar.playToClient(
                ClientPlayerOptionsSyncPayload.TYPE,
                ClientPlayerOptionsSyncPayload.STREAM_CODEC,
                ClientPlayerOptionsSyncPayload::handle
        );

        registrar.playToServer(
                RequestOpenTargetInventoryPayload.TYPE,
                RequestOpenTargetInventoryPayload.STREAM_CODEC,
                RequestOpenTargetInventoryPayload::handle
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
                PlayerReleaseSelfPayload.TYPE,
                PlayerReleaseSelfPayload.STREAM_CODEC,
                PlayerReleaseSelfPayload::handle
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
                InterruptStrugglePayload.TYPE,
                InterruptStrugglePayload.STREAM_CODEC,
                InterruptStrugglePayload::handle
        );

        /* ----------------------------------------- 释放能力相关 ---------------------------------------*/

        registrar.playToServer(
                PlayerReleaseActionPayload.TYPE,
                PlayerReleaseActionPayload.STREAM_CODEC,
                PlayerReleaseActionPayload::handle
        );

        /* ----------------------------------------- 自缚相关 ---------------------------------------*/
        registrar.playToServer(
                SelfBondageActionPayload.TYPE,
                SelfBondageActionPayload.STREAM_CODEC,
                SelfBondageActionPayload::handle
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

        registrar.playToServer(
                PlayerAnimationSequencePopPayload.TYPE,
                PlayerAnimationSequencePopPayload.STREAM_CODEC,
                PlayerAnimationSequencePopPayload::handle
        );

        /* ----------------------------------------- NPC相关 --------------------------------------------*/

        registrar.playToServer(
                ClickOptionPayload.TYPE,
                ClickOptionPayload.STREAM_CODEC,
                ClickOptionPayload::handle
        );

        registrar.playToServer(
                RequestStartConversationPayload.TYPE,
                RequestStartConversationPayload.STREAM_CODEC,
                RequestStartConversationPayload::handle
        );

        registrar.playToServer(
                StopTalkingPayload.TYPE,
                StopTalkingPayload.STREAM_CODEC,
                StopTalkingPayload::handle
        );

        registrar.playToClient(
                OpenNodeGuiPayload.TYPE,
                OpenNodeGuiPayload.STREAM_CODEC,
                OpenNodeGuiPayload::handle
        );


        /* ----------------------------------------- 拘束具实现相关 ---------------------------------------*/

        registrar.playToServer(
                SyncMiRaiTechSuitPayload.TYPE,
                SyncMiRaiTechSuitPayload.STREAM_CODEC,
                SyncMiRaiTechSuitPayload::handle
        );

        registrar.playToClient(
                SyncMiRaiTechControllerGUIPayload.TYPE,
                SyncMiRaiTechControllerGUIPayload.STREAM_CODEC,
                SyncMiRaiTechControllerGUIPayload::handle
        );

        registrar.playToServer(
                SyncVibeLevelPayload.TYPE,
                SyncVibeLevelPayload.STREAM_CODEC,
                SyncVibeLevelPayload::handle
        );

        registrar.playToServer(
                RemoveRestraintToolPayload.TYPE,
                RemoveRestraintToolPayload.STREAM_CODEC,
                RemoveRestraintToolPayload::handle
        );
    }
}