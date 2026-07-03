package com.twi.restraint_dungeon.entity.npc;

import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class GenericNPCEntity extends BaseNPCEntity {

    public GenericNPCEntity(EntityType<? extends BaseNPCEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 为 NPC 注入 AI 行为树 (后续你可以在这里添加具体的寻路、交互、跟随等任务)
     */
    @Override
    protected void registerGoals() {
        // 示例：让 NPC 可以在原地闲晃、看向玩家
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
    }

    /**
     * 核心：创建并返回该 NPC 的基础属性（血量、速度等）
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)       // 20点血量 (10颗心)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)   // 移动速度
                .add(Attributes.FOLLOW_RANGE, 16.0D);    // AI 追踪/感知范围
    }
}