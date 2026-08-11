package com.twi.restraint_dungeon.utils.datagen.recipe_provider;

import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.item.materials.ModMaterials;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_tool.ModRestraintTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class RestraintItemRecipeProvider extends RecipeProvider {

    public RestraintItemRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    protected static void build(@NotNull RecipeOutput output) {

        // 绳子
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModRestraintItems.ROPE.get())
                .requires(Items.STRING)
                .requires(Items.STRING)
                .requires(Items.WHEAT)
                .requires(Items.WHEAT)
                .unlockedBy("has_string", RecipeProvider.has(Items.STRING))
                .save(output);

        // 镣铐
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.SHACKLES.get())
                .pattern("   ")
                .pattern("XCX")
                .pattern("   ")
                .define('C',Items.CHAIN)
                .define('X', Items.IRON_INGOT)
                .unlockedBy("has_iron", RecipeProvider.has(Items.IRON_INGOT))
                .save(output);

        // 胶带
        Ingredient stickyMaterial = Ingredient.of(Items.SLIME_BALL, Items.HONEY_BOTTLE);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModRestraintItems.TAPE.get())
                .requires(stickyMaterial)
                .requires(ModMaterials.RUBBER.get())
                .requires(Items.PAPER)
                .unlockedBy("has_slime", RecipeProvider.has(Items.SLIME_BALL))
                .save(output);

        // 眼罩
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.BLINDFOLD_MASK.get())
                .pattern(" G ")
                .pattern("LLL")
                .pattern("   ")
                .define('L', Items.LEATHER)
                .define('G',Items.GOLD_NUGGET)
                .unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER))
                .save(output);

        // 皮革镣铐
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.LEATHER_CUFFS.get())
                .pattern(" G ")
                .pattern("LCL")
                .pattern("   ")
                .define('C',Items.CHAIN)
                .define('L', Items.LEATHER)
                .define('G',Items.GOLD_NUGGET)
                .unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER))
                .save(output);

        // 铃铛项圈
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModRestraintItems.BELL_COLLAR.get())
                .requires(Items.BELL)
                .requires(ModRestraintItems.LEATHER_COLLAR.get())
                .unlockedBy("has_leather_collar", RecipeProvider.has(ModRestraintItems.LEATHER_COLLAR.get()))
                .save(output);

        // 皮革项圈
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.LEATHER_COLLAR.get())
                .pattern(" G ")
                .pattern("L L")
                .pattern(" N ")
                .define('N',Items.NAME_TAG)
                .define('L', Items.LEATHER)
                .define('G',Items.GOLD_NUGGET)
                .unlockedBy("has_leather", RecipeProvider.has(Items.LEATHER))
                .save(output);

        // 诅咒项圈
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModRestraintItems.CURSED_COLLAR.get())
                .requires(Items.NETHER_STAR)
                .requires(ModRestraintItems.LEATHER_COLLAR.get())
                .unlockedBy("has_nether_star", RecipeProvider.has(Items.NETHER_STAR))
                .save(output);

        // 口球
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.BALL_GAG.get())
                .pattern(" G ")
                .pattern("LRL")
                .pattern(" A ")
                .define('A',ModMaterials.RUBBER.get())
                .define('R',Items.RED_DYE)
                .define('L', Items.LEATHER)
                .define('G',Items.GOLD_NUGGET)
                .unlockedBy("has_latex", RecipeProvider.has(ModMaterials.RUBBER.get()))
                .save(output);

        // 圆环口塞
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.RING_GAG.get())
                .pattern(" G ")
                .pattern("LAL")
                .pattern("   ")
                .define('A',ModMaterials.RUBBER.get())
                .define('L', Items.LEATHER)
                .define('G',Items.GOLD_NUGGET)
                .unlockedBy("has_latex", RecipeProvider.has(ModMaterials.RUBBER.get()))
                .save(output);

        // 单手套
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.ARM_BINDER.get())
                .pattern("AGA")
                .pattern("A A")
                .pattern(" A ")
                .define('A',ModMaterials.RUBBER.get())
                .define('G',Items.GOLD_NUGGET)
                .unlockedBy("has_latex", RecipeProvider.has(ModMaterials.RUBBER.get()))
                .save(output);

        // Mirai拘束靴
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.MIRAI_TECH_BOOT.get())
                .pattern("AAA")
                .pattern("ALA")
                .pattern("ARA")
                .define('A',ModMaterials.RUBBER.get())
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // Mirai眼镜
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.MIRAI_TECH_GLASS.get())
                .pattern(" R ")
                .pattern("ALA")
                .pattern("   ")
                .define('A',ModMaterials.RUBBER.get())
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // Mirai口罩
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.MIRAI_TECH_MASK.get())
                .pattern("   ")
                .pattern("ALA")
                .pattern(" R ")
                .define('A',ModMaterials.RUBBER.get())
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // Mirai手套
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.MIRAI_TECH_MITTEN.get())
                .pattern("   ")
                .pattern("ALA")
                .pattern("ARA")
                .define('A',ModMaterials.RUBBER.get())
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // Mirai手臂套
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.MIRAI_TECH_SLEEVES.get())
                .pattern("ARA")
                .pattern("ALA")
                .pattern("   ")
                .define('A',ModMaterials.RUBBER.get())
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // Mirai拘束衣
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.MIRAI_TECH_SUIT.get())
                .pattern("ARA")
                .pattern("ALA")
                .pattern("AAA")
                .define('A',ModMaterials.RUBBER.get())
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // Mirai拘束衣遥控器
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintItems.MIRAI_TECH_SUIT_REMOTE.get())
                .pattern("LRL")
                .pattern("LGL")
                .pattern("LEL")
                .define('G',Items.GLASS_PANE)
                .define('L',Items.LAPIS_LAZULI)
                .define('R',Items.REDSTONE)
                .define('E',Items.ENDER_PEARL)
                .unlockedBy("has_lapis", RecipeProvider.has(Items.LAPIS_LAZULI))
                .save(output);

        // 跳蛋
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModRestraintTools.VIBRATOR.get())
                .pattern(" I ")
                .pattern(" R ")
                .pattern(" I ")
                .define('R',Items.REDSTONE)
                .define('I',Items.IRON_NUGGET)
                .unlockedBy("has_redstone", RecipeProvider.has(Items.REDSTONE))
                .save(output);
    }

}
