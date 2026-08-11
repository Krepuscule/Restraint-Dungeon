package com.twi.restraint_dungeon.item.restraint_item;

import com.twi.restraint_dungeon.item.restraint_item.restraints.*;
import com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech.*;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.LatexItem;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.MerchantSlimeItem;
import com.twi.restraint_dungeon.item.restraint_item.restraints.slime_item.SlimeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModRestraintItems {
    public static final DeferredRegister<Item> RESTRAINT_ITEMS =
            DeferredRegister.create(Registries.ITEM, MODID);

    public static final DeferredHolder<Item, RopeItem> ROPE = RESTRAINT_ITEMS.register("rope",
            () -> new RopeItem(new Item.Properties()));

    public static final DeferredHolder<Item, ShacklesItem> SHACKLES = RESTRAINT_ITEMS.register("shackles",
            () -> new ShacklesItem(new Item.Properties()));

    public static final DeferredHolder<Item, TapeItem> TAPE = RESTRAINT_ITEMS.register("tape",
            () -> new TapeItem(new Item.Properties()));

    public static final DeferredHolder<Item, BallgagItem> BALL_GAG = RESTRAINT_ITEMS.register("ball_gag",
            () -> new BallgagItem(new Item.Properties()));

    public static final DeferredHolder<Item, BlindfoldMaskItem> BLINDFOLD_MASK = RESTRAINT_ITEMS.register("blindfold_mask",
            () -> new BlindfoldMaskItem(new Item.Properties()));

    public static final DeferredHolder<Item, LeatherCollarItem> LEATHER_COLLAR = RESTRAINT_ITEMS.register("leather_collar",
            () -> new LeatherCollarItem(new Item.Properties()));

    public static final DeferredHolder<Item, BellCollarItem> BELL_COLLAR = RESTRAINT_ITEMS.register("bell_collar",
            () -> new BellCollarItem(new Item.Properties()));

    public static final DeferredHolder<Item, MagicRopeItem> MAGIC_ROPE = RESTRAINT_ITEMS.register("magic_rope",
            () -> new MagicRopeItem(new Item.Properties()));

    public static final DeferredHolder<Item, CursedCollarItem> CURSED_COLLAR = RESTRAINT_ITEMS.register("cursed_collar",
            () -> new CursedCollarItem(new Item.Properties()));

    public static final DeferredHolder<Item, LeatherCuffsItem> LEATHER_CUFFS = RESTRAINT_ITEMS.register("leather_cuffs",
            () -> new LeatherCuffsItem(new Item.Properties()));

    public static final DeferredHolder<Item, ArmBinderItem> ARM_BINDER = RESTRAINT_ITEMS.register("arm_binder",
            () -> new ArmBinderItem(new Item.Properties()));


    public static final DeferredHolder<Item, SlimeItem> SLIME = RESTRAINT_ITEMS.register("slime",
            () -> new SlimeItem(new Item.Properties()));

    public static final DeferredHolder<Item, LatexItem> LATEX = RESTRAINT_ITEMS.register("latex",
            () -> new LatexItem(new Item.Properties()));

    public static final DeferredHolder<Item, MerchantSlimeItem> MERCHANT_SLIME = RESTRAINT_ITEMS.register("merchant_slime",
            () -> new MerchantSlimeItem(new Item.Properties()));


    public static final DeferredHolder<Item, MiraiTechSuitItem> MIRAI_TECH_SUIT = RESTRAINT_ITEMS.register("mirai_tech_suit",
            () -> new MiraiTechSuitItem(new Item.Properties()));

    public static final DeferredHolder<Item, MiraiTechGlassItem> MIRAI_TECH_GLASS = RESTRAINT_ITEMS.register("mirai_tech_glass",
            () -> new MiraiTechGlassItem(new Item.Properties()));

    public static final DeferredHolder<Item, MiraiTechMaskItem> MIRAI_TECH_MASK = RESTRAINT_ITEMS.register("mirai_tech_mask",
            () -> new MiraiTechMaskItem(new Item.Properties()));

    public static final DeferredHolder<Item, MiraiTechSleevesItem> MIRAI_TECH_SLEEVES = RESTRAINT_ITEMS.register("mirai_tech_sleeves",
            () -> new MiraiTechSleevesItem(new Item.Properties()));

    public static final DeferredHolder<Item, MiraiTechMittenItem> MIRAI_TECH_MITTEN = RESTRAINT_ITEMS.register("mirai_tech_mitten",
            () -> new MiraiTechMittenItem(new Item.Properties()));

    public static final DeferredHolder<Item, MiraiTechBootItem> MIRAI_TECH_BOOT = RESTRAINT_ITEMS.register("mirai_tech_boot",
            () -> new MiraiTechBootItem(new Item.Properties()));

    public static final DeferredHolder<Item, RingGagItem> RING_GAG = RESTRAINT_ITEMS.register("ring_gag",
            () -> new RingGagItem(new Item.Properties()));

    // --- 功能性道具 ---
    public static final DeferredHolder<Item, MiraiTechSuitRemote> MIRAI_TECH_SUIT_REMOTE = RESTRAINT_ITEMS.register("mirai_tech_suit_remote",
            () -> new MiraiTechSuitRemote(new Item.Properties().rarity(Rarity.COMMON)));
}