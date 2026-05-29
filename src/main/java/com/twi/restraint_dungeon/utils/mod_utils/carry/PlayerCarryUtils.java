package com.twi.restraint_dungeon.utils.mod_utils.carry;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerCarryCapability;
import com.twi.restraint_dungeon.event.custom_event.PlayerCarryStateEvent;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.updateRestraintPosition;

public class PlayerCarryUtils {

    private static final Map<String, CarryType> CARRY_TYPES = new HashMap<>();

    public static void registerCarryType(CarryType type) {
        CARRY_TYPES.put(type.getID(), type);
    }

    public static CarryType getCarryType(String id) {
        return CARRY_TYPES.get(id);
    }

    // --- 基础数据快捷获取 ---

    public static String getCarryState(LivingEntity entity) {
        return getCarryData(entity).getCarryingState();
    }

    public static UUID getPartnerUUID(LivingEntity entity) {
        return getCarryData(entity).getPartnerUUID();
    }

    /** 检查实体是否具有“被抱起”的标记 */
    public static boolean isTargetFlag(LivingEntity entity) {
        return getCarryData(entity).isTarget();
    }

    public static CarryType getCurrentCarryType(LivingEntity entity) {
        String state = getCarryState(entity);
        if (state == null || state.equals("NONE")) return null;
        return getCarryType(state);
    }

    public static PlayerCarryCapability getCarryData(LivingEntity entity) {
        return entity.getData(ModAttachments.PLAYER_CARRY);
    }

    public static void setCarryData(LivingEntity entity, String state, UUID partnerUUID, boolean isTarget) {
        entity.setData(ModAttachments.PLAYER_CARRY, new PlayerCarryCapability(state, partnerUUID, isTarget));
    }

    public static void clearCarryData(LivingEntity entity) {
        if (entity == null) return;
        setCarryData(entity, "NONE", null, false);
    }


    /** 检查玩家是否正在抱着某人 */
    public static boolean isCarrier(Player player) {
        if(player == null || !player.hasData(ModAttachments.PLAYER_CARRY)) return false;
        return !getCarryState(player).equals("NONE") && !isTargetFlag(player);
    }

    /**
     * 检查实体是否正被携带
     */
    public static boolean isBeingCarried(LivingEntity entity) {
        if (isTargetFlag(entity)) return true;

        if (entity.isPassenger() && entity.getVehicle() instanceof Player carrier) {
            return entity.getUUID().equals(getPartnerUUID(carrier));
        }
        return false;
    }

    public static LivingEntity getCarriedPassenger(Player carrier) {
        String state = getCarryState(carrier);
        UUID partnerUuid = getPartnerUUID(carrier);

        if (state.equals("NONE") || partnerUuid == null) return null;

        Entity passenger = carrier.getFirstPassenger();
        if (passenger instanceof LivingEntity living && living.getUUID().equals(partnerUuid)) {
            return living;
        }

        if (!carrier.level().isClientSide && carrier.level() instanceof ServerLevel serverLevel) {

            Entity foundLocal = serverLevel.getEntity(partnerUuid);
            if (foundLocal instanceof LivingEntity living) {
                return living;
            }
            ServerPlayer foundPlayer = serverLevel.getServer().getPlayerList().getPlayer(partnerUuid);
            if (foundPlayer != null) {
                return foundPlayer;
            }

            for (ServerLevel otherLevel : serverLevel.getServer().getAllLevels()) {
                if (otherLevel == serverLevel) continue;
                Entity foundOther = otherLevel.getEntity(partnerUuid);
                if (foundOther instanceof LivingEntity living) {
                    return living;
                }
            }
        }

        return null;
    }

    public static Player getCarrier(LivingEntity passenger) {
        if (passenger.getVehicle() instanceof Player carrier) {
            if (passenger.getUUID().equals(getPartnerUUID(carrier))) {
                return carrier;
            }
        }
        return null;
    }

    public static void checkTicks(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide || !entity.isAlive()) return;

