package com.twi.restraint_dungeon.effect;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class ModBrewingRecipes {

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        // 渗浆药水 + 紫水晶碎片 -> 黏着药水
        event.getBuilder().addMix(Potions.OOZING, Items.AMETHYST_SHARD, ModPotions.STICKY_POTION);

        // 黏着药水 + 红石粉 -> 延长黏着药水
        event.getBuilder().addMix(ModPotions.STICKY_POTION, Items.REDSTONE, ModPotions.LONG_STICKY_POTION);

        // 普通黏着药水 + 紫水晶碎片 -> 强效黏着药水
        event.getBuilder().addMix(ModPotions.STICKY_POTION, Items.AMETHYST_SHARD, ModPotions.STRONG_STICKY_POTION);
    }
}