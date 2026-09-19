package com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.enchantments.ModEnchantments.CURSE_OF_VIVIFICATION;

@EventBusSubscriber(modid = MODID)
public class VivifiedArmorSpawnHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof VivifiedArmorEntity armorEntity) {

            boolean hasArmor = false;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !armorEntity.getItemBySlot(slot).isEmpty()) {
                    hasArmor = true;
                    break;
                }
            }

            if (!hasArmor) {
                populateFullChainmailWithCurse(armorEntity, (ServerLevel) event.getLevel());
            }
        }
    }

    private static void populateFullChainmailWithCurse(VivifiedArmorEntity armorEntity, ServerLevel level) {
        var enchantmentLookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var curseVivificationHolderOpt = enchantmentLookup.get(CURSE_OF_VIVIFICATION);
        var curseBindHolderOpt = enchantmentLookup.get(CURSE_OF_VIVIFICATION);
        var curseVanishHolderOpt = enchantmentLookup.get(CURSE_OF_VIVIFICATION);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack armorStack = getChainmailItem(slot);
                if (!armorStack.isEmpty()) {
                    curseVivificationHolderOpt.ifPresent(holder -> {
                        armorStack.enchant(holder, 1);
                    });

                    curseBindHolderOpt.ifPresent(holder -> {
                        armorStack.enchant(holder, 1);
                    });

                    curseVanishHolderOpt.ifPresent(holder -> {
                        armorStack.enchant(holder, 1);
                    });

                    armorEntity.setItemSlot(slot, armorStack);
                    armorEntity.setDropChance(slot, 0.045F);
                }
            }
        }
    }

    private static ItemStack getChainmailItem(EquipmentSlot slot) {
        Item item = switch (slot) {
            case HEAD -> Items.CHAINMAIL_HELMET;
            case CHEST -> Items.CHAINMAIL_CHESTPLATE;
            case LEGS -> Items.CHAINMAIL_LEGGINGS;
            case FEET -> Items.CHAINMAIL_BOOTS;
            default -> null;
        };

        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }
}