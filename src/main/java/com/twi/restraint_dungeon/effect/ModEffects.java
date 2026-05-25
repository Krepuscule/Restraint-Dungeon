package com.twi.restraint_dungeon.effect;

import com.twi.restraint_dungeon.effect.ModEffect.CalmEffect;
import com.twi.restraint_dungeon.effect.ModEffect.ClimaxDenyEffect;
import com.twi.restraint_dungeon.effect.ModEffect.ClimaxEffect;
import com.twi.restraint_dungeon.effect.ModEffect.StickyEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MODID);

    public static final DeferredHolder<MobEffect, ClimaxEffect> CLIMAX = EFFECTS.register("climax",
            () -> new ClimaxEffect(MobEffectCategory.HARMFUL, 0xFF69B4));

    public static final DeferredHolder<MobEffect, CalmEffect> CALM = EFFECTS.register("calm", 
            () -> new CalmEffect(MobEffectCategory.BENEFICIAL, 0xADD8E6));

    public static final DeferredHolder<MobEffect, ClimaxDenyEffect> CLIMAX_DENY = EFFECTS.register("climax_deny", 
            () -> new ClimaxDenyEffect(MobEffectCategory.NEUTRAL, 0x808080));

    public static final DeferredHolder<MobEffect, MobEffect> STICKY = EFFECTS.register("sticky",
            () -> new StickyEffect(MobEffectCategory.HARMFUL, 0x6BB34F));

}