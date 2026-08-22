package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.custom_event.RestraintPositionChangeEvent;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;

@EventBusSubscriber(modid = MODID)
public class RestraintServerHandler {

    private record PendingChange(int ticks, RestraintPosition targetPos) {}
    private static final Map<UUID, PendingChange> SERVER_TIMERS = new HashMap<>();
    private static final int ANIMATION_TICKS = 20;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (SERVER_TIMERS.isEmpty()) return;

        Iterator<Map.Entry<UUID, PendingChange>> it = SERVER_TIMERS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PendingChange> entry = it.next();
            UUID uuid = entry.getKey();
            PendingChange pending = entry.getValue();

            if (pending.ticks <= 1) {
                LivingEntity entity = null;

                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    var server = ServerLifecycleHooks.getCurrentServer();

                    ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        entity = player;
                    } else {
                        for (ServerLevel level : server.getAllLevels()) {
                            Entity e = level.getEntity(uuid);
                            if (e instanceof BaseNPCEntity npc) {
                                entity = npc;
                                break;
                            }
                        }
                    }
                }

                if (entity != null) {
                    setChangingPosition(entity, false);
                }
                it.remove();
            } else {
                SERVER_TIMERS.put(uuid, new PendingChange(pending.ticks - 1, pending.targetPos));
            }
        }
    }

    public static void startTransition(LivingEntity entity, RestraintPosition prevPos,RestraintPosition nextPos) {
        setChangingPosition(entity, true);
        SERVER_TIMERS.put(entity.getUUID(), new PendingChange(ANIMATION_TICKS, nextPos));

        NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Pre(entity,prevPos, nextPos,ItemStack.EMPTY));
        updateRestraintPosition(entity,nextPos);
        NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Post(entity,prevPos, nextPos,ItemStack.EMPTY));

    }

    public static RestraintPosition getNextPosition(LivingEntity entity, RestraintPosition current, String direction) {
        return switch (direction) {
            case "up" -> {
                if (current == RestraintPosition.KNEELING && canStanding(entity)) yield RestraintPosition.STANDING;
                if (current == RestraintPosition.SITTING && canKneeling(entity)) yield RestraintPosition.KNEELING;
                if (current == RestraintPosition.LYING_UP && canSitting(entity)) yield RestraintPosition.SITTING;
                yield null;
            }
            case "down" -> {
                if (current == RestraintPosition.STANDING && canKneeling(entity)) yield RestraintPosition.KNEELING;
                if (current == RestraintPosition.KNEELING && canSitting(entity)) yield RestraintPosition.SITTING;
                if (current == RestraintPosition.SITTING && canLyingUp(entity)) yield RestraintPosition.LYING_UP;
                yield null;
            }
            case "left" -> switch (current) {
                case LYING_UP -> canLyingLeft(entity) ? RestraintPosition.LYING_LEFT : null;
                case LYING_LEFT -> canLyingDown(entity) ? RestraintPosition.LYING_DOWN : null;
                case LYING_DOWN -> canLyingRight(entity) ? RestraintPosition.LYING_RIGHT : null;
                case LYING_RIGHT -> canLyingUp(entity) ? RestraintPosition.LYING_UP : null;
                default -> null;
            };
            case "right" -> switch (current) {
                case LYING_UP -> canLyingRight(entity) ? RestraintPosition.LYING_RIGHT : null;
                case LYING_RIGHT -> canLyingDown(entity) ? RestraintPosition.LYING_DOWN : null;
                case LYING_DOWN -> canLyingLeft(entity) ? RestraintPosition.LYING_LEFT : null;
                case LYING_LEFT -> canLyingUp(entity) ? RestraintPosition.LYING_UP : null;
                default -> null;
            };
            default -> null;
        };
    }

    public static boolean canStanding(LivingEntity entity) {
        if(getRestraintPosition(entity) == RestraintPosition.KNEELING && getLegsPose(entity) == RestraintCapability.LegsPose.SPLIT_LEGS){
            return false;
        }
        return true;
    }

    public static boolean canKneeling(LivingEntity entity) { return true; }

    public static boolean canSitting(LivingEntity entity) { return true; }

    public static boolean canLyingUp(LivingEntity entity) { return true; }

    public static boolean canLyingDown(LivingEntity entity) { return true; }

    public static boolean canLyingLeft(LivingEntity entity) { return true; }

    public static boolean canLyingRight(LivingEntity entity) { return true; }
}