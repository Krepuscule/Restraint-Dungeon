package com.twi.restraint_dungeon.client.hud.restraint_interact;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twi.restraint_dungeon.block.ModBlocks;
import com.twi.restraint_dungeon.block.addon_block.placed_sword.PlacedSwordBlockEntity;
import com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractTargetSelector;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.event.mod_event.restraint.restraint_interact.manager.InteractingProgressManager.currentProgress;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.isBeenBindHands;

@EventBusSubscriber(modid = MODID,value = Dist.CLIENT)
public class RestraintInteractHUD {
    private static final float INNER_RADIUS = 7.0f;
    private static final float OUTER_RADIUS = 10.0f;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        if (currentProgress <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 绘制圆环
        drawCircularBar(graphics, centerX, centerY, INNER_RADIUS, OUTER_RADIUS, currentProgress, 0xAA55FF55, 0x66000000);

        // 获取目标信息并渲染
        InteractTargetSelector.TargetResult target = InteractTargetSelector.getTarget(mc.player, 10.0D);
        if (target != null && target.block() != null) {
            BlockState state = target.block().state();
            ItemStack itemStack = new ItemStack(state.getBlock().asItem());
            int iconSize = 16;
            int iconX = centerX - (iconSize / 2);
            int iconY = centerY + (int)OUTER_RADIUS + 8;

            renderInteractTargetIcon(graphics,mc.player,state,target.block().pos(),itemStack,iconX,iconY,iconSize);

            Component displayText = getInteractDisplayText(mc.player,state,target.block().pos(),itemStack);
            if(displayText != null) {
                int textWidth = mc.font.width(displayText);
                int textX = centerX - textWidth / 2;
                int textY = iconY + iconSize + 6;

                graphics.fill(textX - 4, textY - 2, textX + textWidth + 4, textY + 10, 0x88000000);
                graphics.drawString(mc.font, displayText, textX, textY, 0xFFFFFFFF, true);
            }
        }
        RenderSystem.disableBlend();
    }

    private static void drawCircularBar(GuiGraphics graphics, int x, int y, float innerR, float outerR, float progress, int color, int bgColor) {
        Matrix4f matrix = graphics.pose().last().pose();
        renderAnnulus(matrix, x, y, innerR, outerR, 1.0f, bgColor);
        if (progress > 0.001f) {
            renderAnnulus(matrix, x, y, innerR, outerR, Math.min(progress, 1.0f), color);
        }
    }

    private static void renderAnnulus(Matrix4f matrix, int x, int y, float innerR, float outerR, float percentage, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var buffer = com.mojang.blaze3d.vertex.Tesselator.getInstance().begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLE_STRIP, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR);

        float startAngle = (float) Math.toRadians(-90);
        float sweepAngle = (float) (Math.toRadians(360) * percentage);
        int segments = 64;

        for (int i = 0; i <= segments; i++) {
            float angle = startAngle + (sweepAngle * ((float) i / segments));
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            buffer.addVertex(matrix, x + outerR * cos, y + outerR * sin, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x + innerR * cos, y + innerR * sin, 0).setColor(r, g, b, a);
        }
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static Component getInteractDisplayText(Player player, BlockState state, BlockPos pos, ItemStack itemStack) {

        if(isBeenBindHands(player)) return null;

        Block block = state.getBlock();

        // 插入剑
        if(state.is(Blocks.CRACKED_STONE_BRICKS)
                && player.getMainHandItem().getItem() instanceof SwordItem){
            return Component.translatable("event.restraint_dungeon.interact.display_text.placed_sword")
                    .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
        }

        // 拔出剑
        else if (state.is(ModBlocks.PLACED_SWORD.get())
                && player.getMainHandItem().isEmpty()) {
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof PlacedSwordBlockEntity swordBe) {
                ItemStack storedSword = swordBe.getSword();
                if (!storedSword.isEmpty()) {
                    return Component.translatable("event.restraint_dungeon.interact.display_text.out_sword")
                            .append(storedSword.getHoverName()).withStyle(ChatFormatting.GOLD);
                }
            }
        }

