package com.twi.restraint_dungeon.utils.mod_utils.leash;

import com.mojang.datafixers.util.Either;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerLeashData;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenCollar;

public class PlayerLeashUtils {

    private static @Nullable Leashable.LeashData createBlankLeashData() {
        Leashable leash = new Leashable() {
            private Leashable.LeashData data;
            @Override public @Nullable Leashable.LeashData getLeashData() { return this.data; }
            @Override public void setLeashData(@Nullable Leashable.LeashData d) { this.data = d; }
        };
        try {
            leash.setDelayedLeashHolderId(0);
        } catch (Exception ignored) {}
        return leash.getLeashData();
    }


    public static @Nullable Leashable.LeashData toVanillaLeashData(PlayerLeashData leashData, ServerLevel level) {
        if (!leashData.hasLeash()) return null;

        Leashable.LeashData vanillaData = createBlankLeashData();
        if (vanillaData == null) return null;

        if (leashData.getLeasherUUID() != null) {
            Entity entity = level.getEntity(leashData.getLeasherUUID());
            if (entity != null) {
                vanillaData.setLeashHolder(entity);
            } else {
                vanillaData.delayedLeashInfo = Either.left(leashData.getLeasherUUID());
            }
        } else if (leashData.getFencePos() != null) {
            vanillaData.delayedLeashInfo = Either.right(leashData.getFencePos());
        }

        return vanillaData;
    }


    public static @Nullable Entity getLeashHolder(LivingEntity leashedEntity) {
        if (leashedEntity.level().isClientSide()) {
            if (leashedEntity instanceof Mob mob) {
                return mob.getLeashHolder();
            } else if (leashedEntity instanceof Player player) {
                PlayerLeashData data = player.getData(ModAttachments.PLAYER_LEASH.get());

                if (data.getLeasherUUID() != null) {
                    Player targetPlayer = player.level().getPlayerByUUID(data.getLeasherUUID());
                    if (targetPlayer != null) {
                        return targetPlayer;
                    }
                    AABB searchBox = player.getBoundingBox().inflate(32.0);
                    List<Entity> nearbyEntities = player.level().getEntities(null, searchBox);
                    for (Entity entity : nearbyEntities) {
                        if (entity.getUUID().equals(data.getLeasherUUID())) {
                            return entity;
                        }
                    }
                    return null;

                } else if (data.getFencePos() != null) {
                    BlockPos pos = data.getFencePos();
                    List<LeashFenceKnotEntity> knots = player.level().getEntitiesOfClass(
                            LeashFenceKnotEntity.class,
                            new AABB(pos)
                    );
                    return knots.isEmpty() ? null : knots.get(0);
                }
            }
            return null;
        }

        if (leashedEntity instanceof Mob mob) {
            return mob.getLeashHolder();
        } else if (leashedEntity instanceof Player player) {
            PlayerLeashData data = player.getData(ModAttachments.PLAYER_LEASH.get());
            if (!data.hasLeash()) return null;

            ServerLevel serverLevel = (ServerLevel) player.level();

            if (data.getFencePos() != null) {
                return LeashFenceKnotEntity.getOrCreateKnot(serverLevel, data.getFencePos());
            }

            if (data.getLeasherUUID() != null) {
                return serverLevel.getEntity(data.getLeasherUUID());
            }
        }
        return null;
    }

    public static boolean isLeashed(LivingEntity entity) {
        if (entity instanceof BaseNPCEntity npc) {
            return npc.isLeashed();
        } else if (entity instanceof Player player) {
            return player.getData(ModAttachments.PLAYER_LEASH.get()).hasLeash();
        }
        return false;
    }


    public static void dropLeash(LivingEntity entity, boolean broadcastPacket, boolean dropItem) {
        if (entity instanceof Mob mob) {
            mob.dropLeash(broadcastPacket, dropItem);
        } else if (entity instanceof Player player) {
            if (player.level() instanceof ServerLevel serverLevel) {
                PlayerLeashData data = player.getData(ModAttachments.PLAYER_LEASH.get());
                if (data.hasLeash()) {
                    data.reset();
                    player.setData(ModAttachments.PLAYER_LEASH.get(), data);

                    if (broadcastPacket) {
                        serverLevel.getChunkSource().broadcast(player,
                            new net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket(player, null));
                    }
                    if (dropItem) {
                        player.spawnAtLocation(net.minecraft.world.item.Items.LEAD);
                    }
                }
            }
        }
    }

    public static @Nullable Component canBeLeashed(LivingEntity leasher, @Nullable Entity leashHolder) {

        if (!isBeenCollar(leasher)) {
            return Component.translatable("event." + MODID + ".leash.requires_collar").withStyle(ChatFormatting.DARK_RED);
        }

        if (leashHolder != null && leasher.getUUID().equals(leashHolder.getUUID())) {
            return Component.translatable("event." + MODID + ".leash.cant_leash_self").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }
}