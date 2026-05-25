package com.twi.restraint_dungeon.effect;

import com.twi.restraint_dungeon.effect.ModEffect.StickyEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, MODID);

    public static final DeferredHolder<Potion, Potion> STICKY_POTION = POTIONS.register("sticky_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.STICKY, 3600, 0)));

    public static final DeferredHolder<Potion, Potion> LONG_STICKY_POTION = POTIONS.register("long_sticky_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.STICKY, 6000, 0)));

    public static final DeferredHolder<Potion, Potion> STRONG_STICKY_POTION = POTIONS.register("strong_sticky_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.STICKY, 3600, 1)));

    public static void register(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }
}