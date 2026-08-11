package com.twi.restraint_dungeon.block.restraint_device;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.MapCodec;
import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.block.restraint_device.ghost_block.GhostBlockEntity;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntity;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.event.custom_event.RestraintDeviceDismountEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintDeviceMountEvent;
import com.twi.restraint_dungeon.event.custom_event.RestraintPositionChangeEvent;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_position.RestraintPositionEvent.RestraintPosition;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintKeyItem;
import com.twi.restraint_dungeon.item.restraint_lock.RestraintLockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Supplier;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.block_utils.RestraintDeviceUtils.isRidingRestraintDevice;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.getRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintCapabilityUtils.updateRestraintPosition;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.deviceCanBeLock;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.deviceCanBeUnlock;

public class RestraintDevice extends BaseEntityBlock {

    public static final MapCodec<RestraintDevice> CODEC = simpleCodec(props -> new RestraintDevice(props, null));

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected final Map<Direction, VoxelShape> shapeCache = new EnumMap<>(Direction.class);
    protected final Supplier<? extends BlockEntityType<? extends RestraintDeviceEntity>> blockEntityType;

    public RestraintDevice(Properties properties, Supplier<? extends BlockEntityType<? extends RestraintDeviceEntity>> beType) {
        super(properties);
        this.blockEntityType = beType;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        runInitializeShapes();
    }

    public String getID(){
        return "NONE";
    }

    @NotNull
    public ItemStack getLockType(@NotNull Level level, @NotNull BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RestraintDeviceEntity be ? be.getLockType() : ItemStack.EMPTY;
    }

    public void setLockType(@NotNull Level level, @NotNull BlockPos pos, @Nullable ItemStack lockStack) {
        if (level.getBlockEntity(pos) instanceof RestraintDeviceEntity be) {
            be.setLockType(lockStack);
        }
    }

    public Component canBeLocked(LivingEntity entity,RestraintDevice device,BlockPos pos,ItemStack stack,boolean isPlayer){
        return null;
    }

    public Component canBeUnlocked(LivingEntity entity,RestraintDevice device,BlockPos pos,ItemStack stack,boolean isPlayer){
        return null;
    }

