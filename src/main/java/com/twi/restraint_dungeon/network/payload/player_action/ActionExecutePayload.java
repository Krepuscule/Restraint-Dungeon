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

import java.util.Optional;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record ActionExecutePayload(
        String actionId,
        int entityId,
        Optional<BlockPos> clickedPos,
        Direction direction,
        Vec3 hitVec
) implements CustomPacketPayload {

    public static final Type<ActionExecutePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "action_execute"));

    private static final StreamCodec<FriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    public static final StreamCodec<FriendlyByteBuf, ActionExecutePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ActionExecutePayload::actionId,
            ByteBufCodecs.INT, ActionExecutePayload::entityId,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), ActionExecutePayload::clickedPos,
            Direction.STREAM_CODEC, ActionExecutePayload::direction,
            VEC3_STREAM_CODEC, ActionExecutePayload::hitVec,
            ActionExecutePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 🖥️ 服务端核心反序列化与分发处理逻辑
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            HitResult serverHitResult;

            if (entityId != -1) {
                Entity entity = player.level().getEntity(entityId);
                if (entity != null) {
                    serverHitResult = new EntityHitResult(entity, hitVec);
                } else {
                    serverHitResult = BlockHitResult.miss(hitVec, direction, BlockPos.ZERO);
                }
            }
            else if (clickedPos.isPresent()) {
                serverHitResult = new BlockHitResult(hitVec, direction, clickedPos.get(), false);
            }
            else {
                serverHitResult = BlockHitResult.miss(hitVec, direction, BlockPos.ZERO);
            }
            ActionManager.execute(player, actionId, serverHitResult);
        });
    }
}