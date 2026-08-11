package com.twi.restraint_dungeon.item.restraint_item.restraints;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.client.sound.ModSounds;
import com.twi.restraint_dungeon.item.ModDataComponents;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class BellCollarItem extends RestraintItem {

    public static final RestraintDefaults BELL_COLLAR_DEFAULTS = new RestraintDefaults(
            500,
            50.0,
            0.1,
            0.1,
            0.1
    );

    public BellCollarItem(Properties properties) {
        super(properties.stacksTo(1), BELL_COLLAR_DEFAULTS);
        this.setCanEquipPartList(canEquipPartList);
        this.setConnectPartMap(new HashMap<>());
        this.setCanBeLocked(true);
    }

    private final List<PlayerRestraintPart> canEquipPartList = List.of(
            PlayerRestraintPart.restraint_collar
    );

    @Override
    public void onEquip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (!entity.level().isClientSide()) {
            stack.set(ModDataComponents.ACTIVATE_TIME.get(), entity.level().getGameTime());
            entity.level().playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.BELL_SWING,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.0F + (entity.level().random.nextFloat() - entity.level().random.nextFloat()) * 0.2F
            );
        }
    }

    @Override
    public void onUnequip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (!entity.level().isClientSide()) {
            stack.remove(ModDataComponents.ACTIVATE_TIME.get());
            entity.level().playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.BELL_SWING,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.0F + (entity.level().random.nextFloat() - entity.level().random.nextFloat()) * 0.2F
            );
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item." + MODID + ".tooltips.describe.bell_collar").withStyle(ChatFormatting.GRAY));
    }
}
