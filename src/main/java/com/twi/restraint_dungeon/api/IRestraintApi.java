package com.twi.restraint_dungeon.api;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IRestraintApi {

    /**
     * 检查指定部位是否包含目标物品
     * @param entity 目标实体
     * @param part 目标部位
     * @param stack 待检测的 ItemStack
     * @param isStrict 是否要求完全一致 (True: 数据必须完全相同; False: 仅判断物品种类)
     */
    boolean hasRestraint(LivingEntity entity, PlayerRestraintPart part, ItemStack stack, boolean isStrict);

    /**
     * 快速检查某个部位是否穿戴了任何拘束具
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     */
    boolean hasAnyRestraint(LivingEntity entity, PlayerRestraintPart bodyPart);

    /**
     * 获取某个部位当前穿戴的数量
     * @param entity 目标实体,
     * @param bodyPart 目标部位
     */
    int getRestraintCount(LivingEntity entity, PlayerRestraintPart bodyPart);

    /**
     * 获取指定部位的最后一个拘束具
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     */
    ItemStack getPartLastRestraint(LivingEntity entity, PlayerRestraintPart bodyPart);

    /**
     * 根据索引值获取指定部位的拘束具
     * @param entity 目标实体
     * @param bodyPart 目标部位
     * @param index 索引位置
     */
    ItemStack getPartRestraintByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, int index);

    /**
     * 获取指定拘束具在特定部位中的索引位置
     * @param entity 目标实体
     * @param part 目标部位
     * @param stack 待查找的 ItemStack
     * @param isStrict 是否要求完全一致 (True: 数据/NBT必须完全相同; False: 仅判断物品种类)
     * @return 找到的第一个匹配项索引，未找到则返回 -1
     */
    int getRestraintIndex(LivingEntity entity, PlayerRestraintPart part, ItemStack stack, boolean isStrict);

    /**
     * 增加目标部位的拘束具物品(放置于该部位最后一个空位置，返回是否成功替换)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param stack 增加的拘束具的ItemStack
     */
    boolean addRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack);

    /**
     * 增加指定目标位置的拘束具物品(位置需要为空，返回是否成功替换)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param stack 增加的拘束具的ItemStack
     * @param index 索引值
     */
    boolean addRestraintItemByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack, int index);

    /**
     * 移除目标部位最后一个拘束具物品(返回移除的物品)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     */
    ItemStack removeRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart);

    /**
     * 移除目标部位指定索引的拘束具物品(返回移除的物品)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param index 索引值
     */
    ItemStack removeRestraintItemByIndex(LivingEntity entity, PlayerRestraintPart bodyPart, int index);

    /**
     * 更换指定目标位置的拘束具物品(无需检测位置为空，返回是否成功替换)
     * @param entity 目标实体,
     * @param bodyPart 目标部位,
     * @param stack 替换后的拘束具的ItemStack
     * @param index 索引值
     */
    boolean replaceRestraintItem(LivingEntity entity, PlayerRestraintPart bodyPart, ItemStack stack, int index);
}