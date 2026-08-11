package com.twi.restraint_dungeon.network.payload.player_restraint;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintServerHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintServerHandler.getNextPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.isChangingPosition;

public record PlayerSetTargetPositionPayload(UUID targetUUID, String direction) implements CustomPacketPayload {

    public static final Type<PlayerSetTargetPositionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "player_set_target_position"));

    public static final StreamCodec<FriendlyByteBuf, PlayerSetTargetPositionPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerSetTargetPositionPayload::targetUUID,
            ByteBufCodecs.STRING_UTF8, PlayerSetTargetPositionPayload::direction,
            PlayerSetTargetPositionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();
            if (sender.level() instanceof ServerLevel level) {
                Entity target = level.getEntity(this.targetUUID);
                if(!(target instanceof LivingEntity living)) return;
                if ((living instanceof Player || living instanceof BaseNPCEntity) && !isChangingPosition(living)) {
                    RestraintPosition next = getNextPosition(living, getRestraintPosition(living), this.direction);
                    if (next != null) {
                        RestraintServerHandler.startTransition(living,getRestraintPosition(living), next);
                    }
                }
            }
        });
    }
}