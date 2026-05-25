package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_move.PlayerRestraintMove;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerRestraintMoveEvent extends PlayerEvent {
    private final PlayerRestraintMove move;
    private final String direction;

    public PlayerRestraintMoveEvent(Player player, PlayerRestraintMove move, String direction) {
        super(player);
        this.move = move;
        this.direction = direction;
    }

    @Override
    public @NotNull Player getEntity() { return super.getEntity(); }
    public PlayerRestraintMove getMove() { return move; }
    public String getDirection() { return direction; }


    public static class Pre extends PlayerRestraintMoveEvent implements ICancellableEvent {
        public Pre(Player player, PlayerRestraintMove move, String direction) {
            super(player, move, direction);
        }
    }


    public static class Mid extends PlayerRestraintMoveEvent {
        public Mid(Player player, PlayerRestraintMove move, String direction) {
            super(player, move, direction);
        }
    }


    public static class End extends PlayerRestraintMoveEvent {
        public End(Player player, PlayerRestraintMove move, String direction) {
            super(player, move, direction);
        }
    }
}