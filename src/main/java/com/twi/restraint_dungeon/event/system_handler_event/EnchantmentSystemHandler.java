package com.twi.restraint_dungeon.event.system_handler_event;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.enchantments.ModEnchantments;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.addRestraintItem;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.hasRestraint;

@EventBusSubscriber(modid = MODID)
public class EnchantmentSystemHandler {

    @SubscribeEvent
    public static void onVivificationCurseEntityDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(entity instanceof Player) && !(entity instanceof BaseNPCEntity)) return;
        DamageSource source = event.getSource();

        if(!isSpecificDamage(entity,source)) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

            ItemStack armorStack = entity.getItemBySlot(slot);
            if (armorStack.isEmpty()) continue;

            boolean hasCurse = serverLevel.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolder(ModEnchantments.CURSE_OF_VIVIFICATION)
                    .map(holder -> armorStack.getEnchantmentLevel(holder) > 0)
                    .orElse(false);

            if (hasCurse) {
                float triggerChance = getVivificationChance(entity, armorStack, slot, event.getNewDamage(),source);

                if (entity.getRandom().nextFloat() < triggerChance) {
                    ItemStack chainItem = new ItemStack(ModRestraintItems.ROPE.get());
                    PlayerRestraintPart part = mapSlotToRestraintPart(entity,slot, chainItem);
                    if(part == null || !(chainItem.getItem() instanceof RestraintItem)){
                        return;
                    }
                    boolean success = addRestraintItem(entity, part, chainItem);

                    if (success) {
                        if(entity instanceof ServerPlayer player){
                            if(slot == EquipmentSlot.HEAD){
                                player.displayClientMessage(Component.translatable("enchantment." + MODID + ".curse_of_vivification.head.activate").withStyle(ChatFormatting.DARK_RED),true);
                            }else if(slot == EquipmentSlot.CHEST){
                                player.displayClientMessage(Component.translatable("enchantment." + MODID + ".curse_of_vivification.chest.activate").withStyle(ChatFormatting.DARK_RED),true);
                            }else if(slot == EquipmentSlot.LEGS){
                                player.displayClientMessage(Component.translatable("enchantment." + MODID + ".curse_of_vivification.leg.activate").withStyle(ChatFormatting.DARK_RED),true);
                            }else if(slot == EquipmentSlot.FEET){
                                player.displayClientMessage(Component.translatable("enchantment." + MODID + ".curse_of_vivification.feet.activate").withStyle(ChatFormatting.DARK_RED),true);
                            }else{
                                player.displayClientMessage(Component.translatable("enchantment." + MODID + ".curse_of_vivification.activate").withStyle(ChatFormatting.DARK_RED),true);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }


    public static float getVivificationChance(LivingEntity entity, ItemStack stack, EquipmentSlot slot, float damageAmount,DamageSource source) {
        float baseChance = 0.1f;
        float chancePerDamage = 0.05f;

        float calculatedChance = baseChance + (damageAmount * chancePerDamage);

        return net.minecraft.util.Mth.clamp(calculatedChance, 0.0f, 0.5f);
    }

    private static boolean isSpecificDamage(LivingEntity entity,DamageSource source){

        return source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                || source.is(DamageTypes.PLAYER_ATTACK)
                || source.is(DamageTypes.ARROW)
                || source.is(DamageTypes.MOB_PROJECTILE)
                || source.is(DamageTypes.SPIT) 
                || source.is(DamageTypes.FIREWORKS)
                || source.is(DamageTypes.FIREBALL)
                || source.is(DamageTypes.UNATTRIBUTED_FIREBALL)
                || source.is(DamageTypes.WITHER_SKULL)
                || source.is(DamageTypes.THROWN)
                || source.is(DamageTypes.EXPLOSION)
                || source.is(DamageTypes.PLAYER_EXPLOSION)
                || source.is(DamageTypes.SONIC_BOOM);
    }

    private static PlayerRestraintPart mapSlotToRestraintPart(LivingEntity entity,EquipmentSlot slot,ItemStack restraint) {
        if(slot == EquipmentSlot.HEAD){
            if(!hasRestraint(entity,PlayerRestraintPart.restraint_gag,restraint,false)){
                return PlayerRestraintPart.restraint_gag;
            }
        }else if(slot == EquipmentSlot.CHEST){
            if(!hasRestraint(entity,PlayerRestraintPart.restraint_arms_bind,restraint,false)){
                return PlayerRestraintPart.restraint_arms_bind;
            }
            if(!hasRestraint(entity,PlayerRestraintPart.restraint_body_bind,restraint,false)){
                return PlayerRestraintPart.restraint_body_bind;
            }
        }else if(slot == EquipmentSlot.LEGS){
            if(!hasRestraint(entity,PlayerRestraintPart.restraint_legs_bind,restraint,false)){
                return PlayerRestraintPart.restraint_legs_bind;
            }
            if(!hasRestraint(entity,PlayerRestraintPart.restraint_body_bind,restraint,false)){
                return PlayerRestraintPart.restraint_body_bind;
            }
        }else if(slot == EquipmentSlot.FEET){
            if(!hasRestraint(entity,PlayerRestraintPart.restraint_legs_bind,restraint,false)){
                return PlayerRestraintPart.restraint_legs_bind;
            }
        }
        return null;

    }
}
