package com.twi.restraint_dungeon.utils.datagen.block_provider;

import com.twi.restraint_dungeon.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var axeBuilder = this.tag(BlockTags.MINEABLE_WITH_AXE);
        ModBlocks.CROSS_MAP.values().forEach(h -> axeBuilder.add(h.get()));
        ModBlocks.REVERSE_CROSS_MAP.values().forEach(h -> axeBuilder.add(h.get()));
        ModBlocks.TRIANGLE_HORSE_MAP.values().forEach(h -> axeBuilder.add(h.get()));
        ModBlocks.X_CROSS_MAP.values().forEach(h -> axeBuilder.add(h.get()));

        var pickaxeBuilder = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        ModBlocks.CROSS_MAP.values().forEach(h -> axeBuilder.add(h.get()));
        ModBlocks.REVERSE_CROSS_MAP.values().forEach(h -> axeBuilder.add(h.get()));
        ModBlocks.TRIANGLE_HORSE_MAP.values().forEach(h -> axeBuilder.add(h.get()));
        ModBlocks.X_CROSS_MAP.values().forEach(h -> axeBuilder.add(h.get()));
        ModBlocks.CAGE_MAP.values().forEach(h -> pickaxeBuilder.add(h.get()));
        ModBlocks.DOLL_STAND_MAP.values().forEach(h -> pickaxeBuilder.add(h.get()));
    }
}