package com.twi.restraint_dungeon.block;

import com.twi.restraint_dungeon.block.addon_block.placed_sword.PlacedSwordBlock;
import com.twi.restraint_dungeon.block.restraint_device.device.cage.CageBlock;
import com.twi.restraint_dungeon.block.restraint_device.device.doll_stand.DollStandBlock;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_cross.WoodenCrossBlock;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_reverse_cross.WoodenReverseCrossBlock;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_triangle_horse.WoodenTriangleHorseBlock;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_x_cross.WoodenXCrossBlock;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(MODID);


    /* -------------------------------- 工具方块 -------------------------------------------------- */
    public static final DeferredHolder<Block, GhostBlock> GHOST_BLOCK = BLOCKS.register("ghost_block",
            () -> new GhostBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()));

    public static final DeferredHolder<Block, PlacedSwordBlock> PLACED_SWORD = BLOCKS.register("placed_sword",
            () -> new PlacedSwordBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable())
    );

    /* -------------------------------- 拘束架方块 -------------------------------------------------- */

    // 存储木十字架 Block 的映射表
    public static final Map<String, DeferredHolder<Block, WoodenCrossBlock>> CROSS_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<Block, WoodenReverseCrossBlock>> REVERSE_CROSS_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<Block, WoodenTriangleHorseBlock>> TRIANGLE_HORSE_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<Block, WoodenXCrossBlock>> X_CROSS_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<Block, CageBlock>> CAGE_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<Block, DollStandBlock>> DOLL_STAND_MAP = new HashMap<>();

    static {
        List<String> woods = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry");

        for(String wood : woods) {
            // 注册方块
            DeferredHolder<Block, WoodenCrossBlock> block = BLOCKS.register(wood + "_wooden_cross",
                    () -> new WoodenCrossBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).noOcclusion(),
                            () -> ModBlockEntities.CROSS_BE_MAP.get(wood).get(),
                            wood
                    ));

            CROSS_MAP.put(wood, block);


            BLOCK_ITEMS.register(wood + "_wooden_cross", () -> new BlockItem(block.get(), new Item.Properties()));
        }

        for (String wood : woods) {
            DeferredHolder<Block, WoodenReverseCrossBlock> block = BLOCKS.register(wood + "_wooden_reverse_cross",
                    () -> new WoodenReverseCrossBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).noOcclusion(),
                            () -> ModBlockEntities.REVERSE_CROSS_BE_MAP.get(wood).get(),
                            wood
                    ));
            REVERSE_CROSS_MAP.put(wood, block);
            BLOCK_ITEMS.register(wood + "_wooden_reverse_cross", () -> new BlockItem(block.get(), new Item.Properties()));
        }

        for (String wood : woods) {
            DeferredHolder<Block, WoodenTriangleHorseBlock> block = BLOCKS.register(wood + "_wooden_triangle_horse",
                    () -> new WoodenTriangleHorseBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).noOcclusion(),
                            () -> ModBlockEntities.TRIANGLE_HORSE_BE_MAP.get(wood).get(),
                            wood
                    ));
            TRIANGLE_HORSE_MAP.put(wood, block);
            BLOCK_ITEMS.register(wood + "_wooden_triangle_horse", () -> new BlockItem(block.get(), new Item.Properties()));
        }

        for (String wood : woods) {
            DeferredHolder<Block, WoodenXCrossBlock> block = BLOCKS.register(wood + "_wooden_x_cross",
                    () -> new WoodenXCrossBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).noOcclusion(),
                            () -> ModBlockEntities.X_CROSS_BE_MAP.get(wood).get(),
                            wood
                    ));
            X_CROSS_MAP.put(wood, block);
            BLOCK_ITEMS.register(wood + "_wooden_x_cross", () -> new BlockItem(block.get(), new Item.Properties()));
        }

        List<String> materials = List.of("iron");

        for (String mat : materials) {
            DeferredHolder<Block, CageBlock> block = BLOCKS.register(mat + "_cage",
                    () -> new CageBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F).noOcclusion(),
                            () -> ModBlockEntities.CAGE_BE_MAP.get(mat).get(),
                            mat
                    ));
            CAGE_MAP.put(mat, block);
            BLOCK_ITEMS.register(mat + "_cage", () -> new BlockItem(block.get(), new Item.Properties()));
        }

        for (String mat : materials) {
            DeferredHolder<Block, DollStandBlock> block = BLOCKS.register(mat + "_doll_stand",
                    () -> new DollStandBlock(
                            BlockBehaviour.Properties.of().strength(2.0F).noOcclusion(),
                            () -> ModBlockEntities.DOLL_STAND_BE_MAP.get(mat).get(),
                            mat
                    ));
            DOLL_STAND_MAP.put(mat, block);
            BLOCK_ITEMS.register(mat + "_doll_stand", () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

}