package com.twi.restraint_dungeon.utils.datagen.model_provider;

import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.item.materials.ModMaterials;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_lock.ModLockAndKeyItems;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintKeyItem;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintLockItem;
import com.twi.restraint_dungeon.item.restraint_tool.ModRestraintTools;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        ModRestraintItems.RESTRAINT_ITEMS.getEntries().forEach(itemHolder -> {
            String name = itemHolder.getId().getPath();
            generateSimpleItem(itemHolder, "item/restraints/" + name);
        });

        ModLockAndKeyItems.LOCK_AND_KEY_ITEMS.getEntries().forEach(itemHolder -> {
            Item item = itemHolder.get();
            String name = itemHolder.getId().getPath();
            if (item instanceof RestraintLockItem) {
                generateSimpleItem(itemHolder, "item/restraint_locks/" + name);
            } else if (item instanceof RestraintKeyItem) {
                generateSimpleItem(itemHolder, "item/restraint_keys/" + name);
            }
        });

        ModRestraintTools.RESTRAINT_TOOLS.getEntries().forEach(itemHolder -> {
            String name = itemHolder.getId().getPath();
            generateSimpleItem(itemHolder, "item/restraint_tools/" + name);
        });

        ModMaterials.MATERIALS.getEntries().forEach(itemHolder -> {
            String name = itemHolder.getId().getPath();
            generateSimpleItem(itemHolder, "item/materials/" + name);
        });


        ModBlocks.BLOCK_ITEMS.getEntries().forEach(itemHolder -> {
            String name = itemHolder.getId().getPath();

            generateBlockItemModel(name);

        });
    }

    private void generateBlockItemModel(String name) {
        // 基础路径前缀
        String texturePath = "";

        if (name.contains("_wooden_")) {
            String[] parts = name.split("_wooden_");
            if (parts.length > 1) {
                String material = parts[0];
                String type = parts[1];
                texturePath = "block/restraint_device/wooden_" + type + "/icon/" + name;
            }
        } else if (name.endsWith("_cage")) {
            texturePath = "block/restraint_device/cage/icon/" + name;
        } else if (name.endsWith("_doll_stand")) {
            texturePath = "block/restraint_device/doll_stand/icon/" + name;
        }

        // 执行模型生成
        if (!texturePath.isEmpty()) {
            withExistingParent(name, mcLoc("item/generated"))
                    .texture("layer0", modLoc(texturePath));
        }
    }

    private void generateSimpleItem(DeferredHolder<Item, ? extends Item> item, String texturePath) {
        withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc(texturePath));
    }
}