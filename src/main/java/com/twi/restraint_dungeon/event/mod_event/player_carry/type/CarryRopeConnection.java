package com.twi.restraint_dungeon.event.mod_event.player_carry.type;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.restraint_item.restraints.RopeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.updateRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getFirstConnectBind;

public class CarryRopeConnection extends CarryType {
    @Override
    public String getID() {
        return "CARRY_ROPE_CONNECTION";
    }

    @Override
    public Component canUse(Player carrier, LivingEntity passenger) {

        if(super.canUse(carrier, passenger) != null){
            return super.canUse(carrier, passenger);
        }

        if(getRestraintPosition(passenger) != RestraintPosition.CONNECTING
                || !(getFirstConnectBind(passenger).getItem() instanceof RopeItem)){
            return Component.translatable("action." + MODID + ".fail_rope_connection_carry.need_target_rope_connection").withStyle(ChatFormatting.DARK_RED);
        }

        return null;

    }

    @Override
    public boolean canContinue(Player carrier, LivingEntity passenger) {

        if(!super.canContinue(carrier, passenger)){
            return false;
        }

        if(!(getFirstConnectBind(passenger).getItem() instanceof RopeItem)){
            return false;
        }

        return true;
    }

    @Override
    public Vec3 getPassengerRidingOffset(Player carrier, LivingEntity passenger) {

        if(passenger instanceof BaseNPCEntity){
            return new Vec3(-0.5F,0.25F,-0.15F);
        }
        return new Vec3(-0.5F, 0.4F, -0.15F);
    }

    @Override
    public Vector3f getPassengerFirstPersonCameraOffset(Player carrier, LivingEntity passenger){
        return new Vector3f(0.4f,-0.2f,-1.2f);
    }

    public Vector3f getPassengerFirstPersonCameraRotation(Player carrier, LivingEntity passenger) {
        return new Vector3f(0.0f,180.0f,0.0f);
    }

    @Override
    public List<Float> getPassengerEntityDimensions(Player carrier, LivingEntity passenger) {
        return List.of(1.25F,0.5F);
    }

    @Override
    public void onStart(Player carrier, LivingEntity passenger) {
        super.onStart(carrier, passenger);
    }

    @Override
    public void onTicks(Player carrier, LivingEntity passenger) {
        super.onTicks(carrier, passenger);
    }

    @Override
    public void onRelease(Player carrier, LivingEntity passenger) {
        super.onRelease(carrier, passenger);
        if(getFirstConnectBind(passenger).getItem() instanceof RopeItem){
            updateRestraintPosition(passenger, RestraintPosition.CONNECTING);
        }else{
            updateRestraintPosition(passenger, RestraintPosition.LYING_DOWN);
        }
    }
}
