package com.twi.restraint_dungeon.network.payload.restraints_packet.restraint_tool;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public record RemoveRestraintToolPayload(int targetEntityId, int index) implements CustomPacketPayload {

    public static final Type<RemoveRestraintToolPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "remove_restraint_tool"));

    public static final StreamCodec<FriendlyByteBuf, RemoveRestraintToolPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RemoveRestraintToolPayload::targetEntityId,
            ByteBufCodecs.VAR_INT, RemoveRestraintToolPayload::index,
            RemoveRestraintToolPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer actionPlayer)) return;

            Entity target = actionPlayer.level().getEntity(this.targetEntityId);
            if (!(target instanceof LivingEntity targetEntity)) return;

            ItemStack stack = RestraintToolsUtils.getRestraintToolByIndex(targetEntity, this.index);
            if (!stack.isEmpty() && stack.getItem() instanceof RestraintToolItem toolItem) {


                RestraintToolItem.RestraintToolDropRule rule = toolItem.getDropRule(targetEntity, actionPlayer.damageSources().generic(), false, stack);


                if (rule == RestraintToolItem.RestraintToolDropRule.DESTROY) {

                }
                else {
                    ItemStack itemToGive = stack.copy();
                    if (actionPlayer.getMainHandItem().isEmpty()) {
                        actionPlayer.setItemInHand(InteractionHand.MAIN_HAND, itemToGive);
                    }
                    else {
                        boolean inserted = actionPlayer.getInventory().add(itemToGive);
                        if (!inserted) {
                            actionPlayer.spawnAtLocation(itemToGive);
                        }
                    }
                }

                RestraintToolsUtils.removeRestraintTool(targetEntity, this.index);
            }
        });
    }
}