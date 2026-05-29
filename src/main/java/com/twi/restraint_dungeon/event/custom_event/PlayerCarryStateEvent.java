package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCarryStateEvent extends PlayerEvent {

    private final CarryType type;
    private final LivingEntity target;

    public PlayerCarryStateEvent(Player player, CarryType type, LivingEntity target) {
        super(player);
        this.type = type;
        this.target = target;
    }

    @Override
    public @NotNull Player getEntity() {return super.getEntity();}
    public CarryType getCarryType(){return type;}
    public LivingEntity getTarget(){return target;}

    public static class Start extends PlayerCarryStateEvent {
        public Start(Player player,CarryType type,LivingEntity target) {
            super(player,type,target);
        }
    }

    public static class Stop extends PlayerCarryStateEvent {
        public Stop(Player player,CarryType type,LivingEntity target) {
            super(player,type,target);
        }
    }
}
