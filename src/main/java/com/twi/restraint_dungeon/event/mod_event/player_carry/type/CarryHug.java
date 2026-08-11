package com.twi.restraint_dungeon.event.mod_event.player_carry.type;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Objects;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.updateRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;


public class CarryHug extends CarryType {

    @Override
    public String getID() {
        return "CARRY_HUG";
    }

    @Override
    public Component canUse(Player carrier, LivingEntity passenger) {

        if(super.canUse(carrier, passenger) != null){
            return super.canUse(carrier, passenger);
        }

        if(getRestraintPosition(passenger) != RestraintPosition.SITTING){
            return Component.translatable("action." + MODID + ".fail_hug.need_target_sitting").withStyle(ChatFormatting.DARK_RED);
        }

        return null;

    }

    @Override
    public Vec3 getPassengerRidingOffset(Player carrier, LivingEntity passenger) {
        if(passenger instanceof BaseNPCEntity){
            return new Vec3(0.2F,0.25F,-0.4F);
        }

        return new Vec3(0.1F, 0.9F, -0.4F);
    }

    @Override
    public Vector3f getPassengerFirstPersonCameraOffset(Player carrier, LivingEntity passenger){
        return new Vector3f(0.6f,-0.2f,0.0f);
    }

    @Override
    public Vector3f getPassengerFirstPersonCameraRotation(Player carrier, LivingEntity passenger) {
        return new Vector3f(0.0f,-90.0f,0.0f);
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
        updateRestraintPosition(passenger, RestraintPosition.SITTING);
    }
}