    public @Nullable Component lock(LivingEntity entity, RestraintDevice device, BlockPos pos, ItemStack stack,boolean isPlayer) {
        Level level = entity.level();
        if (level.isClientSide) return null;

        if(isPlayer){
            if(stack.getItem() instanceof RestraintLockItem) {
                if(deviceCanBeLock(entity,device,pos,stack,isPlayer) != null){
                    return deviceCanBeLock(entity,device,pos,stack,isPlayer);
                }
                setLockType(level,pos,stack);

            }else{
                return Component.translatable("block." + MODID + ".restraint_device.lock.fail");
            }
        }else{
            setLockType(level,pos,stack);
        }

        onLock(entity, device, pos, stack, isPlayer);
        level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 0.8f);
        return null;
    }

    public @Nullable Component unlock(LivingEntity entity, RestraintDevice device, BlockPos pos, ItemStack stack,boolean isPlayer) {
        Level level = entity.level();
        if (level.isClientSide) return null;

        if(isPlayer){
            if(stack.getItem() instanceof RestraintKeyItem) {
                if(deviceCanBeUnlock(entity,device,pos,stack,isPlayer) != null){
                    return deviceCanBeUnlock(entity,device,pos,stack,isPlayer);
                }
                setLockType(level,pos,ItemStack.EMPTY);

            }else{
                return Component.translatable("block." + MODID + ".restraint_device.unlock.fail");
            }
        }else{
            setLockType(level,pos,ItemStack.EMPTY);
        }

        onUnLock(entity, device, pos, stack, isPlayer);
        level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.2f);
        return null;
    }

    public void onLock(LivingEntity entity, RestraintDevice device, BlockPos pos, ItemStack stack,boolean isPlayer){

    }

    public void onUnLock(LivingEntity entity, RestraintDevice device, BlockPos pos, ItemStack stack,boolean isPlayer){

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return this.blockEntityType != null ? this.blockEntityType.get().create(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;

        return createTickerHelper(type, this.blockEntityType.get(), RestraintDeviceEntity::tick
        );
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // --- 形状处理逻辑 ---

    protected void runInitializeShapes() {
        VoxelShape raw = buildRawShape();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            shapeCache.put(dir, rotateShape(Direction.NORTH, dir, raw));
        }
    }

    private VoxelShape buildRawShape() {
        VoxelShape combined = Shapes.empty();
        for (BBCube cube : getBBCubes()) {
            combined = Shapes.or(combined, Block.box(
                    8 + cube.posX, cube.posY, 8 + cube.posZ,
                    8 + cube.posX + cube.width, cube.posY + cube.height, 8 + cube.posZ + cube.depth
            ));
        }
        return combined;
    }

    protected List<BBCube> getBBCubes() {
        return List.of(new BBCube(-8, 0, -8, 16, 8, 16));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shapeCache.getOrDefault(state.getValue(FACING), Shapes.block());
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return this.getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    @NotNull
    public InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
//        if (!level.isClientSide) {
//            if(!isMountableDevice(level,pos)) return InteractionResult.PASS;
//            if (mount(level, pos, player)) {
//                return InteractionResult.SUCCESS;
//            }
//        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @NotNull
    public ItemInteractionResult useItemOn(@NotNull ItemStack heldItem,
                                           @NotNull BlockState state,
                                           @NotNull Level level,
                                           @NotNull BlockPos pos,
                                           @NotNull Player player,
                                           @NotNull InteractionHand hand,
                                           @NotNull BlockHitResult hit) {


        boolean isLock = heldItem.getItem() instanceof RestraintLockItem;
        boolean isKey = heldItem.getItem() instanceof RestraintKeyItem;
        if (!isLock && !isKey) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            ItemStack currentLock = this.getLockType(level, pos);
            Component feedback = null;

            boolean lockSuccess = false;
            boolean unlockSuccess = false;

            if (currentLock.isEmpty()) {
                if (isLock) {
                    ItemStack lockItem = heldItem.copy();
                    feedback = this.lock(player, this, pos, lockItem, true);
                    if(feedback == null){
                        if(heldItem.getItem() instanceof RestraintLockItem lock
                                && lock.shouldReduceStackWhenUse(player,lockItem)){
                            heldItem.shrink(1);
                        }
                        lockSuccess = true;
                    }
                }
            } else {
                if (isKey) {
                    feedback = this.unlock(player, this, pos, heldItem, true);
                    if(feedback == null){
                        if(currentLock.getItem() instanceof RestraintLockItem lock && heldItem.getItem() instanceof RestraintKeyItem key
                                && lock.shouldDropLockStackWhenUnlock(player,currentLock,heldItem)){

                            int selectedSlot = player.getInventory().selected;
                            ItemStack currentInHand = player.getInventory().getItem(selectedSlot);
                            if (currentInHand.isEmpty()){
                                player.getInventory().setItem(selectedSlot, currentLock.copy());
                            }else if(!player.getInventory().add(currentLock.copy())){
                                player.drop(currentLock.copy(), false);
                            }

                        }
                        unlockSuccess = true;
                    }
                }
            }


            if (feedback != null) {
                player.displayClientMessage(feedback, true);
                return ItemInteractionResult.FAIL;
            }else{
                if(isLock && lockSuccess){
                    player.displayClientMessage(Component.translatable("block." + MODID + ".restraint_device.lock.success")
                            .withStyle(ChatFormatting.GREEN),true);
                }else if (isKey && unlockSuccess){
                    player.displayClientMessage(Component.translatable("block." + MODID + ".restraint_device.unlock.success")
                            .withStyle(ChatFormatting.GREEN),true);
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    public boolean mount(Level level, BlockPos pos, Entity target) {
        if (level.isClientSide) return false;

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RestraintDevice block)) return false;
        if(!block.isMountableDevice(level,pos)) return false;
        if(!(target instanceof LivingEntity living)) return false;

        if(!(target instanceof Player) && !(target instanceof BaseNPCEntity)) return false;

        if (block.canMount(level, pos, target) != null){
            if(target instanceof Player player){
                player.displayClientMessage(block.canMount(level, pos, target),true);
            }
            return false;
        }

        RestraintDeviceMountEvent mountEvent = new RestraintDeviceMountEvent.Pre(living, block, level, pos);
        NeoForge.EVENT_BUS.post(mountEvent);
        if (mountEvent.isCanceled()) {
            return false;
        }

        Direction facing = state.getValue(FACING);
        float finalYaw = block.getSeatYaw(facing);

        double seatX = pos.getX() + 0.5;
        double seatY = pos.getY();
        double seatZ = pos.getZ() + 0.5;

        SeatEntity seat = new SeatEntity(level, seatX, seatY, seatZ, pos);
        seat.setYRot(finalYaw);
        level.addFreshEntity(seat);

        if (living.startRiding(seat,true)) {

            RestraintPosition position = getRestraintPosition(living);
            NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Pre(living, position, RestraintPosition.RIDING, ItemStack.EMPTY));
            updateRestraintPosition(living, RestraintPosition.RIDING);
            NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Post(living, position, RestraintPosition.RIDING, ItemStack.EMPTY));

            living.setYRot(finalYaw);
            living.setXRot(0);

            living.yBodyRot = finalYaw;
            living.yBodyRotO = finalYaw;
            living.setYHeadRot(finalYaw);
            living.yHeadRotO = finalYaw;

            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        finalYaw, 0,
                        Collections.emptySet()
                );
            }else {
                living.absMoveTo(living.getX(), living.getY(), living.getZ(), finalYaw, 0);
                living.setYRot(finalYaw);
                living.setXRot(0);
            }

            NeoForge.EVENT_BUS.post(new RestraintDeviceMountEvent.Post(living, block, level, pos));
            return true;
        }
        seat.discard();
        return false;
    }

    public boolean dismount(Level level, BlockPos pos, LivingEntity rider) {
        if (level.isClientSide) return false;

        if (!(rider.getVehicle() instanceof SeatEntity seat)) return false;

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RestraintDevice block)) return false;

        RestraintDeviceDismountEvent dismountEvent = new RestraintDeviceDismountEvent.Pre(rider, block, level, pos);
        NeoForge.EVENT_BUS.post(dismountEvent);
        if (dismountEvent.isCanceled()) {
            return false;
        }

        Vec3 safePos = findSafeDismountLocation(level, pos, rider);

        rider.stopRiding();

        if (rider instanceof ServerPlayer serverPlayer) {
            serverPlayer.setDeltaMovement(Vec3.ZERO);
            serverPlayer.connection.teleport(
                    safePos.x, safePos.y, safePos.z,
                    serverPlayer.getYRot(), serverPlayer.getXRot(),
                    Collections.emptySet()
            );
        } else {
            rider.setDeltaMovement(Vec3.ZERO);
            rider.teleportTo(safePos.x, safePos.y, safePos.z);
            if (rider instanceof BaseNPCEntity npc) {
                npc.getNavigation().moveTo(safePos.x, safePos.y, safePos.z, 1.0D);
                npc.getNavigation().stop();
            }
        }

        NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Pre(rider,RestraintPosition.RIDING,getDismountRestraintPosition(rider),ItemStack.EMPTY));
        updateRestraintPosition(rider, getDismountRestraintPosition(rider));
        NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Post(rider,RestraintPosition.RIDING,getDismountRestraintPosition(rider),ItemStack.EMPTY));

        seat.discard();
        NeoForge.EVENT_BUS.post(new RestraintDeviceDismountEvent.Post(rider, block,level, pos));
        return true;
    }

    public boolean forceDismount(Level level, BlockPos pos, LivingEntity rider){
        if (level.isClientSide) return false;

        NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Pre(rider,RestraintPosition.RIDING,getDismountRestraintPosition(rider),ItemStack.EMPTY));
        updateRestraintPosition(rider, getDismountRestraintPosition(rider));
        NeoForge.EVENT_BUS.post(new RestraintPositionChangeEvent.Post(rider,RestraintPosition.RIDING,getDismountRestraintPosition(rider),ItemStack.EMPTY));

        rider.stopRiding();

        NeoForge.EVENT_BUS.post(new RestraintDeviceDismountEvent.Post(rider, this,level, pos));
        return true;
    }

    public RestraintPosition getDismountRestraintPosition(LivingEntity rider){
        return RestraintPosition.STANDING;
    }

    private Vec3 findSafeDismountLocation(Level level, BlockPos basePos, LivingEntity rider) {
        BlockState state = level.getBlockState(basePos);
        Direction facing = Direction.SOUTH;

        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            facing = state.getValue(HorizontalDirectionalBlock.FACING);
        }

        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        Direction opposite = facing.getOpposite();

        Direction[] horizontalPriority = new Direction[]{facing, left, right, opposite};

        int[] verticalOffsets = new int[]{0, 1, -1};

        for (int distance = 1; distance <= 2; distance++) {
            for (int yOffset : verticalOffsets) {
                for (Direction dir : horizontalPriority) {

                    BlockPos targetFloorPos = basePos.relative(dir, distance).above(yOffset);

                    if (level.getBlockState(targetFloorPos.above()).isAir() &&
                            level.getBlockState(targetFloorPos.above(2)).isAir()) {

                        BlockState floorState = level.getBlockState(targetFloorPos);

                        if (!floorState.isAir() && floorState.isFaceSturdy(level, targetFloorPos, Direction.UP)) {

                            double surfaceY = targetFloorPos.getY() + floorState.getShape(level, targetFloorPos).max(Direction.Axis.Y);

                            return new Vec3(targetFloorPos.getX() + 0.5, surfaceY, targetFloorPos.getZ() + 0.5);
                        }
                    }
                }
            }
        }

        return new Vec3(basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 0.5);
    }

    public boolean isMountableDevice(Level level,BlockPos pos){
        return true;
    }

    public Component canMount(Level level, BlockPos pos, Entity target) {

        if(!(target instanceof LivingEntity entity)){
            return Component.translatable("block." + MODID + ".restraint_device.mount.fail.invalid_entity")
                    .withStyle(ChatFormatting.DARK_RED);
        }

        if(!(target instanceof Player) && !(target instanceof BaseNPCEntity)){
            return Component.translatable("block." + MODID + ".restraint_device.mount.fail.invalid_entity")
                    .withStyle(ChatFormatting.DARK_RED);
        }

        if(!this.getLockType(level,pos).isEmpty()){
            return Component.translatable("block." + MODID + ".restraint_device.mount.fail.is_locked")
                    .withStyle(ChatFormatting.DARK_RED);
        }
        List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.1));
        if(!seats.isEmpty()){
            return Component.translatable("block." + MODID + ".restraint_device.mount.fail.passenger_full")
                    .withStyle(ChatFormatting.DARK_RED);
        }
        if(getRestraintPosition(entity) == RestraintPosition.CONNECTING){
             return Component.translatable("block." + MODID + ".restraint_device.mount.fail.cant_connecting")
                    .withStyle(ChatFormatting.DARK_RED);
        }

        return null;
    }

    public Component canDismount(Level level, BlockPos pos, Entity rider) {

        if(!(rider instanceof LivingEntity entity)){
            return Component.translatable("block." + MODID + ".restraint_device.dismount.fail.invalid_target")
                    .withStyle(ChatFormatting.DARK_RED);
        }

        if(!(rider instanceof Player) && !(rider instanceof BaseNPCEntity)){
            return Component.translatable("block." + MODID + ".restraint_device.dismount.fail.invalid_target")
                    .withStyle(ChatFormatting.DARK_RED);
        }

        if(!this.getLockType(level,pos).isEmpty()){
            return Component.translatable("block." + MODID + ".restraint_device.dismount.fail.is_locked")
                    .withStyle(ChatFormatting.DARK_RED);
        }
        return null;
    }
    public void onMount(LivingEntity entity, Level level, BlockPos pos) {}
    public void onDismount(LivingEntity entity, Level level, BlockPos pos) {}
    public void onRiderTick(LivingEntity entity, Level level, BlockPos pos) {}

    // --- 幽灵方块管理 ---

    public boolean canPlaceAt(@NotNull BlockState state, LevelReader level, BlockPos pos) {
        VoxelShape shape = getShape(state, level, pos, CollisionContext.empty());
        if (shape.isEmpty()) return true;

        AABB bounds = shape.bounds();
        for (BlockPos targetPos : BlockPos.betweenClosed(
                pos.offset((int) Math.floor(bounds.minX + 0.001), (int) Math.floor(bounds.minY + 0.001), (int) Math.floor(bounds.minZ + 0.001)),
                pos.offset((int) Math.ceil(bounds.maxX - 0.001) - 1, (int) Math.ceil(bounds.maxY - 0.001) - 1, (int) Math.ceil(bounds.maxZ - 0.001) - 1)
        )) {
            if (targetPos.equals(pos)) continue;
            BlockState targetState = level.getBlockState(targetPos);
            if (!targetState.isAir() && !targetState.canBeReplaced()) return false;
        }
        return true;
    }

    protected void placeGhostBlocks(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        VoxelShape shape = getShape(state, level, pos, CollisionContext.empty());
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            for (int x = (int) Math.floor(x1 + 0.001); x < (int) Math.ceil(x2 - 0.001); x++) {
                for (int y = (int) Math.floor(y1 + 0.001); y < (int) Math.ceil(y2 - 0.001); y++) {
                    for (int z = (int) Math.floor(z1 + 0.001); z < (int) Math.ceil(z2 - 0.001); z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            BlockPos target = pos.offset(x, y, z);
                            level.setBlock(target, ModBlocks.GHOST_BLOCK.get().defaultBlockState(), 3);
                            if (level.getBlockEntity(target) instanceof GhostBlockEntity ghostBE) {
                                ghostBE.setMasterPos(pos);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 自适应清理幽灵方块
     */
    protected void removeGhostBlocks(Level level, BlockPos pos, BlockState state) {
        VoxelShape shape = shapeCache.get(state.getValue(FACING));
        if (shape == null) {
            shape = getShape(state, level, pos, CollisionContext.empty());
        }

        if (shape.isEmpty()) return;
        AABB bounds = shape.bounds();

        int minX = (int) Math.floor(bounds.minX + 0.001);
        int maxX = (int) Math.ceil(bounds.maxX - 0.001);
        int minY = (int) Math.floor(bounds.minY + 0.001);
        int maxY = (int) Math.ceil(bounds.maxY - 0.001);
        int minZ = (int) Math.floor(bounds.minZ + 0.001);
        int maxZ = (int) Math.ceil(bounds.maxZ - 0.001);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos targetPos = pos.offset(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.is(ModBlocks.GHOST_BLOCK.get())) {
                        if (level.getBlockEntity(targetPos) instanceof GhostBlockEntity ghostBE) {
                            if (pos.equals(ghostBE.getMasterPos())) {
                                level.removeBlock(targetPos, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        placeGhostBlocks(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {

                List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.05));

                for (SeatEntity seat : seats) {
                    for (Entity passenger : seat.getPassengers()) {
                        if (passenger instanceof LivingEntity living) {
                            this.forceDismount(level,pos,living);
                        }
                    }
                    if (seat.getPassengers().isEmpty()) {
                        seat.discard();
                    }
                }

                level.updateNeighborsAt(pos, state.getBlock());
                removeGhostBlocks(level, pos, state);
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return canPlaceAt(state, level, pos);
    }

    // --- 红石逻辑 ---

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) { return true; }

    @Override
    protected int getSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return state.getAnalogOutputSignal( (Level) level, pos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        LivingEntity rider = null;
        return getSignalStrength(state, level, pos, rider);
    }

    protected int getSignalStrength(BlockState state, Level level, BlockPos pos, @Nullable LivingEntity entity) {
        return 0;
    }

    // --- 内部辅助工具 ---

    protected static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = {shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((x1, y1, z1, x2, y2, z2) -> buffer[1] = Shapes.or(buffer[1],
                    Block.box(16 * (1 - z2), 16 * y1, 16 * x1, 16 * (1 - z1), 16 * y2, 16 * x2)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    public record BBCube(double posX, double posY, double posZ, double width, double height, double depth) {}

    // --- 骑乘参数与逻辑 ---

    public double getBaseOffsetY(Entity passenger) { return 1.0; }
    public double getBaseOffsetX(Entity passenger) { return 0.0; }
    public double getBaseOffsetZ(Entity passenger) { return 0.0; }
    public Vector3f getRiderFirstCameraOffset(){
        return new Vector3f(0.0f,0.0f,0.0f);
    }
    public Vector3f getRiderFirstCameraRotation(){
        return new Vector3f(0.0f,0.0f,0.0f);
    }
    public float getMaxHeadRotation(){
        return 105.0F;
    }
    protected float getSeatYaw(Direction facing) { return facing.toYRot(); }

    public boolean canBlindfold(@NotNull BlockState state, @NotNull BlockPos pos){
        return false;
    }

    public boolean canGag(@NotNull BlockState state, @NotNull BlockPos pos){
        return false;
    }

    public boolean canBindArms(@NotNull BlockState state, @NotNull BlockPos pos){
        return true;
    }

    public boolean canBindLegs(@NotNull BlockState state, @NotNull BlockPos pos){
        return true;
    }

    public boolean canBindHands(@NotNull BlockState state, @NotNull BlockPos pos){
        return true;
    }

    public void renderDeviceBlindfold(Player player, RestraintDevice device, @NotNull BlockState state, @NotNull BlockPos pos, GuiGraphics guiGraphics){
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID,
                "textures/models/blindfold_overlay/restraint_device/" + device.getID().toLowerCase() + ".png");

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        float alpha = 1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        guiGraphics.blit(texture, 0, 0, 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}