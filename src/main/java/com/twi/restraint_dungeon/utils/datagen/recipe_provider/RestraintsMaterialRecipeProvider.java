package com.twi.restraint_dungeon.utils.datagen.recipe_provider;

import com.twi.restraint_dungeon.item.materials.ModMaterials;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RestraintsMaterialRecipeProvider extends RecipeProvider {
    public RestraintsMaterialRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    protected static void build(@NotNull RecipeOutput output) {


        // 乳胶 烧炼 为 橡胶
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModRestraintItems.LATEX.get()),
                        RecipeCategory.MISC,
                        ModMaterials.RUBBER.get(),
                        0.35F,
                        200
                )

                .unlockedBy("has_latex", RecipeProvider.has(ModRestraintItems.LATEX.get()))
                .save(output, RecipeProvider.getHasName(ModMaterials.RUBBER.get()) + "_from_smelting");

    }
}
