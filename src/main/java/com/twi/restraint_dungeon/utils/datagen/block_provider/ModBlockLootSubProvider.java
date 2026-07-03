package com.twi.restraint_dungeon.utils.datagen.block_provider;

import com.twi.restraint_dungeon.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModBlockLootSubProvider extends BlockLootSubProvider {

    public ModBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.VANILLA_SET, registries);
    }

    @Override
    protected void generate() {
        ModBlocks.CROSS_MAP.values().forEach(holder -> this.dropSelf(holder.get()));
        ModBlocks.REVERSE_CROSS_MAP.values().forEach(holder -> this.dropSelf(holder.get()));
        ModBlocks.TRIANGLE_HORSE_MAP.values().forEach(holder -> this.dropSelf(holder.get()));
        ModBlocks.X_CROSS_MAP.values().forEach(holder -> this.dropSelf(holder.get()));

        ModBlocks.CAGE_MAP.values().forEach(holder -> this.dropSelf(holder.get()));
        ModBlocks.DOLL_STAND_MAP.values().forEach(holder -> this.dropSelf(holder.get()));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {

        return Stream.of(
                ModBlocks.CROSS_MAP.values().stream(),
                ModBlocks.REVERSE_CROSS_MAP.values().stream(),
                ModBlocks.TRIANGLE_HORSE_MAP.values().stream(),
                ModBlocks.X_CROSS_MAP.values().stream(),
                ModBlocks.CAGE_MAP.values().stream(),
                ModBlocks.DOLL_STAND_MAP.values().stream()
        ).flatMap(stream -> stream).map(net.neoforged.neoforge.registries.DeferredHolder::get).collect(Collectors.toList());
    }
}