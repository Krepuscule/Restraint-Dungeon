package com.twi.restraint_dungeon.item.restraint_item.restraints.mirai_tech;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.effect.ModEffects;
import com.twi.restraint_dungeon.item.DataComponentsUtils;
import com.twi.restraint_dungeon.item.ModDataComponents;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_lock.ModLockAndKeyItems;
import com.twi.restraint_dungeon.item.restraint_lock.lock.MiRaiTechLockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.pleasant.PleasantValueManager.performUpdate;
import static com.twi.restraint_dungeon.utils.mod_utils.pleasant.ThrillUtils.getThrillLevel;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.getAllPartRestraint;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.hasRestraint;
import static com.twi.restraint_dungeon.utils.mod_utils.struggle.StruggleUtils.isNearHookStrugglingState;


public class MiraiTechSuitItem extends RestraintItem {

    public static final RestraintDefaults MIRAI_TECH_SUIT_DEFAULTS = new RestraintDefaults(
            500,
            50.0,
            0.1,
            0.1,
            0.1
    );

    // 模块常量
    public static final String SUFFIX_LOCKED = "_Locked";
    public static final String KEY_AROUSED = "ArousedMode";
    public static final String KEY_DENY = "DenyMode";
    public static final String[] MODULE_KEYS = {
            "ActiveBlindfold", "ActiveGag", "ActiveArmsBind",
            "ActiveHandsBind", "ActiveBodyBind", "ActiveLegsBind"
    };

    public MiraiTechSuitItem(Properties properties) {
        super(properties.stacksTo(1), MIRAI_TECH_SUIT_DEFAULTS);
        this.setCanEquipPartList(List.of(PlayerRestraintPart.restraint_body_bind));
        this.setCanBeLocked(true);
    }


    public UUID getPairingID(ItemStack stack) {
        return stack.get(ModDataComponents.RESTRAINT_PAIRING_ID.get());
    }

    public void setPairingID(ItemStack stack, UUID id) {
        stack.set(ModDataComponents.RESTRAINT_PAIRING_ID.get(), id);
    }

    public boolean isModuleActive(ItemStack stack, int index) {
        if (index < 0 || index >= MODULE_KEYS.length) return false;
        return DataComponentsUtils.getMiraiModules(stack).getOrDefault(MODULE_KEYS[index], false);
    }

    public boolean isModuleLocked(ItemStack stack, int index) {
        if (index < 0 || index >= MODULE_KEYS.length) return false;

        return DataComponentsUtils.getMiraiModules(stack).getOrDefault(MODULE_KEYS[index] + SUFFIX_LOCKED, false);
    }

    public boolean isArousedMode(ItemStack stack) {
        return DataComponentsUtils.getArousedMode(stack);
    }

    public boolean isDenyMode(ItemStack stack) {
        return DataComponentsUtils.getDenyMode(stack);
    }


    public void onArousedEnable(LivingEntity entity, ItemStack stack) {
        updateAllPartsThrill(entity, 3.0);
        if (!entity.level().isClientSide) {
            DataComponentsUtils.updateActivateTime(entity, stack, entity.level().getGameTime());
        }
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    public void onArousedDisable(LivingEntity entity, ItemStack stack) {
        updateAllPartsThrill(entity, 1.0 / 3.0);

        DataComponentsUtils.removeActivateTime(entity, stack);

        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    public void onDenyEnable(LivingEntity entity, ItemStack stack) {
        if (!entity.level().isClientSide) {
            DataComponentsUtils.updateDenyActivateTime(entity, stack, entity.level().getGameTime());
        }

        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 0.8f);
    }

    public void onDenyDisable(LivingEntity entity, ItemStack stack) {
        DataComponentsUtils.removeDenyActivateTime(entity, stack);
        if(entity.hasEffect(ModEffects.CLIMAX_DENY)){
            entity.removeEffect(ModEffects.CLIMAX_DENY);
        }

        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5f, 0.8f);
    }

