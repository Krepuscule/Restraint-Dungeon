package com.twi.restraint_dungeon.utils.datagen.recipe_provider;

import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_lock.ModLockAndKeyItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class RestraintLockAndKeyRecipeProvider extends RecipeProvider {

    public RestraintLockAndKeyRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    protected static void build(@NotNull RecipeOutput output) {

        // 个人通用锁具
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModLockAndKeyItems.PERSONAL_COMMON_LOCK.get())
                .pattern("I I")
                .pattern("LEL")
                .pattern(" R ")
                .define('I',Items.IRON_INGOT)
                .define('E', Items.ENDER_EYE)
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // 个人通用钥匙
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModLockAndKeyItems.PERSONAL_COMMON_KEY.get())
                .pattern("  L")
                .pattern("LR ")
                .pattern("EL ")
                .define('E', Items.ENDER_EYE)
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // 铁锁
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModLockAndKeyItems.IRON_LOCK.get())
                .pattern("I I")
                .pattern("IBI")
                .pattern("III")
                .define('I',Items.IRON_INGOT)
                .define('B',Items.IRON_BLOCK)
                .unlockedBy("has_iron", RecipeProvider.has(Items.IRON_INGOT))
                .save(output);


        // 铁钥匙
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModLockAndKeyItems.IRON_KEY.get())
                .pattern("  I")
                .pattern("II ")
                .pattern("II ")
                .define('I',Items.IRON_INGOT)
                .unlockedBy("has_iron", RecipeProvider.has(Items.IRON_INGOT))
                .save(output);

    }

}
