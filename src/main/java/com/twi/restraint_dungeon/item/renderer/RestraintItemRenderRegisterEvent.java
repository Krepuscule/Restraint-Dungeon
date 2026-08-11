package com.twi.restraint_dungeon.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twi.restraint_dungeon.entity.npc.ModNPCs;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID,  value = Dist.CLIENT)
public class RestraintItemRenderRegisterEvent {

    public static final ModelLayerLocation RESTRAINT_PLAYER_MODEL = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MODID, "restraint_player"), "main");
    public static final ModelLayerLocation RESTRAINT_PLAYER_SLIM_MODEL = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MODID, "restraint_player_slim"), "main");

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {

        CubeDeformation restraintDeformation = new CubeDeformation(0.05F);

        LayerDefinition wideLayer = LayerDefinition.create(PlayerModel.createMesh(restraintDeformation, false), 64, 64);
        LayerDefinition slimLayer = LayerDefinition.create(PlayerModel.createMesh(restraintDeformation, true), 64, 64);

        event.registerLayerDefinition(RESTRAINT_PLAYER_MODEL, () -> wideLayer);
        event.registerLayerDefinition(RESTRAINT_PLAYER_SLIM_MODEL, () -> slimLayer);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {

        addCustomLayer(event, PlayerSkin.Model.WIDE, false);
        addCustomLayer(event, PlayerSkin.Model.SLIM, true);

    }

    private static void addCustomLayer(EntityRenderersEvent.AddLayers event, PlayerSkin.Model modelType, boolean slim) {
        PlayerRenderer renderer = event.getSkin(modelType);
        if (renderer != null) {
            PlayerModel<AbstractClientPlayer> model = new PlayerModel<>(
                    event.getEntityModels().bakeLayer(slim ? RESTRAINT_PLAYER_SLIM_MODEL : RESTRAINT_PLAYER_MODEL),
                    slim
            );

            renderer.addLayer(new RestraintItemLayer<>(renderer, model));
        }
    }
}