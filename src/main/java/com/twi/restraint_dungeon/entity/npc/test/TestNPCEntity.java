package com.twi.restraint_dungeon.entity.npc.test;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.goal.NPCRandomStrollGoal;
import com.twi.restraint_dungeon.entity.npc.utils.goal.NPCStopAndLookPlayerGoal;
import com.twi.restraint_dungeon.entity.npc.utils.goal.ReturnToHomeGoal;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.DialogueTree;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.impl.TestDialogueTree;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;

public class TestNPCEntity extends BaseNPCEntity {

    public TestNPCEntity(EntityType<? extends BaseNPCEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected float setNPCGagOffset() {
        return -0.4F;
    }

    protected static final Component[] TEST_NPC_NAMES = {
            Component.translatable("npc." + MODID + ".test_npc.name_00")
    };

    @Override
    protected Component[] getNamePool() {

        return TEST_NPC_NAMES;
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        if (source.is(DamageTypes.GENERIC_KILL) ||
                source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean canBeKidnap(){
        return false;
    }

    @Override
    protected int getMaxSkinCount() { return 1; }

    @Override
    public String getTextureFolder() { return "textures/npc/skins/test/"; }

    @Override
    public boolean canBeConversation(Player player, BaseNPCEntity npc) {
        return super.canBeConversation(player, npc)
            && !isPassenger()
            && getRestraintPosition(this) == RestraintPositionEvent.RestraintPosition.STANDING
            && npc.distanceToSqr(player) <= 16.0F; }

    @Override
    public DialogueTree getDialogueTree() {
        return new TestDialogueTree();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new NPCStopAndLookPlayerGoal(this, 5.0F));
        this.goalSelector.addGoal(2, new ReturnToHomeGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new NPCRandomStrollGoal(this, 1.0D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
}