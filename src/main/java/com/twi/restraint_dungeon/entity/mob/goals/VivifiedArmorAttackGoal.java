package com.twi.restraint_dungeon.entity.mob.goals;

import com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.VivifiedArmorEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class VivifiedArmorAttackGoal extends Goal {
    private final VivifiedArmorEntity mob;
    private int chargingTicks = 0;
    private LivingEntity target;

    public VivifiedArmorAttackGoal(VivifiedArmorEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void tick() {
        boolean lowHealth = mob.getHealth() < 5.0f;
        AttributeInstance speedAttr = mob.getAttribute(Attributes.MOVEMENT_SPEED);

        if (lowHealth) {
            if (speedAttr != null) speedAttr.setBaseValue(0.35);
            mob.getNavigation().moveTo(target, 1.25);

            double distanceSq = mob.distanceToSqr(target);
            if (distanceSq <= 2.0) {
                chargingTicks++;
                mob.getNavigation().stop();
                mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

                if (chargingTicks >= 20) {
                    performForcedEquip(target);
                    chargingTicks = 0;
                }
            } else {
                chargingTicks = 0;
            }
        } else {
            if (speedAttr != null) speedAttr.setBaseValue(0.25);
            mob.getNavigation().moveTo(target, 1.0);
            if (mob.distanceToSqr(target) <= 4.0) {
                mob.doHurtTarget(target);
            }
        }
    }

    private void performForcedEquip(LivingEntity player) {
        if (player instanceof ServerPlayer serverPlayer) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack armorToEquip = mob.getItemBySlot(slot);
                    if (!armorToEquip.isEmpty()) {
                        ItemStack playerExistingArmor = serverPlayer.getItemBySlot(slot);
                        if (!playerExistingArmor.isEmpty()) {
                            serverPlayer.drop(playerExistingArmor.copy(), false);
                            serverPlayer.setItemSlot(slot, ItemStack.EMPTY);
                        }
                        serverPlayer.setItemSlot(slot, armorToEquip.copy());
                        mob.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
            mob.discard();
        }
    }
}