package com.twi.restraint_dungeon.event.custom_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.common_capability.StruggleCapability.StruggleMode;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 一个专门用于设置挣扎动画的事件，不建议用于具体逻辑
 */
public class PlayerStrugglingEvent extends PlayerEvent {
    private final PlayerRestraintPart StrugglingPart;
    private final StruggleMode StrugglingMode;
    private final RestraintPosition currentPosition;
    private final boolean isSuccess;

    public PlayerStrugglingEvent(Player player,
                                 PlayerRestraintPart strugglingPart,
                                 StruggleMode struggleMode,
                                 RestraintPosition currentPosition,
                                 boolean isSuccess) {
        super(player);
        this.StrugglingPart = strugglingPart;
        this.StrugglingMode = struggleMode;
        this.currentPosition = currentPosition;
        this.isSuccess = isSuccess;
    }

    public @NotNull Player getEntity() {
        return super.getEntity();
    }

    public PlayerRestraintPart getStrugglingPart() {return StrugglingPart;}
    public StruggleMode getStrugglingMode() {return StrugglingMode;}
    public RestraintPosition getCurrentPosition() {return currentPosition;}
    public boolean isSuccess() {return isSuccess;}

    public static class Begin extends PlayerStrugglingEvent {
        public Begin(Player player,
                     PlayerRestraintPart strugglingPart,
                     StruggleMode struggleMode,
                     RestraintPosition currentPosition,
                     boolean isSuccess) {
            super(player, strugglingPart,struggleMode,currentPosition,isSuccess);
        }
    }

    public static class End extends PlayerStrugglingEvent {
        public End(Player player,
                     PlayerRestraintPart strugglingPart,
                     StruggleMode struggleMode,
                     RestraintPosition currentPosition,
                     boolean isSuccess) {
            super(player, strugglingPart,struggleMode,currentPosition,isSuccess);
        }
    }




}
