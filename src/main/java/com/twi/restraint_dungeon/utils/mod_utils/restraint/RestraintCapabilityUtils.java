package com.twi.restraint_dungeon.utils.mod_utils.restraint;


import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.ArmsPose;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.LegsPose;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.player_capability.RestraintRenderOffsets;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getFirstArmsBind;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getFirstLegsBind;

public class RestraintCapabilityUtils {


    public static RestraintCapability getCap(LivingEntity entity) {
        return entity.getData(ModAttachments.ENTITY_RESTRAINT);
    }

    private static void sync(LivingEntity entity, RestraintCapability cap) {
        entity.setData(ModAttachments.ENTITY_RESTRAINT, cap);
    }

    public static ArmsPose getPrevArmsPose(LivingEntity entity) {
        return getCap(entity).getPrevArmsPose();
    }

    public static ArmsPose getArmsPose(LivingEntity entity) {
        return getCap(entity).getArmsPose();
    }

    public static void setArmsPose(LivingEntity entity, ArmsPose pose) {
        var cap = getCap(entity);

        cap.setPrevArmsPose(cap.getArmsPose());
        cap.setArmsPose(pose);
        sync(entity, cap);

    }

    public static LegsPose getPrevLegsPose(LivingEntity entity) {
        return getCap(entity).getPrevLegsPose();
    }

    public static LegsPose getLegsPose(LivingEntity entity) {
        return getCap(entity).getLegsPose();
    }

    public static void setLegsPose(LivingEntity entity, LegsPose pose) {
        var cap = getCap(entity);

        cap.setPrevLegsPose(cap.getLegsPose());
        cap.setLegsPose(pose);
        sync(entity, cap);

    }

    public static RestraintPosition getRestraintPosition(LivingEntity entity) {
        return getCap(entity).getRestraintPosition();
    }

    public static RestraintPosition getPreviousPosition(LivingEntity entity) {
        return getCap(entity).getPreviousPosition();
    }

    public static void updateRestraintPosition(LivingEntity entity, RestraintPosition newPos) {
        var cap = getCap(entity);
        if (cap.getRestraintPosition() != newPos) {
            cap.setPreviousPosition(cap.getRestraintPosition());
            cap.setRestraintPosition(newPos);
            sync(entity, cap);
            entity.refreshDimensions();
        }
    }

    public static boolean isChangingPosition(LivingEntity entity) {
        return getCap(entity).isChangingPosition();
    }

    public static void setChangingPosition(LivingEntity entity, boolean changing) {
        var cap = getCap(entity);
        cap.setChangingPosition(changing);
        sync(entity, cap);
    }


    public static PlayerRestraintPart getTargetPart(LivingEntity entity) {
        return getCap(entity).getTargetPart();
    }

    public static void setTargetPart(LivingEntity entity, PlayerRestraintPart part) {
        var cap = getCap(entity);
        cap.setTargetPart(part);
        sync(entity, cap);
    }


    public static void updatePoseByRestraint(LivingEntity entity) {

        if (getFirstArmsBind(entity).getItem() instanceof RestraintItem ri) {
            setArmsPose(entity, ri.setBindArmsPose(entity));
        } else {
            setArmsPose(entity, ArmsPose.NONE);
        }

        if (getFirstLegsBind(entity).getItem() instanceof RestraintItem ri) {
            setLegsPose(entity, ri.setBindLegsPose(entity));
        } else {
            setLegsPose(entity, LegsPose.NONE);
        }

    }

    public static float getRenderOffset(Player player, PlayerRestraintPart part) {
        if (player == null || part == null) return 0.0F;

        RestraintRenderOffsets offsets = player.getData(ModAttachments.RENDER_OFFSETS);
        if (part == PlayerRestraintPart.restraint_blindfold) {
            return offsets.getBlindfoldOffset();
        } else if (part == PlayerRestraintPart.restraint_gag) {
            return offsets.getGagOffset();
        }
        return 0.0F;
    }


    public static boolean setRenderOffset(Player player, PlayerRestraintPart part, float value) {
        if (player == null || part == null) return false;

        RestraintRenderOffsets offsets = player.getData(ModAttachments.RENDER_OFFSETS);
        boolean changed = false;

        if (part == PlayerRestraintPart.restraint_blindfold) {
            if (offsets.getBlindfoldOffset() != value) {
                offsets.setBlindfoldOffset(value);
                changed = true;
            }
        } else if (part == PlayerRestraintPart.restraint_gag) {
            if (offsets.getGagOffset() != value) {
                offsets.setGagOffset(value);
                changed = true;
            }
        }

        if (changed) {
            player.setData(ModAttachments.RENDER_OFFSETS, offsets);
        }
        return changed;
    }
}
