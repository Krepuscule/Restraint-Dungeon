package com.twi.restraint_dungeon.network.payload.restraints_packet;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.ModDataComponents;
import com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech.MiraiTechSuitItem;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;

public record SyncMiRaiTechSuitPacket(UUID targetUUID, int index, boolean isActivation) implements CustomPacketPayload {
    public static final Type<SyncMiRaiTechSuitPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_mirai_suit"));

    public static final StreamCodec<FriendlyByteBuf, SyncMiRaiTechSuitPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SyncMiRaiTechSuitPacket::targetUUID,
            ByteBufCodecs.VAR_INT, SyncMiRaiTechSuitPacket::index,
            ByteBufCodecs.BOOL, SyncMiRaiTechSuitPacket::isActivation,
            SyncMiRaiTechSuitPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();
            if (!(sender.level() instanceof ServerLevel serverLevel)) return;

            Entity targetEntity = serverLevel.getEntity(this.targetUUID());
            if (targetEntity instanceof LivingEntity target) {
                for (ItemStack stack : getAllPartRestraint(target, PlayerRestraintPart.restraint_body_bind)) {
                    if (stack.getItem() instanceof MiraiTechSuitItem suitItem) {
                        suitItem.handlePacketUpdate(target, stack, this.index(), this.isActivation());

                        context.reply(new SyncMiRaiTechControllerGUIPacket(this.targetUUID(),
                            stack.getOrDefault(ModDataComponents.MIRAI_MODULES.get(), Collections.emptyMap()),
                            Boolean.TRUE.equals(stack.get(ModDataComponents.AROUSED_MODE.get())),
                            Boolean.TRUE.equals(stack.get(ModDataComponents.DENY_MODE.get()))
                        ));
                        return;
                    }
                }
            }
        });
    }
}