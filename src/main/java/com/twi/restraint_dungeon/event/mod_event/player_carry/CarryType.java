package com.twi.restraint_dungeon.event.mod_event.player_carry;

import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
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
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindLegs;


public abstract class CarryType {

    /** 该携带类型的ID  */
    public String getID(){
        return "NONE";
    }

    /** 能否开始携带 */
    public Component canUse(Player carrier, LivingEntity passenger) {
        if(!carrier.isAlive() || !passenger.isAlive()){
            return Component.translatable("action." + MODID + ".fail_common.no_target").withStyle(ChatFormatting.DARK_RED);
        }

        if(isBeenBindArms(carrier) || isBeenBindHands(carrier) || isBeenBindLegs(carrier)) {
            return Component.translatable("action." + MODID + ".fail_common.is_being_binding").withStyle(ChatFormatting.DARK_RED);
        }

        if(!isBeenFullyBind(passenger)) {
            return Component.translatable("action." + MODID + ".fail_carry.need_bind").withStyle(ChatFormatting.DARK_RED);
        }

        if(isRidingRestraintDevice(passenger)){
            return Component.translatable("action." + MODID + ".fail_carry.locked_by_block").withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }
    
    /** 是否应该继续携带 */
    public boolean canContinue(Player carrier, LivingEntity passenger) {
        return carrier.isAlive() && passenger.isAlive()
                && carrier.level() == passenger.level()
                && isBeenFullyBind(passenger);
    }


    /** 获取乘客相对于载具的坐标偏移 */
    public Vec3 getPassengerRidingOffset(Player carrier, LivingEntity passenger) {
        return new Vec3(0.0, 0.0, 0.0);
    }

    /** 获取乘客玩家第一人称相机位置的偏移 */
    public Vector3f getPassengerFirstPersonCameraOffset(Player carrier,LivingEntity passenger){
        return new Vector3f(0.0f,0.0f,0.0f);
    }

    /** 获取乘客玩家第一人称相机位置的旋转 */
    public Vector3f getPassengerFirstPersonCameraRotation(Player carrier, LivingEntity passenger) {
        return new Vector3f(0.0f,0.0f,0.0f);
    }


    /** 开始时触发 */
    public void onStart(Player carrier, LivingEntity passenger) {

    }

    /** 每 Tick 执行 */
    public void onTicks(Player carrier, LivingEntity passenger) {}

    /** 结束时触发 */
    public void onRelease(Player carrier, LivingEntity passenger) {


    }
}