        // 打开容器
        else if(block instanceof ChestBlock
                || block instanceof BarrelBlock
                || block instanceof EnderChestBlock
                || block instanceof ShulkerBoxBlock){

            return Component.translatable("event.restraint_dungeon.interact.display_text.open_container")
                    .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
        }

        // 讲台
        else if(state.is(Blocks.LECTERN)){
            if(player.getMainHandItem().isEmpty() && state.getValue(LecternBlock.HAS_BOOK)){
                return Component.translatable("event.restraint_dungeon.interact.display_text.check_book")
                        .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
            }else if((player.getMainHandItem().is(Items.WRITABLE_BOOK) || player.getMainHandItem().is(Items.WRITTEN_BOOK))
                    && !state.getValue(LecternBlock.HAS_BOOK)){
                return Component.translatable("event.restraint_dungeon.interact.display_text.put_book")
                        .append(player.getMainHandItem().getHoverName()).withStyle(ChatFormatting.GOLD);
            }
        }

        // 红石
        else if (block instanceof ButtonBlock) {
            if(!state.getValue(BlockStateProperties.POWERED)){
                return Component.translatable("event.restraint_dungeon.interact.display_text.redstone_activate")
                        .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
            }
        }else if (block instanceof LeverBlock) {
            if(!state.getValue(BlockStateProperties.POWERED)){
                return Component.translatable("event.restraint_dungeon.interact.display_text.redstone_activate")
                        .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
            }else{
                return Component.translatable("event.restraint_dungeon.interact.display_text.redstone_deactivate")
                        .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
            }
        } else if ((block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof FenceGateBlock)
                && state.hasProperty(BlockStateProperties.OPEN)) {
            if(!state.getValue(BlockStateProperties.OPEN)){
                return Component.translatable("event.restraint_dungeon.interact.display_text.open_door")
                        .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
            }else{
                return Component.translatable("event.restraint_dungeon.interact.display_text.close_door")
                        .append(itemStack.getHoverName()).withStyle(ChatFormatting.GOLD);
            }
        }

        return null;
    }

    private static void renderInteractTargetIcon(GuiGraphics graphics,Player player, BlockState state,BlockPos pos,ItemStack itemStack, int x, int y,int iconSize) {
        ItemStack renderStack = ItemStack.EMPTY;
        Block block = state.getBlock();

        if(isBeenBindHands(player)) return;

        // 插入剑
        if(state.is(Blocks.CRACKED_STONE_BRICKS)
                && player.getMainHandItem().getItem() instanceof SwordItem){
            renderStack = player.getMainHandItem();
        }

        // 拔出剑
        else if (state.is(ModBlocks.PLACED_SWORD.get())
                && player.getMainHandItem().isEmpty()) {
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof PlacedSwordBlockEntity swordBe) {
                ItemStack storedSword = swordBe.getSword();
                if (!storedSword.isEmpty()) {
                    renderStack = storedSword;
                }
            }
        }

        // 打开容器
        else if(block instanceof ChestBlock
                || block instanceof BarrelBlock
                || block instanceof EnderChestBlock
                || block instanceof ShulkerBoxBlock){
            renderStack = itemStack;
        }

        // 讲台
        else if(state.is(Blocks.LECTERN)){
            if(player.getMainHandItem().isEmpty() && state.getValue(LecternBlock.HAS_BOOK)){
                renderStack = itemStack;
            }else if((player.getMainHandItem().is(Items.WRITABLE_BOOK) || player.getMainHandItem().is(Items.WRITTEN_BOOK))
                    && !state.getValue(LecternBlock.HAS_BOOK)){
                renderStack = player.getMainHandItem();
            }
        }

        // 红石
        else if (block instanceof ButtonBlock || block instanceof LeverBlock
                || block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof FenceGateBlock) {
            renderStack = itemStack;
        }

        if(!renderStack.isEmpty()) {
            graphics.renderFakeItem(renderStack, x, y);
            graphics.fill(x - 2, y - 2, x + iconSize + 2, y + iconSize + 2, 0x88000000);
        }
    }
}
