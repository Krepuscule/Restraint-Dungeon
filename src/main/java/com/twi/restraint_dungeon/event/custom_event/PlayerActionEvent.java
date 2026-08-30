package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.action.BaseAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerActionEvent extends Event {
    private final @Nullable Player player;
    private final BaseAction action;

    public PlayerActionEvent(@Nullable Player player, BaseAction action) {
        this.player = player;
        this.action = action;
    }

    @Nullable
    public Player getEntity() {
        return player;
    }

    public BaseAction getAction() {
        return action;
    }

    public static class Before extends PlayerActionEvent {
        private final HitResult hitResult;
        public Before(@Nullable Player player, BaseAction action, HitResult hitResult) {
            super(player, action);
            this.hitResult = hitResult;
        }
        public HitResult getHitResult() { return hitResult; }
    }

    public static class Start extends PlayerActionEvent {
        private final HitResult hitResult;
        public Start(@Nullable Player player, BaseAction action, HitResult hitResult) {
            super(player, action);
            this.hitResult = hitResult;
        }
        public HitResult getHitResult() { return hitResult; }
    }

    public static class Finish extends PlayerActionEvent {
        private final LivingEntity entity;
        public Finish(@Nullable Player player, BaseAction action, LivingEntity entity) {
            super(player, action);
            this.entity = entity;
        }
        @Nullable public LivingEntity getTargetEntity() { return entity; }
    }

    public static class Abort extends PlayerActionEvent {
        private final LivingEntity entity;
        public Abort(@Nullable Player player, BaseAction action, LivingEntity entity) {
            super(player, action);
            this.entity = entity;
        }
        @Nullable public LivingEntity getTargetEntity() { return entity; }
    }
}
