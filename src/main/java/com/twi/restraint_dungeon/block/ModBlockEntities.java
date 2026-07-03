package com.twi.restraint_dungeon.block;

import com.twi.restraint_dungeon.block.addon_block.placed_sword.PlacedSwordBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.device.cage.CageBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.device.doll_stand.DollStandBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_cross.WoodenCrossBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_reverse_cross.WoodenReverseCrossBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_triangle_horse.WoodenTriangleHorseBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.device.wooden_x_cross.WoodenXCrossBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GhostBlockEntity>> GHOST_BE =
            BLOCK_ENTITIES.register("ghost_be", () ->
                    BlockEntityType.Builder.of(GhostBlockEntity::new, ModBlocks.GHOST_BLOCK.get())
                            .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlacedSwordBlockEntity>> PLACED_SWORD_BE =
            BLOCK_ENTITIES.register("placed_sword_be", () ->
                    BlockEntityType.Builder.of(PlacedSwordBlockEntity::new, ModBlocks.PLACED_SWORD.get())
                            .build(null));

    public static final Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenCrossBlockEntity.Variant>>> CROSS_BE_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenReverseCrossBlockEntity.Variant>>> REVERSE_CROSS_BE_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenTriangleHorseBlockEntity.Variant>>> TRIANGLE_HORSE_BE_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenXCrossBlockEntity.Variant>>> X_CROSS_BE_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<CageBlockEntity.Variant>>> CAGE_BE_MAP = new HashMap<>();
    public static final Map<String, DeferredHolder<BlockEntityType<?>, BlockEntityType<DollStandBlockEntity.Variant>>> DOLL_STAND_BE_MAP = new HashMap<>();

    static {
        List<String> woods = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry","crimson","warped");

        for (String wood : woods) {
            CROSS_BE_MAP.put(wood, BLOCK_ENTITIES.register(wood + "_wooden_cross_be",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new WoodenCrossBlockEntity.Variant(pos, state, wood),
                            ModBlocks.CROSS_MAP.get(wood).get()
                    ).build(null)));
        }

        for (String wood : woods) {
            REVERSE_CROSS_BE_MAP.put(wood, BLOCK_ENTITIES.register(wood + "_wooden_reverse_cross_be",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new WoodenReverseCrossBlockEntity.Variant(pos, state, wood),
                            ModBlocks.REVERSE_CROSS_MAP.get(wood).get()
                    ).build(null)));
        }

        for (String wood : woods) {
            TRIANGLE_HORSE_BE_MAP.put(wood, BLOCK_ENTITIES.register(wood + "_wooden_triangle_horse_be",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new WoodenTriangleHorseBlockEntity.Variant(pos, state, wood),
                            ModBlocks.TRIANGLE_HORSE_MAP.get(wood).get()
                    ).build(null)));
        }

        for (String wood : woods) {
            X_CROSS_BE_MAP.put(wood, BLOCK_ENTITIES.register(wood + "_wooden_x_cross_be",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new WoodenXCrossBlockEntity.Variant(pos, state, wood),
                            ModBlocks.X_CROSS_MAP.get(wood).get()
                    ).build(null)));
        }

        List<String> materials = List.of("iron");

        for (String mat : materials) {
            CAGE_BE_MAP.put(mat, BLOCK_ENTITIES.register(mat + "_cage_be",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new CageBlockEntity.Variant(pos, state, mat),
                            ModBlocks.CAGE_MAP.get(mat).get()
                    ).build(null)));
        }

        for (String mat : materials) {
            DOLL_STAND_BE_MAP.put(mat, BLOCK_ENTITIES.register(mat + "_doll_stand_be",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new DollStandBlockEntity.Variant(pos, state, mat),
                            ModBlocks.DOLL_STAND_MAP.get(mat).get()
                    ).build(null)));
        }

    }

}