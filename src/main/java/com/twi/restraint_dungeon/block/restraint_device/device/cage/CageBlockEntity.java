package com.twi.restraint_dungeon.block.restraint_device.device.cage;

import com.twi.restraint_dungeon.block.ModBlockEntities;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDeviceEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CageBlockEntity extends RestraintDeviceEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static final RawAnimation OPENING = RawAnimation.begin().thenPlay("animation.cage.open").thenLoop("animation.cage.opened");
    private static final RawAnimation OPENED = RawAnimation.begin().thenLoop("animation.cage.opened");
    private static final RawAnimation CLOSING = RawAnimation.begin().thenPlay("animation.cage.close").thenLoop("animation.cage.closed");
    private static final RawAnimation CLOSED = RawAnimation.begin().thenLoop("animation.cage.closed");


    @Override
    public String getModelName() {
        return "cage";
    }

    public static class Variant extends CageBlockEntity {
        private final String materialType;

        public Variant(BlockPos pos, BlockState state, String materialType) {
            super(ModBlockEntities.CAGE_BE_MAP.get(materialType).get(), pos, state);
            this.materialType = materialType;
        }

        @Override
        public String getTextureName() {
            return this.materialType + "_cage";
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "cage_controller", 5, state -> {
            BlockState blockState = getBlockState();
            if (blockState.hasProperty(CageBlock.OPEN)) {
                boolean isOpen = blockState.getValue(CageBlock.OPEN);
                String currentAnim = state.getController().getCurrentAnimation() != null 
                        ? state.getController().getCurrentAnimation().animation().name() : "";

                if (isOpen) {
                    if (!currentAnim.equals("animation.cage.open") && !currentAnim.equals("animation.cage.opened")) {
                        return state.setAndContinue(OPENING);
                    }
                    return state.setAndContinue(OPENED);
                } else {
                    if (!currentAnim.equals("animation.cage.close") && !currentAnim.equals("animation.cage.closed")) {
                        return state.setAndContinue(CLOSING);
                    }
                    return state.setAndContinue(CLOSED);
                }
            }
            return state.setAndContinue(CLOSED);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}