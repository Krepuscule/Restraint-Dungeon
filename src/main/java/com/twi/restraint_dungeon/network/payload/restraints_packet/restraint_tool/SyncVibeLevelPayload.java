package com.twi.restraint_dungeon.network.payload.restraints_packet.restraint_tool;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.item.restraint_tool.tools.vibrator.VibratorItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record SyncVibeLevelPayload(int targetEntityId, int index, int level, boolean activate) implements CustomPacketPayload {

    public static final Type<SyncVibeLevelPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_vibe_level"));

    public static final StreamCodec<FriendlyByteBuf, SyncVibeLevelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncVibeLevelPayload::targetEntityId,
            ByteBufCodecs.VAR_INT, SyncVibeLevelPayload::index,
            ByteBufCodecs.VAR_INT, SyncVibeLevelPayload::level,
            ByteBufCodecs.BOOL, SyncVibeLevelPayload::activate,
            SyncVibeLevelPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Entity target = player.level().getEntity(this.targetEntityId);
            if (!(target instanceof LivingEntity targetEntity)) return;

            if (this.index == -1) {
                ItemStack stack = targetEntity.getMainHandItem();
                if (!(stack.getItem() instanceof VibratorItem)) {
                    stack = targetEntity.getOffhandItem();
                }

                if (!stack.isEmpty() && stack.getItem() instanceof VibratorItem vibratorItem) {
                    vibratorItem.setVibeLevel(stack, this.level, targetEntity);
                    vibratorItem.setActivated(stack, this.activate, targetEntity);
                }
            }

            else {
                var toolsContainer = targetEntity.getData(ModAttachments.RESTRAINT_TOOLS);
                List<ItemStack> list = toolsContainer.getToolsList();

                if (this.index >= 0 && this.index < list.size()) {
                    ItemStack stack = list.get(this.index);
                    if (!stack.isEmpty() && stack.getItem() instanceof VibratorItem vibratorItem) {
                        vibratorItem.setVibeLevel(stack, this.level, targetEntity);
                        vibratorItem.setActivated(stack, this.activate, targetEntity);

                        RestraintToolsUtils.sync(targetEntity);
                    }
                }
            }
        });
    }
}