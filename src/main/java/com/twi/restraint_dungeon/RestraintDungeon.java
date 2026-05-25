package com.twi.restraint_dungeon;

import com.mojang.logging.LogUtils;
import com.twi.restraint_dungeon.action.utils.ModActions;
import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.attributes.ModAttributes;
import com.twi.restraint_dungeon.block.ModBlockEntities;
import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntities;
import com.twi.restraint_dungeon.client.command.ModArgumentTypes;
import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.effect.ModPotions;
import com.twi.restraint_dungeon.event.mod_event.player_carry.ModCarryTypes;
import com.twi.restraint_dungeon.item.ModCreativeTabs;
import com.twi.restraint_dungeon.item.ModDataComponents;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_lock.ModLockAndKeyItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import java.util.UUID;

@Mod(RestraintDungeon.MODID)
public class RestraintDungeon {
    public static final String MODID = "restraint_dungeon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public RestraintDungeon(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);

        ModRestraintItems.RESTRAINT_ITEMS.register(modEventBus);
        ModLockAndKeyItems.LOCK_AND_KEY_ITEMS.register(modEventBus);

        SeatEntities.SEAT_ENTITIES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ModEffects.EFFECTS.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);

        ModArgumentTypes.ARGUMENT_TYPES.register(modEventBus);

        ModCarryTypes.register();
        ModActions.register();
    }
}
