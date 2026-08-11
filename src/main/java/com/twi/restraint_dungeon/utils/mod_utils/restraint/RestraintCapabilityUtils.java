package com.twi.restraint_dungeon.utils.mod_utils.restraint;


import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.ArmsPose;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.LegsPose;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerRestraintOptions;
import com.twi.restraint_dungeon.event.custom_event.PoseChangeEvent.*;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.event.system_handler_event.LivingEntityServerTaskScheduler;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

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
            if(cap.getRestraintPosition() != RestraintPosition.CARRIED && cap.getRestraintPosition() != RestraintPosition.RIDING){
                cap.setPreviousPosition(cap.getRestraintPosition());
            }
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

    public static int isChangingRestraint(LivingEntity entity){return getCap(entity).isChangingRestraint();}

    public static void setChangingRestraint(LivingEntity entity,int changing){
        var cap = getCap(entity);
        cap.setChangingRestraint(changing);
        sync(entity,cap);
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

        ArmsPose prev_ArmsPose;
        ArmsPose curr_ArmsPose;

        LegsPose prev_LegsPose;
        LegsPose curr_LegsPose;

        if (getFirstArmsBind(entity).getItem() instanceof RestraintItem ri) {
            curr_ArmsPose = ri.setBindArmsPose(entity);
        } else {
            curr_ArmsPose = ArmsPose.NONE;
        }

        if (getFirstLegsBind(entity).getItem() instanceof RestraintItem ri) {
            curr_LegsPose = ri.setBindLegsPose(entity);
        } else {
            curr_LegsPose = LegsPose.NONE;
        }

        if(curr_ArmsPose != getArmsPose(entity)){
            prev_ArmsPose = getArmsPose(entity);
            setChangingRestraint(entity, 1);

            LivingEntityServerTaskScheduler.runDelayed(entity, 20, () -> {
                setChangingRestraint(entity, 0);
            });
            setArmsPose(entity, curr_ArmsPose);
            NeoForge.EVENT_BUS.post(new ArmsPoseChangeEvent(entity,prev_ArmsPose,curr_ArmsPose));
        }

        if(curr_LegsPose != getLegsPose(entity)){
            prev_LegsPose = getLegsPose(entity);
            setChangingRestraint(entity, 2);

            LivingEntityServerTaskScheduler.runDelayed(entity, 20, () -> {
                setChangingRestraint(entity, 0);
            });
            setLegsPose(entity, curr_LegsPose);
            NeoForge.EVENT_BUS.post(new LegsPoseChangeEvent(entity,prev_LegsPose,curr_LegsPose));
        }
    }

    public static float getRenderOffset(Player player, PlayerRestraintPart part) {
        if (player == null || part == null) return 0.0F;

        PlayerRestraintOptions offsets = player.getData(ModAttachments.PLAYER_OPTION);
        if (part == PlayerRestraintPart.restraint_blindfold) {
            return offsets.getBlindfoldOffset();
        } else if (part == PlayerRestraintPart.restraint_gag) {
            return offsets.getGagOffset();
        }
        return 0.0F;
    }


    public static boolean setRenderOffset(Player player, PlayerRestraintPart part, float value) {
        if (player == null || part == null) return false;

        PlayerRestraintOptions offsets = player.getData(ModAttachments.PLAYER_OPTION);
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
            player.setData(ModAttachments.PLAYER_OPTION, offsets);
        }
        return changed;
    }


    public static boolean canOpenTargetInventory(Player player){
        if(player == null) return false;

        PlayerRestraintOptions options = player.getData(ModAttachments.PLAYER_OPTION);

        return options.canOpenInventory();
    }

    public static boolean setCanOpenTargetInventory(Player player,boolean canOpenInventory){
        if(player == null) return false;

        PlayerRestraintOptions options = player.getData(ModAttachments.PLAYER_OPTION);
        boolean changed = false;
        if(options.canOpenInventory() != canOpenInventory){
            options.setCanOpenInventory(canOpenInventory);
            changed = true;
        }
        if(changed){
            player.setData(ModAttachments.PLAYER_OPTION,options);
        }
        return changed;
    }
}
