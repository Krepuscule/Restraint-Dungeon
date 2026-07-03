package com.twi.restraint_dungeon.utils.datagen.recipe_provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        RestraintDeviceRecipeProvider.build(output);
        RestraintItemRecipeProvider.build(output);
        RestraintLockAndKeyRecipeProvider.build(output);

        RestraintsMaterialRecipeProvider.build(output);
    }
}