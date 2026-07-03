package com.twi.restraint_dungeon.utils.datagen.block_provider;

import com.twi.restraint_dungeon.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // 1. 处理木质拘束具 (使用 Stripped Log 材质)
        processWoodMap(ModBlocks.CROSS_MAP);
        processWoodMap(ModBlocks.REVERSE_CROSS_MAP);
        processWoodMap(ModBlocks.X_CROSS_MAP);
        processWoodMap(ModBlocks.TRIANGLE_HORSE_MAP);

        // 2. 处理金属类拘束具 (使用 Metal Block 材质)
        processMetalMap(ModBlocks.CAGE_MAP);
        processMetalMap(ModBlocks.DOLL_STAND_MAP);
    }

    /**
     * 自动生成木质方块的 BlockState
     * 映射规则：oak -> minecraft:block/stripped_oak_log
     */
    private void processWoodMap(Map<String, ? extends DeferredHolder<Block, ? extends Block>> map) {
        map.forEach((woodType, holder) -> {
            ResourceLocation logTexture;
            if(woodType.equals("crimson") || woodType.equals("warped")){
                logTexture = ResourceLocation.withDefaultNamespace("block/stripped_" + woodType + "_stem");
            }else{
                logTexture = ResourceLocation.withDefaultNamespace("block/stripped_" + woodType + "_log");
            }
            

            simpleBlock(holder.get(), models().getExistingFile(logTexture));
        });
    }

    /**
     * 自动生成金属方块的 BlockState
     * 映射规则：iron -> minecraft:block/iron_block
     */
    private void processMetalMap(Map<String, ? extends DeferredHolder<Block, ? extends Block>> map) {
        map.forEach((metalType, holder) -> {
            ResourceLocation metalTexture = ResourceLocation.withDefaultNamespace("block/" + metalType + "_block");
            
            simpleBlock(holder.get(), models().getExistingFile(metalTexture));
        });
    }
}