package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.action.BaseAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerActionEvent extends PlayerEvent {
    private final BaseAction action;

    public PlayerActionEvent(Player player, BaseAction action) {
        super(player);
        this.action = action;
    }

    @Override
    public @NotNull Player getEntity() {return super.getEntity();}
    public BaseAction getAction() {return action;}

    public static class Start extends PlayerActionEvent{

        private final HitResult hitResult;

        public Start(Player player, BaseAction action, HitResult hitResult) {
            super(player, action);
            this.hitResult = hitResult;
        }
        public HitResult getHitResult() {return hitResult;}
    }

    public static class Finish extends PlayerActionEvent{

        private final LivingEntity entity;

        public Finish(Player player, BaseAction action, LivingEntity entity) {
            super(player, action);
            this.entity = entity;
        }

        public LivingEntity getTargetEntity() {return entity;}
    }

    public static class Abort extends PlayerActionEvent{

        private final LivingEntity entity;

        public Abort(Player player, BaseAction action, LivingEntity entity) {
            super(player, action);
            this.entity = entity;
        }

        public LivingEntity getTargetEntity() {return entity;}
    }
}
