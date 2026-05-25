package com.twi.restraint_dungeon.event.mod_event.player_carry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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
        return new Vec3(0, 1.2, 0.5); // 默认前方
    }

    /** 获取乘客的身体旋转角度  */
    default float getPassengerBodyRotation(Player carrier, LivingEntity passenger) {
        return 0.0F; // 默认与载具方向一致
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