        ServerPlayer carrier = null;
        LivingEntity passenger = null;

        if (entity instanceof ServerPlayer player && isCarrier(player)) {
            carrier = player;
            UUID pUUID = getPartnerUUID(carrier);
            if (pUUID != null) {
                Entity found = ((ServerLevel) carrier.level()).getEntity(pUUID);
                if (found instanceof LivingEntity living) {
                    passenger = living;
                }
            }
        }
        else if (isBeingCarried(entity)) {
            passenger = entity;
            Player p = getCarrier(passenger);
            if (p instanceof ServerPlayer sp) {
                carrier = sp;
            }
        }
        if (carrier != null && passenger != null) {

            if (passenger.level() == carrier.level() && passenger.getVehicle() != carrier) {
                if (passenger.distanceToSqr(carrier) < 144.0) { // 12格范围内
                    passenger.moveTo(carrier.position());
                    if (passenger.startRiding(carrier, true)) {
                        carrier.server.getPlayerList().broadcastAll(new ClientboundSetPassengersPacket(carrier));
                    }
                }
                return;
            }

            if (passenger.level() == carrier.level()) {
                CarryType type = getCarryType(getCarryState(carrier));
                if (type != null && !type.getID().equals("NONE")) {
                    if (!type.canContinue(carrier, passenger)) {
                        stopCarrying(carrier);
                        carrier.displayClientMessage(Component.translatable("action." + MODID + ".escape.target_released")
                                .withStyle(ChatFormatting.YELLOW), true);
                        if (passenger instanceof Player targetPlayer) {
                            targetPlayer.displayClientMessage(Component.translatable("action." + MODID + ".escape.escape_from_carry")
                                    .withStyle(ChatFormatting.GREEN), true);
                        }
                    }
                }
            }
        } else if (carrier != null) {

            if (carrier.tickCount % 100 == 0 && carrier.portalProcess == null) {
                stopCarrying(carrier);
            }
        }
    }


    public static void startCarrying(Player carrier, LivingEntity passenger, String typeId) {
        CarryType type = getCarryType(typeId);

        if(type.canUse(carrier, passenger) == null){
            setCarryData(carrier, typeId, passenger.getUUID(), false);

            setCarryData(passenger, typeId, carrier.getUUID(), true);

            updateRestraintPosition(passenger, RestraintPositionEvent.RestraintPosition.CARRIED);

            if (passenger.startRiding(carrier, true)) {
                if (!carrier.level().isClientSide && carrier instanceof ServerPlayer serverCarrier) {
                    serverCarrier.server.getPlayerList().broadcastAll(new ClientboundSetPassengersPacket(carrier));
                }

                if (passenger instanceof Mob mob) {
                    mob.setNoAi(true);
                }
                type.onStart(carrier, passenger);

                NeoForge.EVENT_BUS.post(new PlayerCarryStateEvent.Start(carrier,type,passenger));
            } else {
                clearCarryData(carrier);
                clearCarryData(passenger);
            }
            carrier.refreshDimensions();

        }
    }

    public static void stopCarrying(Player carrier) {
        LivingEntity passenger = getCarriedPassenger(carrier);
        CarryType type = getCurrentCarryType(carrier);

        if (carrier != null && passenger != null) {
            passenger.stopRiding();

            if (!carrier.level().isClientSide && carrier instanceof ServerPlayer serverCarrier) {
                serverCarrier.server.getPlayerList().broadcastAll(new ClientboundSetPassengersPacket(carrier));
            }

            if (passenger instanceof Mob mob) {
                mob.setNoAi(false);
            }

            updateRestraintPosition(passenger, RestraintPositionEvent.RestraintPosition.STANDING);

            if (type != null) {
                type.onRelease(carrier, passenger);
            }
        }
        clearCarryData(carrier);
        clearCarryData(passenger);
        if (carrier != null) {
            carrier.refreshDimensions();
        }

        NeoForge.EVENT_BUS.post(new PlayerCarryStateEvent.Stop(carrier,type,passenger));
    }
}