package com.twi.restraint_dungeon.utils.datagen.recipe_provider;

import com.twi.restraint_dungeon.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class RestraintDeviceRecipeProvider extends RecipeProvider {

    public RestraintDeviceRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }


    protected static void build(@NotNull RecipeOutput output) {
        List<String> woods = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "crimson", "warped");

        for (String wood : woods) {
            ItemLike strippedLog = getStrippedLogForWood(wood);

            if (ModBlocks.CROSS_MAP.containsKey(wood)) {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CROSS_MAP.get(wood).get())
                        .pattern("XXX")
                        .pattern("IXI")
                        .pattern(" S ")
                        .define('X', strippedLog)
                        .define('S', Items.SMOOTH_STONE_SLAB)
                        .define('I',Items.IRON_INGOT)
                        .unlockedBy("has_stripped_" + wood + "_log", RecipeProvider.has(strippedLog))
                        .save(output);
            }


            if (ModBlocks.REVERSE_CROSS_MAP.containsKey(wood)) {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.REVERSE_CROSS_MAP.get(wood).get())
                        .pattern("IXI")
                        .pattern("XXX")
                        .pattern(" S ")
                        .define('X', strippedLog)
                        .define('S', Items.SMOOTH_STONE_SLAB)
                        .define('I',Items.IRON_INGOT)
                        .unlockedBy("has_stripped_" + wood + "_log", RecipeProvider.has(strippedLog))
                        .save(output);
            }

            if (ModBlocks.X_CROSS_MAP.containsKey(wood)) {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.X_CROSS_MAP.get(wood).get())
                        .pattern("X X")
                        .pattern("IXI")
                        .pattern("XSX")
                        .define('X', strippedLog)
                        .define('S', Items.SMOOTH_STONE_SLAB)
                        .define('I',Items.IRON_INGOT)
                        .unlockedBy("has_stripped_" + wood + "_log", RecipeProvider.has(strippedLog))
                        .save(output);
            }


            if (ModBlocks.TRIANGLE_HORSE_MAP.containsKey(wood)) {
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TRIANGLE_HORSE_MAP.get(wood).get())
                        .pattern("IXI")
                        .pattern("XXX")
                        .pattern("XSX")
                        .define('X', strippedLog)
                        .define('S', Items.SMOOTH_STONE_SLAB)
                        .define('I',Items.IRON_INGOT)
                        .unlockedBy("has_stripped_" + wood + "_log", RecipeProvider.has(strippedLog))
                        .save(output);
            }
        }


        ModBlocks.CAGE_MAP.values().forEach(holder -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get())
                    .pattern(" B ")
                    .pattern("III")
                    .pattern(" B ")
                    .define('I', Items.IRON_BARS)
                    .define('B',Items.IRON_BLOCK)
                    .unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT))
                    .save(output);
        });

        ModBlocks.DOLL_STAND_MAP.values().forEach(holder -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, holder.get())
                    .pattern("I I")
                    .pattern("I I")
                    .pattern("IBI")
                    .define('I', Items.IRON_INGOT)
                    .define('B',Items.IRON_BLOCK)
                    .unlockedBy("has_iron_ingot", RecipeProvider.has(Items.IRON_INGOT))
                    .save(output);
        });
    }


    private static ItemLike getStrippedLogForWood(String wood) {
        String path;
        if (wood.equals("crimson") || wood.equals("warped")) {
            path = "stripped_" + wood + "_stem";
        } else {
            path = "stripped_" + wood + "_log";
        }
        

        ResourceLocation rl = ResourceLocation.withDefaultNamespace(path);
        ItemLike item = BuiltInRegistries.ITEM.get(rl);

        return item != Items.AIR ? item : Items.STRIPPED_OAK_LOG;
    }
}