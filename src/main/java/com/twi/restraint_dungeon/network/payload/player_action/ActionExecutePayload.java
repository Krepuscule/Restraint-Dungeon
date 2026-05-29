package com.twi.restraint_dungeon.network.payload.player_action;

import com.twi.restraint_dungeon.action.utils.ActionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record ActionExecutePayload(String actionId, int targetId) implements CustomPacketPayload {

    public static final Type<ActionExecutePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "action_execute"));


    public static final StreamCodec<FriendlyByteBuf, ActionExecutePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ActionExecutePayload::actionId,
            ByteBufCodecs.INT, ActionExecutePayload::targetId,
            ActionExecutePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理逻辑
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            HitResult hitResult;
            if (targetId != -1) {
                Entity entity = player.level().getEntity(targetId);
                if (entity != null) {
                    hitResult = new EntityHitResult(entity);
                } else {
                    hitResult = BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
                }
            } else {
                hitResult = player.pick(2.0, 0.0f, false);
            }
            ActionManager.execute(player, actionId, hitResult);
        });
    }
}