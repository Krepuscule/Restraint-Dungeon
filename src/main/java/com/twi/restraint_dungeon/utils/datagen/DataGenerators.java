package com.twi.restraint_dungeon.utils.datagen;

import com.twi.restraint_dungeon.utils.datagen.block_provider.ModBlockLootSubProvider;
import com.twi.restraint_dungeon.utils.datagen.block_provider.ModBlockStateProvider;
import com.twi.restraint_dungeon.utils.datagen.block_provider.ModBlockTagsProvider;
import com.twi.restraint_dungeon.utils.datagen.language_provider.ModLanguageProvider;
import com.twi.restraint_dungeon.utils.datagen.model_provider.ModItemModelProvider;
import com.twi.restraint_dungeon.utils.datagen.recipe_provider.ModRecipeProvider;
import com.twi.restraint_dungeon.utils.datagen.sound_provider.ModSoundDefinitionsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, "zh_cn"));

        ModBlockTagsProvider blockTags = new ModBlockTagsProvider(output, lookupProvider, MODID, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);

        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(output, MODID, existingFileHelper));
        generator.addProvider(event.includeClient(),new ModBlockStateProvider(output,existingFileHelper));
        generator.addProvider(event.includeServer(), new LootTableProvider(
                output,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider
        ));

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));

        generator.addProvider(
                event.includeClient(),
                new ModSoundDefinitionsProvider(output, existingFileHelper)
        );
    }
}