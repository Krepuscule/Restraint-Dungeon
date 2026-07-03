package com.twi.restraint_dungeon.event.mod_event.player_carry;

import com.twi.restraint_dungeon.event.mod_event.player_carry.type.CarryHug;
import com.twi.restraint_dungeon.event.mod_event.player_carry.type.CarryRopeConnection;
import com.twi.restraint_dungeon.event.mod_event.player_carry.type.CarryShoulder;
import com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils;

public class ModCarryTypes {
    public static void register() {

        PlayerCarryUtils.registerCarryType(new CarryRopeConnection());
        PlayerCarryUtils.registerCarryType(new CarryHug());
        PlayerCarryUtils.registerCarryType(new CarryShoulder());

    }
}
