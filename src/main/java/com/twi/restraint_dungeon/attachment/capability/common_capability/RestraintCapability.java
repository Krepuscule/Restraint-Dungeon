package com.twi.restraint_dungeon.attachment.capability.common_capability;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class RestraintCapability {

    public enum ArmsPose {
        NONE, CROSS_BEHIND_BACK
    }

    public enum LegsPose {
        NONE, LEGS_TOGETHER
    }

    public enum PlayerRestraintPart {
        restraint_blindfold, restraint_gag, restraint_collar, restraint_arms_bind,
        restraint_legs_bind, restraint_hands_bind, restraint_body_bind, restraint_connection;

        // 向后切换
        public PlayerRestraintPart next() {
            PlayerRestraintPart[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

        // 向前切换
        public PlayerRestraintPart previous() {
            PlayerRestraintPart[] values = values();
            return values[(this.ordinal() - 1 + values.length) % values.length];
        }

        public String getTranslationKey() {
            return "part.restraint_dungeon." + this.name();
        }
    }

    private ArmsPose prevArmsPose = ArmsPose.NONE;
    private LegsPose prevLegsPose = LegsPose.NONE;
    private ArmsPose armsPose = ArmsPose.NONE;
    private LegsPose legsPose = LegsPose.NONE;

    private RestraintPositionEvent.RestraintPosition previousPosition = RestraintPositionEvent.RestraintPosition.STANDING;
    private RestraintPositionEvent.RestraintPosition restraintPosition = RestraintPositionEvent.RestraintPosition.STANDING;

    private PlayerRestraintPart targetPart = PlayerRestraintPart.restraint_blindfold;

    private boolean isChangingPosition = false;

    public RestraintCapability() {}

    public PlayerRestraintPart getTargetPart() { return targetPart; }
    public void setTargetPart(PlayerRestraintPart part) { this.targetPart = part; }

    public ArmsPose getPrevArmsPose() {return prevArmsPose;}
    public void setPrevArmsPose(ArmsPose armsPose) {this.prevArmsPose = armsPose;}
    public LegsPose getPrevLegsPose() {return prevLegsPose;}
    public void setPrevLegsPose(LegsPose legsPose) {this.prevLegsPose = legsPose;}

    public ArmsPose getArmsPose() { return armsPose; }
    public void setArmsPose(ArmsPose armsPose) { this.armsPose = armsPose; }

    public LegsPose getLegsPose() { return legsPose; }
    public void setLegsPose(LegsPose legsPose) { this.legsPose = legsPose; }

    public RestraintPositionEvent.RestraintPosition getPreviousPosition() { return previousPosition; }
    public void setPreviousPosition(RestraintPositionEvent.RestraintPosition previousPosition) { this.previousPosition = previousPosition; }

    public RestraintPositionEvent.RestraintPosition getRestraintPosition() { return restraintPosition; }
    public void setRestraintPosition(RestraintPositionEvent.RestraintPosition pos) { this.restraintPosition = pos; }

    public boolean isChangingPosition() { return isChangingPosition; }
    public void setChangingPosition(boolean changingPosition) { isChangingPosition = changingPosition; }

    public static final Codec<RestraintCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("target_part").forGetter(c -> c.targetPart.name()),
            Codec.STRING.fieldOf("prev_pos").forGetter(c -> c.previousPosition.name()),
            Codec.STRING.fieldOf("curr_pos").forGetter(c -> c.restraintPosition.name()),
            Codec.STRING.fieldOf("arms_pose").forGetter(c -> c.armsPose.name()),
            Codec.STRING.fieldOf("legs_pose").forGetter(c -> c.legsPose.name()),
            Codec.BOOL.fieldOf("is_changing").forGetter(c -> c.isChangingPosition)
    ).apply(instance, (sel, prev, curr, arms, legs, changing) -> {
        RestraintCapability cap = new RestraintCapability();
        cap.targetPart = PlayerRestraintPart.valueOf(sel);
        cap.previousPosition = RestraintPositionEvent.RestraintPosition.valueOf(prev);
        cap.restraintPosition = RestraintPositionEvent.RestraintPosition.valueOf(curr);
        cap.armsPose = ArmsPose.valueOf(arms);
        cap.legsPose = LegsPose.valueOf(legs);
        cap.isChangingPosition = changing;
        return cap;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, RestraintCapability> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