    private void updateAllPartsThrill(LivingEntity entity, double multiplier) {
        PlayerRestraintPart[] parts = {
                PlayerRestraintPart.restraint_blindfold,
                PlayerRestraintPart.restraint_gag,
                PlayerRestraintPart.restraint_arms_bind,
                PlayerRestraintPart.restraint_hands_bind,
                PlayerRestraintPart.restraint_body_bind,
                PlayerRestraintPart.restraint_legs_bind
        };

        for (PlayerRestraintPart part : parts) {
            ItemStack partStack = getMiRaiTechPartItem(entity, part.toString());
            if (!partStack.isEmpty() && partStack.getItem() instanceof RestraintItem restraint) {
                double currentThrill = restraint.getThrillValue(partStack);
                restraint.setThrillValue(entity,partStack, currentThrill * multiplier);
            }
        }
    }

    @Override
    public double onStrengthStruggle(UUID playerUUID, double ItemStrengthIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearHookStrugglingState(player)) {
                return ItemStrengthIndex * 2.0;
            }
        }
        return super.onStrengthStruggle(playerUUID, ItemStrengthIndex);
    }
    @Override
    public double onLooseStruggle(UUID playerUUID,double ItemLooseIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearHookStrugglingState(player)) {
                return ItemLooseIndex * 2.0;
            }
        }
        return super.onLooseStruggle(playerUUID, ItemLooseIndex);
    }
    @Override
    public double onUnlockStruggle(UUID playerUUID,double ItemLockIndex){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if(isNearHookStrugglingState(player)) {
                return ItemLockIndex * 2.0;
            }
        }
        return super.onLooseStruggle(playerUUID, ItemLockIndex);
    }

    @Override
    public Component canUseKidnap(LivingEntity actionEntity,LivingEntity target,ItemStack stack,PlayerRestraintPart bodyPart,int index){
        if(hasRestraint(target,bodyPart,stack,false)){
            return Component.translatable("item.restraint_dungeon.cant_use_kidnap.mirai_tech").withStyle(ChatFormatting.DARK_RED);
        }
        return null;
    }

    @Override
    public void onRestraintTick(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        if (part != PlayerRestraintPart.restraint_body_bind) return;

        Level level = entity.level();
        if (level.getGameTime() % 20 == 0) {
            validateModuleOnline(entity, stack);
        }

        if (!level.isClientSide) {
            if (isModuleActive(stack, 4)) {
                long activateTime = DataComponentsUtils.getActivateTime(stack);

                long diff = level.getGameTime() - activateTime;
                if (diff > 0 && diff % 1200 == 0) {
                    performUpdate(entity, getThrillLevel(entity));
                }
            } else {
                DataComponentsUtils.removeActivateTime(entity, stack);
            }

            if (isDenyMode(stack)) {
                if (!entity.hasEffect(ModEffects.CLIMAX_DENY)) {
                    entity.addEffect(new MobEffectInstance(ModEffects.CLIMAX_DENY, 600, 0, false, false, true));
                } else {
                    long denyTime = DataComponentsUtils.getDenyActivateTime(stack);
                    if ((level.getGameTime() - denyTime) % 500 == 0) {
                        entity.addEffect(new MobEffectInstance(ModEffects.CLIMAX_DENY, 600, 0, false, false, true));
                    }
                }
            }
        }
    }


    @Override
    public ResourceLocation getTextureResourceLocation(LivingEntity entity, String bodyPart, ItemStack stack,int index,boolean isSlim) {
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        String mode = isArousedMode(stack) ? "_aroused" : "";
        String lock = isModuleLocked(stack, 4) ? "_lock" : "_unlock";

        return ResourceLocation.fromNamespaceAndPath(MODID, 
            "textures/models/restraints/mirai_tech/" + itemName + "/" + bodyPart + "/" + itemName + mode + lock + ".png");
    }

    public ItemStack getMiRaiTechPartItem(LivingEntity entity, String bodyPart) {

        List<ItemStack> stacks = getAllPartRestraint(entity, PlayerRestraintPart.valueOf(bodyPart));
        for(ItemStack stack : stacks){
            if(bodyPart.equals(PlayerRestraintPart.restraint_blindfold.toString()) && stack.getItem() instanceof MiraiTechGlassItem){
                return stack;
            }else if(bodyPart.equals(PlayerRestraintPart.restraint_gag.toString()) && stack.getItem() instanceof MiraiTechMaskItem){
                return stack;
            }else if(bodyPart.equals(PlayerRestraintPart.restraint_arms_bind.toString()) && stack.getItem() instanceof MiraiTechSleevesItem){
                return stack;
            }else if(bodyPart.equals(PlayerRestraintPart.restraint_hands_bind.toString()) && stack.getItem() instanceof MiraiTechMittenItem){
                return stack;
            }else if(bodyPart.equals(PlayerRestraintPart.restraint_body_bind.toString()) && stack.getItem() instanceof MiraiTechSuitItem){
                return stack;
            }else if(bodyPart.equals(PlayerRestraintPart.restraint_legs_bind.toString()) && stack.getItem() instanceof MiraiTechBootItem){
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,
                                                           Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = (hand == InteractionHand.MAIN_HAND) ? player.getOffhandItem() : player.getMainHandItem();

        if (offhand.getItem() instanceof MiraiTechSuitRemote) {
            if (!level.isClientSide) {
                handlePairing(player, offhand, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    private void handlePairing(Player player, ItemStack controller, ItemStack suit) {
        MiraiTechSuitRemote controllerItem = (MiraiTechSuitRemote) controller.getItem();
        if (this.getPairingID(suit) != null) {
            player.displayClientMessage(Component.translatable("item.restraint_dungeon.mirai_tech_suit.pairing_failed").withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        
        UUID newID = UUID.randomUUID();
        this.setPairingID(suit, newID);
        controllerItem.setPairingID(controller, newID);

        player.displayClientMessage(Component.translatable("item.restraint_dungeon.mirai_tech_suit.pairing_success").withStyle(ChatFormatting.GREEN), true);
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
    }

    // --- 状态校验与重置 ---

    public void validateModuleOnline(LivingEntity entity, ItemStack suitStack) {
        if (entity.level().isClientSide) return;

        Map<String, Boolean> currentModules = new HashMap<>(DataComponentsUtils.getMiraiModules(suitStack));
        boolean changed = false;

        // 获取对应的部件映射
        String[] PART_MAPPING = {
                PlayerRestraintPart.restraint_blindfold.toString(),
                PlayerRestraintPart.restraint_gag.toString(),
                PlayerRestraintPart.restraint_arms_bind.toString(),
                PlayerRestraintPart.restraint_hands_bind.toString(),
                PlayerRestraintPart.restraint_body_bind.toString(),
                PlayerRestraintPart.restraint_legs_bind.toString()
        };

        for (int i = 0; i < 6; i++) {
            String bodyPart = PART_MAPPING[i];
            String nbtKey = MODULE_KEYS[i];
            String lockKey = nbtKey + SUFFIX_LOCKED;

            if (!hasMiRaiTechPartItem(entity, bodyPart)) {
                boolean isActive = currentModules.getOrDefault(nbtKey, false);
                boolean isLocked = currentModules.getOrDefault(lockKey, false);

                if (isActive || isLocked) {
                    currentModules.put(nbtKey, false);
                    currentModules.put(lockKey, false);

                    this.onModuleDeactivate(entity, suitStack, i);
                    this.onModuleLockChanged(entity, suitStack, i, false);

                    changed = true;
                }
            }
        }

        if (changed) {
            DataComponentsUtils.updateMiraiModules(entity, suitStack, currentModules);
        }
    }

    public boolean hasMiRaiTechPartItem(LivingEntity entity, String bodyPart) {
        List<ItemStack> stacks = getAllPartRestraint(entity, PlayerRestraintPart.valueOf(bodyPart));
        for (ItemStack stack : stacks) {
            Item item = stack.getItem();
            if (item instanceof MiraiTechGlassItem || item instanceof MiraiTechMaskItem || 
                item instanceof MiraiTechSleevesItem || item instanceof MiraiTechMittenItem ||
                item instanceof MiraiTechSuitItem || item instanceof MiraiTechBootItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当部位被激活时触发
     */
    public void onModuleActivate(LivingEntity entity, ItemStack stack, int index) {

    }

    /**
     * 当部位被解除激活时触发
     */
    public void onModuleDeactivate(LivingEntity entity, ItemStack stack, int index) {

    }

    /**
     * 当部位锁定状态改变时触发
     */
    public void onModuleLockChanged(LivingEntity entity, ItemStack stack, int index, boolean isLocked) {
        if (isLocked) {
            String partName = MODULE_KEYS[index];

            switch (partName) {
                case "ActiveBlindfold" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_blindfold);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechGlassItem restraint) {
                            if (!(restraint.getLockType(entity,stackPart).getItem() instanceof MiRaiTechLockItem)) {
                                restraint.setLockType(entity,stackPart, new ItemStack(ModLockAndKeyItems.MIRAI_TECH_LOCK.get()));
                            }
                        }
                    }
                }
                case "ActiveGag" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_gag);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechMaskItem restraint) {
                            if (!(restraint.getLockType(entity,stackPart).getItem() instanceof MiRaiTechLockItem)) {
                                restraint.setLockType(entity,stackPart, new ItemStack(ModLockAndKeyItems.MIRAI_TECH_LOCK.get()));
                            }
                        }
                    }
                }
                case "ActiveArmsBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_arms_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechSleevesItem restraint) {
                            if (!(restraint.getLockType(entity,stackPart).getItem() instanceof MiRaiTechLockItem)) {
                                restraint.setLockType(entity,stackPart, new ItemStack(ModLockAndKeyItems.MIRAI_TECH_LOCK.get()));
                            }
                        }
                    }
                }
                case "ActiveHandsBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_hands_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechMittenItem restraint) {
                            if (!(restraint.getLockType(entity,stackPart).getItem() instanceof MiRaiTechLockItem)) {
                                restraint.setLockType(entity,stackPart, new ItemStack(ModLockAndKeyItems.MIRAI_TECH_LOCK.get()));
                            }
                        }
                    }
                }
                case "ActiveBodyBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_body_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechSuitItem restraint) {
                            if (!(restraint.getLockType(entity,stackPart).getItem() instanceof MiRaiTechLockItem)) {
                                restraint.setLockType(entity,stackPart, new ItemStack(ModLockAndKeyItems.MIRAI_TECH_LOCK.get()));
                            }
                        }
                    }
                }
                case "ActiveLegsBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_legs_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechBootItem restraint) {
                            if (!(restraint.getLockType(entity,stackPart).getItem() instanceof MiRaiTechLockItem)) {
                                restraint.setLockType(entity,stackPart, new ItemStack(ModLockAndKeyItems.MIRAI_TECH_LOCK.get()));
                            }
                        }
                    }
                }
            }
        }else{
            String partName = MODULE_KEYS[index];

            switch (partName) {
                case "ActiveBlindfold" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_blindfold);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechGlassItem restraint) {
                            if (restraint.getLockType(entity,stackPart) != ItemStack.EMPTY) {
                                restraint.setLockType(entity,stackPart, ItemStack.EMPTY);
                            }
                        }
                    }
                }
                case "ActiveGag" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_gag);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechMaskItem restraint) {
                            if (restraint.getLockType(entity,stackPart) != ItemStack.EMPTY) {
                                restraint.setLockType(entity,stackPart, ItemStack.EMPTY);
                            }
                        }
                    }
                }
                case "ActiveArmsBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_arms_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechSleevesItem restraint) {
                            if (restraint.getLockType(entity,stackPart) != ItemStack.EMPTY) {
                                restraint.setLockType(entity,stackPart, ItemStack.EMPTY);
                            }
                        }
                    }
                }
                case "ActiveHandsBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_hands_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechMittenItem restraint) {
                            if (restraint.getLockType(entity,stackPart) != ItemStack.EMPTY) {
                                restraint.setLockType(entity,stackPart, ItemStack.EMPTY);
                            }
                        }
                    }
                }
                case "ActiveBodyBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_body_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechSuitItem restraint) {
                            if (restraint.getLockType(entity,stackPart) != ItemStack.EMPTY) {
                                restraint.setLockType(entity,stackPart, ItemStack.EMPTY);
                            }
                        }
                    }
                }
                case "ActiveLegsBind" -> {
                    List<ItemStack> partStack = getAllPartRestraint(entity, PlayerRestraintPart.restraint_legs_bind);
                    for (ItemStack stackPart : partStack) {
                        if (stackPart.getItem() instanceof MiraiTechBootItem restraint) {
                            if (restraint.getLockType(entity,stackPart) != ItemStack.EMPTY) {
                                restraint.setLockType(entity,stackPart, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEquip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {

    }

    @Override
    public void onUnequip(LivingEntity entity, ItemStack stack, PlayerRestraintPart part, int index) {
        DataComponentsUtils.removeActivateTime(entity, stack);
        DataComponentsUtils.removeDenyActivateTime(entity, stack);
        resetAllModules(entity,stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.describe.mirai_tech_suit").withStyle(ChatFormatting.GRAY));
        
        UUID id = getPairingID(stack);
        if (id != null) {
            String rawSig = id.toString().substring(0, 8).toUpperCase();

            StringBuilder magicSig = new StringBuilder();
            for (char c : rawSig.toCharArray()) {
                if (Character.isDigit(c)) {
                    magicSig.append((char) ('G' + (c - '0')));
                } else if (c != '-') {
                    magicSig.append(c);
                }
            }

            long time = System.currentTimeMillis() / 50;

            int wave = (int) (Math.sin(time * 0.1) * 52 + 203);

            int r = 80;
            int g = wave;
            int b = 255;

            int color = (r << 16) | (g << 8) | b;

            Style magicStyle = Style.EMPTY
                    .withFont(ResourceLocation.fromNamespaceAndPath("minecraft", "alt"))
                    .withColor(TextColor.fromRgb(color));

            tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.mirai_tech_suit.pairing").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(magicSig)).withStyle(magicStyle)));
        }else {
            tooltip.add(Component.translatable("item.restraint_dungeon.tooltips.mirai_tech_suit.unpairing").withStyle(ChatFormatting.DARK_RED));
        }
    }

    public void resetAllModules(LivingEntity entity, ItemStack stack) {

        Map<String, Boolean> resetModules = new HashMap<>();
        for (String key : MODULE_KEYS) {
            resetModules.put(key, false);
            resetModules.put(key + SUFFIX_LOCKED, false);
        }

        DataComponentsUtils.updateMiraiModules(entity, stack, resetModules);

        DataComponentsUtils.updateArousedMode(entity, stack, false);
        DataComponentsUtils.updateDenyMode(entity, stack, false);

        DataComponentsUtils.removeActivateTime(entity, stack);
        DataComponentsUtils.removeDenyActivateTime(entity, stack);
    }

    public void handlePacketUpdate(LivingEntity target, ItemStack stack, int index, boolean isActivation) {
        if (index == 100) { // Aroused
            DataComponentsUtils.updateArousedMode(target, stack, isActivation);
            if (isActivation) onArousedEnable(target, stack);
            else onArousedDisable(target, stack);

        } else if (index == 101) { // Deny
            DataComponentsUtils.updateDenyMode(target, stack, isActivation);
            if (isActivation) onDenyEnable(target, stack);
            else onDenyDisable(target, stack);

        } else if (index >= 0 && index < MODULE_KEYS.length) {
            String key = MODULE_KEYS[index];
            String lockKey = key + SUFFIX_LOCKED;
            Map<String, Boolean> modules = new HashMap<>(DataComponentsUtils.getMiraiModules(stack));

            if (isActivation) {
                boolean currentActive = modules.getOrDefault(key, false);

                if (!currentActive) {

                    modules.put(key, true);
                    modules.put(lockKey, true);

                    onModuleActivate(target, stack, index);

                    onModuleLockChanged(target, stack, index, true);

                } else {

                    modules.put(key, false);
                    // 若需要关闭即解锁，则取消下一句注释
                    // modules.put(lockKey, false);
                    onModuleDeactivate(target, stack, index);
                }
            } else {
                boolean isCurrentActive = modules.getOrDefault(key, false);

                if (!isCurrentActive) {
                    boolean currentLock = modules.getOrDefault(lockKey, false);
                    boolean newLockState = !currentLock;

                    modules.put(lockKey, newLockState);

                    onModuleLockChanged(target, stack, index, newLockState);
                }
            }
            DataComponentsUtils.updateMiraiModules(target, stack, modules);
        }
    }
}