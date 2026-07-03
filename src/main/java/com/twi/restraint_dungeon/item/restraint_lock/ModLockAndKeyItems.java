package com.twi.restraint_dungeon.item.restraint_lock;

import com.twi.restraint_dungeon.item.restraint_lock.key.CommonKeyItem;
import com.twi.restraint_dungeon.item.restraint_lock.key.IronKeyItem;
import com.twi.restraint_dungeon.item.restraint_lock.lock.CommonLockItem;
import com.twi.restraint_dungeon.item.restraint_lock.lock.IronLockItem;
import com.twi.restraint_dungeon.item.restraint_lock.lock.MiRaiTechLockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModLockAndKeyItems {

    public static final DeferredRegister<Item> LOCK_AND_KEY_ITEMS =
            DeferredRegister.create(Registries.ITEM, MODID);

    public static final DeferredHolder<Item, CommonLockItem> PERSONAL_COMMON_LOCK = LOCK_AND_KEY_ITEMS.register("personal_common_lock",
            CommonLockItem::new);
    public static final DeferredHolder<Item, CommonKeyItem> PERSONAL_COMMON_KEY = LOCK_AND_KEY_ITEMS.register("personal_common_key",
            CommonKeyItem::new);

    public static final DeferredHolder<Item, IronLockItem> IRON_LOCK = LOCK_AND_KEY_ITEMS.register("iron_lock",
            IronLockItem::new);
    public static final DeferredHolder<Item, MiRaiTechLockItem> MIRAI_TECH_LOCK = LOCK_AND_KEY_ITEMS.register("mirai_tech_lock",
           MiRaiTechLockItem::new);
    public static final DeferredHolder<Item, IronKeyItem> IRON_KEY = LOCK_AND_KEY_ITEMS.register("iron_key",
            IronKeyItem::new);

}
