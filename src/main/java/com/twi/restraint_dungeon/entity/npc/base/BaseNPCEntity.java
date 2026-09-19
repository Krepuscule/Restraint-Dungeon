package com.twi.restraint_dungeon.entity.npc.base;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.*;
import com.twi.restraint_dungeon.attachment.capability.npc_capability.NPCData;
import com.twi.restraint_dungeon.attachment.capability.player_capability.PlayerCarryCapability;
import com.twi.restraint_dungeon.block.restraint_device.RestraintDevice;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.DialogueTree;
import com.twi.restraint_dungeon.event.mod_event.player_carry.CarryType;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.RestraintDungeon.NULL_UUID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDevice;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.getRestraintDeviceContext;
import static com.twi.restraint_dungeon.utils.mod_utils.carry.PlayerCarryUtils.getCurrentCarryType;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.*;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public abstract class BaseNPCEntity extends PathfinderMob implements GeoEntity {

    private int currentNodeId = -1;       // 记录当前停留的 ConversationNode 的 ID 编号
    private int totalButtonClickCount = 0; // 记录玩家总计点击了多少次对话选项按钮,关闭后重置

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private BlockPos homePos = BlockPos.ZERO;
    private UUID talkingPlayerUUID = NULL_UUID;
    private double homeRadius = 10.0D;
    protected static final Component[] BASE_NAMES = {
            Component.literal("April"),
            Component.literal("May"),
            Component.literal("July"),
    };

    protected BaseNPCEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    /**
     * 获取模组ID
     */
    public String getModId() {
        return MODID;
    }

    public String getModelLocation() {
        if(isSlimModel()){
            return "geo/npc/npc_slim.geo.json";
        }else{
            return "geo/npc/npc_wide.geo.json";
        }
    }

    public String getAnimationLocation() {
        return "animations/npc_animations.json";
    }

    public String getTextureFolder() {
        return "textures/npc/skins/default/";
    }

    protected Component[] getNamePool() {
        return BASE_NAMES;
    }

    protected Component setRandomName(RandomSource random) {
        Component[] pool = getNamePool();
        return pool[random.nextInt(pool.length)];
    }

    protected int setRandomSkinIndex(RandomSource random) {
        return random.nextInt(this.getMaxSkinCount());
    }

    protected boolean setNPCModelSlim() {
        return true;
    }

    protected float setNPCBlindfoldOffset() {
        return 0.0F;
    }

    protected float setNPCGagOffset() {
        return 0.0F;
    }

    public int getSkinIndex() {
        NPCData data = this.getData(ModAttachments.NPC_DATA.get());
        int currentSkin = data.getSkinIndex();
        int maxIndex = this.getMaxSkinCount() - 1;
        if (currentSkin >= maxIndex) {
            return maxIndex;
        }
        return currentSkin;
    }

    public Component getNPCName() {
        return this.getData(ModAttachments.NPC_DATA.get()).getNPCName();
    }

    public boolean isSlimModel() {
        return this.getData(ModAttachments.NPC_DATA.get()).isSlim();
    }

    public float getNPCBlindfoldOffset() {
        return this.getData(ModAttachments.NPC_DATA.get()).getBlindfoldOffset();
    }

    public float getNPCGagOffset() {
        return this.getData(ModAttachments.NPC_DATA.get()).getGagOffset();
    }

    protected int getMaxSkinCount() {
        return 7;
    }

    public boolean canBeKidnap() {
        return false;
    }

    public boolean canBeConversation(Player player,BaseNPCEntity npc) {
        return !isTalking() && getDialogueTree() != null;
    }

    public int getCurrentNodeId() {
        return this.currentNodeId;
    }

    public void setCurrentNodeId(int nodeId) {
        this.currentNodeId = nodeId;
    }

    public int getTotalButtonClickCount() {
        return this.totalButtonClickCount;
    }

    public void setTotalButtonClickCount(int count) {
        this.totalButtonClickCount = count;
    }

    public void incrementButtonClick() {
        this.totalButtonClickCount++;
    }

    public boolean isTalking() {
        return this.talkingPlayerUUID != NULL_UUID;
    }

    public UUID getTalkingPlayerUUID() {
        return this.talkingPlayerUUID;
    }

    /**
     * 获取该 NPC 的对话树。若 NPC 不支持对话，返回 null。
     */
    public DialogueTree getDialogueTree() {
        return null;
    }

    public void startTalkingWith(Player player) {
        if (!this.level().isClientSide()) {
            this.talkingPlayerUUID = player.getUUID();
        }
    }

    public void stopTalking() {
        if (!this.level().isClientSide()) {
            this.talkingPlayerUUID = NULL_UUID;
            this.currentNodeId = -1;
            this.totalButtonClickCount = 0;
        }
    }



    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("HomePos", NbtUtils.writeBlockPos(this.homePos));
        tag.putDouble("HomeRadius", this.homeRadius);
        tag.putInt("CurrentNodeId", this.currentNodeId);
        tag.putInt("TotalButtonClickCount", this.totalButtonClickCount);
        if (this.talkingPlayerUUID != null) {
            tag.putUUID("TalkingPlayerUUID", this.talkingPlayerUUID);
        } else {
            tag.putUUID("TalkingPlayerUUID", NULL_UUID);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomePos")) {
            this.homePos = NbtUtils.readBlockPos(tag, "HomePos").orElse(this.blockPosition());
        } else {
            this.homePos = this.blockPosition();
        }
        if (tag.contains("HomeRadius")) {
            this.homeRadius = tag.getDouble("HomeRadius");
        }
        if (tag.contains("CurrentNodeId")) {
            this.currentNodeId = tag.getInt("CurrentNodeId");
        }
        if (tag.contains("TotalButtonClickCount")) {
            this.totalButtonClickCount = tag.getInt("TotalButtonClickCount");
        }
        if (tag.contains("TalkingPlayerUUID")) {
            this.talkingPlayerUUID = tag.getUUID("TalkingPlayerUUID");
        } else {
            this.talkingPlayerUUID = NULL_UUID;
        }
    }

    @Override
    public void restoreFrom(@NotNull Entity ancestor) {
        super.restoreFrom(ancestor);
        if (ancestor.hasData(ModAttachments.PLAYER_CARRY)) {
            PlayerCarryCapability oldData = ancestor.getData(ModAttachments.PLAYER_CARRY);
            PlayerCarryCapability newData = this.getData(ModAttachments.PLAYER_CARRY);
            newData.copyFrom(oldData);
        }
    }

    public BlockPos getHomePos() {
        return this.homePos;
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos.immutable();
    }

    public double getHomeRadius() {
        return this.homeRadius;
    }

    public void setHomeRadius(double radius) {
        this.homeRadius = radius;
    }


    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.isTalking()) {
            Player player = this.level().getPlayerByUUID(this.talkingPlayerUUID);
            if (player == null || !player.isAlive() || this.distanceToSqr(player) > 16.0D || !this.isAlive()) {
                this.stopTalking();
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean canRide(@NotNull Entity vehicle) {
        if (vehicle instanceof Boat || vehicle instanceof AbstractMinecart) {
            return false;
        }
        return super.canRide(vehicle);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (!this.level().isClientSide()) {
            NPCData currentData = this.getData(ModAttachments.NPC_DATA.get());

            if (this.homePos.equals(BlockPos.ZERO)) {
                this.homePos = this.blockPosition().immutable();
            }

            if (Component.literal("NPC").equals(currentData.getNPCName())) {
                RandomSource random = this.getRandom();
                int randomSkin = this.setRandomSkinIndex(random);
                Component randomName = this.setRandomName(random);
                boolean isSlim = setNPCModelSlim();
                float blindfoldOffset = setNPCBlindfoldOffset();
                float gagOffset = setNPCGagOffset();

                NPCData newData = new NPCData();
                newData.setSkinIndex(randomSkin);
                newData.setNPCName(randomName);
                newData.setSlim(isSlim);
                newData.setBlindfoldOffset(blindfoldOffset);
                newData.setGagOffset(gagOffset);

                this.setData(ModAttachments.NPC_DATA.get(), newData);

                this.setCustomName(randomName);
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return isBeenCollar(this);
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "arms_controller", 0, event -> {
            BaseNPCEntity npc = event.getAnimatable();
            ArmsPose curr = getArmsPose(npc);
            ArmsPose prev = getPrevArmsPose(npc);
            RestraintPosition prevPos = getPreviousPosition(npc);
            RestraintPosition pos = getRestraintPosition(npc);

            if(isChangingPosition(npc) && prevPos != pos){
                if(curr == ArmsPose.NONE){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("arms_none_" + prevPos.toString().toLowerCase() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("arms_none_" + pos.toString().toLowerCase())
                    );
                }else{
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("arms_bind_0" + curr.ordinal() + "_" + prevPos.toString().toLowerCase() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("arms_bind_0" + curr.ordinal() + "_" + pos.toString().toLowerCase())
                    );
                }
            }

            if(isChangingRestraint(npc) == 1 && prev != curr){
                if(prev == ArmsPose.NONE){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("arms_bind_0" + curr.ordinal() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("arms_bind_0" + curr.ordinal() + "_" + pos.toString().toLowerCase())
                    );
                }else{
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("arms_bind_0" + prev.ordinal() + "_back_" + pos.toString().toLowerCase())
                            .thenLoop("arms_none_" + pos.toString().toLowerCase())
                    );
                }
            }

            if(pos == RestraintPosition.CONNECTING){
                ItemStack stack = getFirstConnectBind(npc);
                if(stack.getItem() instanceof RestraintItem restraintItem){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop(restraintItem.getConnectBindAnimation(npc) + "_arms"));
                }
            }else if(pos == RestraintPosition.RIDING){
                Block block = getRestraintDevice(npc);
                RestraintDeviceUtils.DeviceContext context = getRestraintDeviceContext(npc);
                if (context != null && block instanceof RestraintDevice device && device.canBindArms(context.state(), context.pos())) {
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop(device.getID().toLowerCase() + "_arms")
                    );
                }else{
                    if(isBeenBindArms(npc)){
                        return event.setAndContinue(RawAnimation.begin().thenLoop("arms_bind_0" + curr.ordinal() + "_standing"));
                    }else{
                        return event.setAndContinue(RawAnimation.begin().thenLoop("arms_none_standing"));
                    }
                }
            }else if(pos == RestraintPosition.CARRIED){
                CarryType type = getCurrentCarryType(npc);
                if (type != null && isBeenBindArms(npc)) {
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop("arms_bind_0" + curr.ordinal() + "_" + type.getID().toLowerCase() + "_target")
                    );
                }
            }

            if (!isBeenBindArms(npc) && event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk_arms"));
            }

            if(isBeenBindArms(npc)){
                return event.setAndContinue(RawAnimation.begin().thenLoop("arms_bind_0" + curr.ordinal() + "_" + pos.toString().toLowerCase()));
            }else{
                return event.setAndContinue(RawAnimation.begin().thenLoop("arms_none_" + pos.toString().toLowerCase()));
            }
        }));

        controllers.add(new AnimationController<>(this, "legs_controller", 0, event -> {
            BaseNPCEntity npc = event.getAnimatable();
            LegsPose curr = getLegsPose(npc);
            LegsPose prev = getPrevLegsPose(npc);
            RestraintPosition prevPos = getPreviousPosition(npc);
            RestraintPosition pos = getRestraintPosition(npc);

            if(isChangingPosition(npc) && prevPos != pos){
                if(curr == LegsPose.NONE){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("legs_none_" + prevPos.toString().toLowerCase() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("legs_none_" + pos.toString().toLowerCase())
                    );
                }else{
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("legs_bind_0" + curr.ordinal() + "_" + prevPos.toString().toLowerCase() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("legs_bind_0" + curr.ordinal() + "_" + pos.toString().toLowerCase())
                    );
                }
            }

            if(isChangingRestraint(npc) == 2 && prev != curr){
                if(prev == LegsPose.NONE){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("legs_bind_0" + curr.ordinal() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("legs_bind_0" + curr.ordinal() + "_" + pos.toString().toLowerCase())
                    );
                }else{
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("legs_bind_0" + prev.ordinal() + "_back_" + pos.toString().toLowerCase())
                            .thenLoop("legs_none_" + pos.toString().toLowerCase())
                    );
                }
            }

            if(pos == RestraintPosition.CONNECTING){
                ItemStack stack = getFirstConnectBind(npc);
                if(stack.getItem() instanceof RestraintItem restraintItem){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop(restraintItem.getConnectBindAnimation(npc) + "_legs"));
                }
            }else if(pos == RestraintPosition.RIDING){
                Block block = getRestraintDevice(npc);
                RestraintDeviceUtils.DeviceContext context = getRestraintDeviceContext(npc);
                if (context != null && block instanceof RestraintDevice device && device.canBindLegs(context.state(), context.pos())) {
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop(device.getID().toLowerCase() + "_legs")
                    );
                }else{
                    if(isBeenBindLegs(npc)){
                        return event.setAndContinue(RawAnimation.begin().thenLoop("legs_bind_0" + curr.ordinal() + "_standing"));
                    }else{
                        return event.setAndContinue(RawAnimation.begin().thenLoop("legs_none_standing"));
                    }
                }
            }else if(pos == RestraintPosition.CARRIED){
                CarryType type = getCurrentCarryType(npc);
                if (type != null && isBeenBindLegs(npc)) {
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop("legs_bind_0" + curr.ordinal() + "_" + type.getID().toLowerCase() + "_target")
                    );
                }
            }

            if (!isBeenBindLegs(npc) && event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk_legs"));
            }

            if(isBeenBindLegs(npc)){
                return event.setAndContinue(RawAnimation.begin().thenLoop("legs_bind_0" + curr.ordinal() + "_" + pos.toString().toLowerCase()));
            }else{
                return event.setAndContinue(RawAnimation.begin().thenLoop("legs_none_" + pos.toString().toLowerCase()));
            }
        }));

        controllers.add(new AnimationController<>(this, "body_controller", 0, event -> {
            BaseNPCEntity npc = event.getAnimatable();
            RestraintPosition prevPos = getPreviousPosition(npc);
            RestraintPosition pos = getRestraintPosition(npc);

            if(isChangingPosition(npc) && prevPos != pos){
                if(prevPos == RestraintPosition.LYING_RIGHT && pos == RestraintPosition.LYING_DOWN){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("body_" + prevPos.toString().toLowerCase() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("body_" + pos.toString().toLowerCase() + "_02")
                    );
                }else{
                    return event.setAndContinue(RawAnimation.begin()
                            .thenPlay("body_" + prevPos.toString().toLowerCase() + "_to_" + pos.toString().toLowerCase())
                            .thenLoop("body_" + pos.toString().toLowerCase())
                    );
                }
            }

            if(pos == RestraintPosition.CONNECTING){
                ItemStack stack = getFirstConnectBind(npc);
                if(stack.getItem() instanceof RestraintItem restraintItem){
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop(restraintItem.getConnectBindAnimation(npc) + "_body"));
                }
            }else if(pos == RestraintPosition.RIDING){
                Block block = getRestraintDevice(npc);
                RestraintDeviceUtils.DeviceContext context = getRestraintDeviceContext(npc);
                if (context != null && block instanceof RestraintDevice device) {
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop(device.getID().toLowerCase() + "_body")
                    );
                }
            }else if(pos == RestraintPosition.CARRIED){
                CarryType type = getCurrentCarryType(npc);
                if (type != null) {
                    return event.setAndContinue(RawAnimation.begin()
                            .thenLoop("base_body_" + type.getID().toLowerCase() + "_target")
                    );
                }
            }

            return event.setAndContinue(RawAnimation.begin().thenLoop("body_" + pos.toString().toLowerCase()));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}