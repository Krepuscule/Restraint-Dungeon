package com.twi.restraint_dungeon.item;

import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.item.materials.ModMaterials;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_lock.ModLockAndKeyItems;
import com.twi.restraint_dungeon.item.restraint_tool.ModRestraintTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RESTRAINT_TAB =
            CREATIVE_MODE_TABS.register("restraints_creative_mode_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creative_mode_tab." + MODID + ".restraints.title"))
                    .icon(() -> new ItemStack(ModRestraintItems.ROPE.get()))
                    .displayItems((parameters, output) -> {

                        output.accept(ModRestraintItems.ROPE.get());
                        output.accept(ModRestraintItems.TAPE.get());
                        output.accept(ModRestraintItems.SHACKLES.get());
                        output.accept(ModRestraintItems.BALL_GAG.get());
                        output.accept(ModRestraintItems.HARNESS_BALL_GAG.get());
                        output.accept(ModRestraintItems.RING_GAG.get());
                        output.accept(ModRestraintItems.BLINDFOLD_MASK.get());
                        output.accept(ModRestraintItems.LEATHER_COLLAR.get());
                        output.accept(ModRestraintItems.BELL_COLLAR.get());
                        output.accept(ModRestraintItems.CURSED_COLLAR.get());
                        output.accept(ModRestraintItems.LEATHER_CUFFS.get());
                        output.accept(ModRestraintItems.LEATHER_MITTEN.get());
                        output.accept(ModRestraintItems.ARM_BINDER.get());
                        output.accept(ModRestraintItems.SPLIT_BINDER.get());
                        output.accept(ModRestraintItems.K9_CORSET.get());
                        output.accept(ModRestraintItems.SLIME.get());
                        output.accept(ModRestraintItems.LATEX.get());
                        output.accept(ModRestraintItems.MERCHANT_SLIME.get());
                        output.accept(ModRestraintItems.MIRAI_TECH_SUIT.get());
                        output.accept(ModRestraintItems.MIRAI_TECH_GLASS.get());
                        output.accept(ModRestraintItems.MIRAI_TECH_MASK.get());
                        output.accept(ModRestraintItems.MIRAI_TECH_SLEEVES.get());
                        output.accept(ModRestraintItems.MIRAI_TECH_MITTEN.get());
                        output.accept(ModRestraintItems.MIRAI_TECH_BOOT.get());

                        output.accept(ModRestraintItems.MIRAI_TECH_SUIT_REMOTE.get());

                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOCK_AND_KEY_TAB =
            CREATIVE_MODE_TABS.register("locks_and_keys_creative_mode_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creative_mode_tab." + MODID + ".restraint_locks_and_keys.title"))
                    .icon(() -> new ItemStack(ModLockAndKeyItems.IRON_LOCK.get()))
                    .displayItems((parameters, output) -> {

                        output.accept(ModLockAndKeyItems.IRON_LOCK.get());
                        output.accept(ModLockAndKeyItems.IRON_KEY.get());
                        output.accept(ModLockAndKeyItems.PERSONAL_COMMON_LOCK.get());
                        output.accept(ModLockAndKeyItems.PERSONAL_COMMON_KEY.get());

                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RESTRAINT_TOOLS_TAB =
            CREATIVE_MODE_TABS.register("restraint_tools_creative_mode_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creative_mode_tab." + MODID + ".restraint_tools.title"))
                    .icon(() -> new ItemStack(ModRestraintTools.VIBRATOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModRestraintTools.VIBRATOR.get());

                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RESTRAINT_BLOCK_TAB =
            CREATIVE_MODE_TABS.register("restraint_device_creative_mode_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creative_mode_tab." + MODID + ".restraint_device_title"))
                    .icon(() -> ModBlocks.CROSS_MAP.get("oak").get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        addMapToTab(ModBlocks.CROSS_MAP, output);
                        addMapToTab(ModBlocks.REVERSE_CROSS_MAP, output);
                        addMapToTab(ModBlocks.X_CROSS_MAP, output);
                        addMapToTab(ModBlocks.TRIANGLE_HORSE_MAP, output);

                        addMapToTab(ModBlocks.CAGE_MAP, output);
                        addMapToTab(ModBlocks.DOLL_STAND_MAP, output);

                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RESTRAINT_MATERIALS =
            CREATIVE_MODE_TABS.register("restraints_materials_creative_mode_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creative_mode_tab." + MODID + ".restraint_materials_title"))
                    .icon(() -> ModMaterials.RUBBER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModMaterials.RUBBER.get());

                    })
                    .build());

    private static void addMapToTab(Map<String, ? extends DeferredHolder<Block, ? extends Block>> map, CreativeModeTab.Output output) {
        List<String> sortedKeys = map.keySet().stream().sorted().toList();
        for (String key : sortedKeys) {
            DeferredHolder<Block, ? extends Block> holder = map.get(key);
            if (holder != null) {
                output.accept(holder.get());
            }
        }
    }
}