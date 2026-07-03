package com.twi.restraint_dungeon.client.hud.self_bondage_hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.client.hud.action_hud.ActionSelectMenu;
import com.twi.restraint_dungeon.event.mod_event.self_bondage.SelfBondageClientEvent;
import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import com.twi.restraint_dungeon.item.restraint_tool.RestraintToolItem;
import com.twi.restraint_dungeon.network.payload.player_kidnap.PlayerKidnapActionPayload;
import com.twi.restraint_dungeon.network.payload.player_self_bondage.SelfBondageActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.getAllRestraintsByPart;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintToolsUtils.isToolsFull;

public class SelfBondageMenu extends Screen {
    private final List<ExecuteOption> options = new ArrayList<>();
    private final float innerR = 45f;
    private final float outerR = 105f;
    private final Player player;;

    public record ExecuteOption(Component name, PlayerRestraintPart part,boolean isTool,boolean isNone) {}

    public SelfBondageMenu() {
        super(Component.translatable("hud." + MODID + ".self_bondage_menu"));
        this.player = Minecraft.getInstance().player;

        SelfBondageClientEvent.clearClientCache();

        if(player != null){
            if(player.getMainHandItem().getItem() instanceof RestraintItem ri){
                List<PlayerRestraintPart> partList = ri.getCanEquipPartList();
                if(!partList.isEmpty()){
                    for(int i = 0 ; i < partList.size() ; i++){
                        List<ItemStack> restraints = getAllRestraintsByPart(player,partList.get(i));
                        if(ri.canUseKidnap(player,player,player.getMainHandItem(),partList.get(i),restraints.size()) == null){
                            if(partList.get(i) == PlayerRestraintPart.restraint_connection && ri.canConnectBind(player,player,player.getMainHandItem()) != null){
                                continue;
                            }
                            options.add(new ExecuteOption(Component.translatable(partList.get(i).getTranslationKey())
                                    ,partList.get(i),false,false));
                        }
                    }

                }
            }else if(player.getMainHandItem().getItem() instanceof RestraintToolItem rt && !isToolsFull(player)){
                options.add(new ExecuteOption(Component.translatable("hud." + MODID + ".self_bondage_menu.add_restraint_tool")
                        ,PlayerRestraintPart.restraint_blindfold,true,false));
//                options.add(new ExecuteOption(Component.translatable("hud." + MODID + ".self_bondage_menu.add_restraint_tool_ui")
//                        ,PlayerRestraintPart.restraint_blindfold,true,false));
            }
        }

        if (options.isEmpty()) {
            options.add(new ExecuteOption(
                    Component.translatable("hud." + MODID + ".self_bondage_menu.no_options").withStyle(ChatFormatting.GRAY),
                    null,
                    false,
                    true));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        float cx = width / 2f;
        float cy = height / 2f;
        int entryCount = options.size();
        float sectorStep = 360f / entryCount;
        renderCenterPartInfo(graphics, cx, cy);

        for (int i = 0; i < entryCount; i++) {

            float startAngle = -90f - (i * sectorStep);
            float endAngle = -90f - ((i + 1) * sectorStep);

            ExecuteOption entry = options.get(i);

            boolean hovered;
            if (entry.isNone) {
                hovered = false;
            } else if (entryCount == 1) {
                double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));
                hovered = dist >= innerR && dist <= outerR;
            } else {
                hovered = isMouseInSector(mouseX, mouseY, cx, cy, startAngle, endAngle);
            }

            int color = hovered ? 0xAA444444 : 0x55000000;
            drawRadialSector(graphics, cx, cy, innerR, outerR, startAngle, endAngle, color);

            if (hovered) {
                drawRadialOutline(graphics, cx, cy, innerR, outerR, startAngle, endAngle, 0xFFFFFFFF, entryCount);
            }
            double textRad = Math.toRadians((startAngle + endAngle) / 2f);
            float tx = cx + (float) Math.cos(textRad) * (innerR + outerR) / 2f;
            float ty = cy + (float) Math.sin(textRad) * (innerR + outerR) / 2f;

            int textColor = hovered ? 0xFFFFCC00 : 0xFFFFFF;
            graphics.drawCenteredString(font, entry.name(), (int) tx, (int) ty - 4, textColor);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    @Override public void renderTransparentBackground(@NotNull GuiGraphics guiGraphics) {}

    private void renderCenterPartInfo(GuiGraphics graphics, float cx, float cy) {
        if (player == null) return;
        ItemStack mainHand = player.getMainHandItem();
        ResourceLocation iconResource = null;
        if(mainHand.getItem() instanceof RestraintItem ri){
            iconResource = ri.getItemIconResourceLocation(player,mainHand);
        }else if(mainHand.getItem() instanceof RestraintToolItem rt){
            iconResource = rt.getItemIconResourceLocation(player,mainHand);
        }

        if (iconResource != null) {
            Component itemText = Component.empty();
            graphics.blit(iconResource,(int) cx - 8,(int) cy + 10,0,0,16,16,16,16);

            if(player.getMainHandItem().getItem() instanceof RestraintToolItem rt){
                itemText = Component.translatable("hud." + MODID + ".self_bondage_menu.choose_option").withStyle(ChatFormatting.GOLD);
            }else{
                itemText = Component.translatable("hud." + MODID + ".self_bondage_menu.choose_part").withStyle(ChatFormatting.GOLD);
            }

            graphics.drawCenteredString(font, itemText, (int)cx, (int)cy - 20, 0xFFFFFF);
        }
    }

    private void drawRadialSector(GuiGraphics graphics, float cx, float cy, float r1, float r2, float a1, float a2, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = graphics.pose().last().pose();

        float a = (color >> 24 & 255) / 255f;
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;

        for (float angle = a1; angle >= a2; angle -= 2f) {
            double rad = Math.toRadians(angle);
            buffer.addVertex(matrix, cx + (float)Math.cos(rad) * r1, cy + (float)Math.sin(rad) * r1, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, cx + (float)Math.cos(rad) * r2, cy + (float)Math.sin(rad) * r2, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawRadialOutline(GuiGraphics graphics, float cx, float cy, float r1, float r2, float a1, float a2, int color, int entryCount) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        Matrix4f matrix = graphics.pose().last().pose();
        float a = (color >> 24 & 255) / 255f, r = (color >> 16 & 255) / 255f, g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;

        BufferBuilder buffer;

        if (entryCount == 1) {
            buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (float angle = a1; angle >= a2; angle -= 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r2, cy + (float) Math.sin(rad) * r2, 0).setColor(r, g, b, a);
            }
            BufferUploader.drawWithShader(buffer.buildOrThrow());

            buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (float angle = a1; angle >= a2; angle -= 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r1, cy + (float) Math.sin(rad) * r1, 0).setColor(r, g, b, a);
            }
            BufferUploader.drawWithShader(buffer.buildOrThrow());

        } else {
            buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            double radStart = Math.toRadians(a1);
            buffer.addVertex(matrix, cx + (float) Math.cos(radStart) * r1, cy + (float) Math.sin(radStart) * r1, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, cx + (float) Math.cos(radStart) * r2, cy + (float) Math.sin(radStart) * r2, 0).setColor(r, g, b, a);

            for (float angle = a1; angle >= a2; angle -= 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r2, cy + (float) Math.sin(rad) * r2, 0).setColor(r, g, b, a);
            }

            double radEnd = Math.toRadians(a2);
            buffer.addVertex(matrix, cx + (float) Math.cos(radEnd) * r2, cy + (float) Math.sin(radEnd) * r2, 0).setColor(r, g, b, a);

            for (float angle = a2; angle <= a1; angle += 1f) {
                double rad = Math.toRadians(angle);
                buffer.addVertex(matrix, cx + (float) Math.cos(rad) * r1, cy + (float) Math.sin(rad) * r1, 0).setColor(r, g, b, a);
            }
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.disableBlend();
    }

    private boolean isMouseInSector(double mx, double my, float cx, float cy, float a1, float a2) {
        double dist = Math.sqrt(Math.pow(mx - cx, 2) + Math.pow(my - cy, 2));
        if (dist < innerR || dist > outerR) return false;

        float mouseAngle = (float) Math.toDegrees(Math.atan2(my - cy, mx - cx));

        float nMouse = (mouseAngle % 360 + 360) % 360;
        float nA1 = (a1 % 360 + 360) % 360;
        float nA2 = (a2 % 360 + 360) % 360;

        if (nA1 >= nA2) {
            return nMouse <= nA1 && nMouse >= nA2;
        } else {
            return nMouse <= nA1 || nMouse >= nA2;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && player != null) {
            int entryCount = options.size();
            float sectorStep = 360f / entryCount;
            float cx = width / 2f;
            float cy = height / 2f;

            for (int i = 0; i < entryCount; i++) {
                float startAngle = -90f - (i * sectorStep);
                float endAngle = -90f - ((i + 1) * sectorStep);
                ExecuteOption entry = options.get(i);

                if (entry.isNone()) continue;

                boolean clicked;
                if (entryCount == 1) {
                    double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));
                    clicked = dist >= innerR && dist <= outerR;
                } else {
                    clicked = isMouseInSector(mouseX, mouseY, cx, cy, startAngle, endAngle);
                }

                if (clicked) {
                    SelfBondageClientEvent.clientSelectedPart = entry.part();
                    SelfBondageClientEvent.clientSelectedStack = player.getMainHandItem().copy();

                    PacketDistributor.sendToServer(new SelfBondageActionPayload(true, entry.part().name()));

                    this.onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
