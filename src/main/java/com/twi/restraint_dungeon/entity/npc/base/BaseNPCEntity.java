package com.twi.restraint_dungeon.entity.npc.base;

import com.twi.restraint_dungeon.attachment.ModAttachments;
import com.twi.restraint_dungeon.attachment.capability.NPCCapability.NPCData;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public abstract class BaseNPCEntity extends PathfinderMob implements GeoEntity {

    protected static final String[] BASE_NAMES = {"April", "May", "July"};

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    protected BaseNPCEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }


    /**
     * 获取模组ID
     */
    public String getModId() {
        return MODID;
    }

    /**
     * 获取geo模型文件的位置
     */
    public String getModelLocation() {
        if(isSlimModel()){
            return "geo/npc_slim.geo.json";
        }else{
            return "geo/npc_wide.geo.json";
        }
    }

    /**
     * 获取动画文件的位置
     */
    public String getAnimationLocation() {
        return "animations/npc.animation.json";
    }

    /**
     * 获取贴图的基础文件夹路径
     */
    public String getTextureFolder() {
        return "textures/npc/skins/default/";
    }

    /**
     * 获取皮肤的总数
     */
    protected int getMaxSkinCount() {
        return 7;
    }

    /**
     * 该NPC可否被束缚
     */
    public boolean canBeKidnap() {
        return true;
    }


    /**
     * 获取可用的名字库数组（子类可重写此方法以提供完全不同的名字池）
     */
    protected String[] getNamePool() {
        return BASE_NAMES;
    }


    /**
     * 从当前名字库中随机抽取一个名字
     */
    protected String setRandomName(RandomSource random) {
        String[] pool = getNamePool();
        return pool[random.nextInt(pool.length)];
    }

    /**
     * 随机选择并分配一个皮肤索引
     */
    protected int setRandomSkinIndex(RandomSource random) {
        return random.nextInt(this.getMaxSkinCount());
    }

    /**
     * 设置其是否为Slim类型手臂
     */
    protected boolean setNPCModelSlim() {
        return true;
    }

    /**
     * 获取当前实际的皮肤索引 (如果索引值大于等于最大值，则默认返回最大值)
     */
    public int getSkinIndex() {
        NPCData data = this.getData(ModAttachments.NPC_DATA.get());
        int currentSkin = data.getSkinIndex();

        int maxIndex = this.getMaxSkinCount() - 1;

        if (currentSkin >= maxIndex) {
            return maxIndex;
        }

        return currentSkin;
    }

    /**
     * 获取当前实际的 NPC 名字
     */
    public String getNPCName() {
        return this.getData(ModAttachments.NPC_DATA.get()).getNPCName();
    }

    /**
     * NPC是否使用Slim模型
     */
    public boolean isSlimModel() {

        return this.getData(ModAttachments.NPC_DATA.get()).isSlim();
    }


    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (!this.level().isClientSide()) {
            NPCData currentData = this.getData(ModAttachments.NPC_DATA.get());

            if ("NPC".equals(currentData.getNPCName())) {
                RandomSource random = this.getRandom();
                int randomSkin = this.setRandomSkinIndex(random);
                String randomName = this.setRandomName(random);
                boolean isSlim = setNPCModelSlim();

                NPCData newData = new NPCData();
                newData.setSkinIndex(randomSkin);
                newData.setNPCName(randomName);
                newData.setSlim(isSlim);

                this.setData(ModAttachments.NPC_DATA.get(), newData);

                this.setCustomName(Component.literal(randomName));
                this.setCustomNameVisible(true);
            }
        }
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}