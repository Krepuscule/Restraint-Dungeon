package com.twi.restraint_dungeon.event.mod_event.player_carry;

import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenFullyBind;


public interface CarryType {
    
    // --- 判定逻辑 ---

    /** 该携带类型的ID  */
    default String getID(){
        return "NONE";
    }

    /** 能否开始携带 */
    default Component canUse(Player carrier, LivingEntity passenger) {
        return null;
    }
    
    /** 是否应该继续携带 */
    default boolean canContinue(Player carrier, LivingEntity passenger) { 
        return carrier.isAlive() && passenger.isAlive()
                && carrier.level() == passenger.level()
                && isBeenFullyBind(passenger);
    }


    /** 获取乘客相对于载具的坐标偏移 */
    default Vec3 getPassengerRidingOffset(Player carrier, LivingEntity passenger) {
        return new Vec3(0, 1.2, 0.5);
    }

    /** 获取乘客玩家第一人称相机位置的偏移 */
    default Vector3f getPassengerFirstPersonCameraOffset(Player carrier,LivingEntity passenger){
        return new Vector3f(0.0f,0.0f,0.0f);
    }

    /** 获取乘客玩家第一人称相机位置的旋转 */
    default Vector3f getPassengerFirstPersonCameraRotation(Player carrier, LivingEntity passenger) {
        return new Vector3f(0.0f,0.0f,0.0f);
    }


    /** 开始时触发 */
    default void onStart(Player carrier, LivingEntity passenger) {

    }

    /** 每 Tick 执行 */
    default void onTicks(Player carrier, LivingEntity passenger) {}

    /** 结束时触发 */
    default void onRelease(Player carrier, LivingEntity passenger) {


    }
}