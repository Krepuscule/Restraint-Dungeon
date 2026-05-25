package com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.ArmsPose;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public class InteractTargetSelector {

    private static final List<IInteractHandler> BLOCK_HANDLERS = new ArrayList<>();

    public static void registerHandler(IInteractHandler handler) {
        BLOCK_HANDLERS.add(handler);
    }

    public static boolean isBackMode(Player player) {
        if(getFirstArmsBind(player).getItem() instanceof RestraintItem restraintItem
                && restraintItem.setBindArmsPose(player) == ArmsPose.CROSS_BEHIND_BACK) {
            return isBeenBindArms(player) && !isBeenBindHands(player);
        }
        return false; 
    }

    public static Direction getLogicFacing(Player player) {
        Direction forward = player.getDirection();
        return isBackMode(player) ? forward.getOpposite() : forward;
    }

    public static void serverAttemptBlockStart(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        BlockState state = player.level().getBlockState(pos);
        for (IInteractHandler handler : BLOCK_HANDLERS) {
            if (handler.isTarget(state)) {
                if (!handler.canDo(player)) return;

                double range = handler.getInteractRange(player, state);
                int time = handler.getInteractTime(player, state);

                if (player.distanceToSqr(Vec3.atCenterOf(pos)) > range * range) return;

                InteractingProgressManager.startOrUpdate(player, pos, null, hand, time, range, (p) -> {
                    if (handler.isTarget(p.level().getBlockState(pos))) {
                        handler.onComplete(p, pos, hand);
                    }
                });
                return;
            }
        }
    }

    public static @Nullable TargetResult getTarget(Player player, double maxRange) {
        Level level = player.level();
        if (isBackMode(player)) {
            Direction backDir = getLogicFacing(player);
            BlockPos[] checkPositions = {
                    player.blockPosition().relative(backDir),
                    player.blockPosition().above().relative(backDir),
                    player.blockPosition(),
                    player.blockPosition().above()
            };

            for (IInteractHandler handler : BLOCK_HANDLERS) {
                for (BlockPos pos : checkPositions) {
                    BlockState state = level.getBlockState(pos);
                    if (handler.isTarget(state)) {
                        return new TargetResult(new TargetContext(pos, state, backDir.getOpposite(), Vec3.atCenterOf(pos)), null, null);
                    }
                }
            }
        } else {
            HitResult hit = player.pick(maxRange, 0.0F, false);
            if (hit instanceof BlockHitResult bHit && bHit.getType() != HitResult.Type.MISS) {
                BlockState state = level.getBlockState(bHit.getBlockPos());
                for (IInteractHandler handler : BLOCK_HANDLERS) {
                    if (handler.isTarget(state)) {
                        return new TargetResult(new TargetContext(bHit.getBlockPos(), state, bHit.getDirection(), bHit.getLocation()), null, null);
                    }
                }
            }
        }
        return null;
    }

    public record TargetContext(BlockPos pos, BlockState state, Direction face, Vec3 hitVec) {}
    public record TargetResult(@Nullable TargetContext block, @Nullable Object entity, @Nullable String extra) {}
}