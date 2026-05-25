package com.twi.restraint_dungeon.block.restraint_device;


import com.twi.restraint_dungeon.block.ModBlockEntities;
import com.twi.restraint_dungeon.block.addon_block.placed_sword.PlacedSwordBlockRenderer;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntities;
import com.twi.restraint_dungeon.block.restraint_device.seat_entity.SeatEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID,  value = Dist.CLIENT)
public class ClientBlockRendererEvent {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(SeatEntities.SEAT.get(), SeatEntityRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.PLACED_SWORD_BE.get(), PlacedSwordBlockRenderer::new);

        ModBlockEntities.CROSS_BE_MAP.values().forEach(beType -> {
            event.registerBlockEntityRenderer(beType.get(), context -> new RestraintDeviceRenderer<>());
        });

        ModBlockEntities.REVERSE_CROSS_BE_MAP.values().forEach(beType -> {
            event.registerBlockEntityRenderer(beType.get(), context -> new RestraintDeviceRenderer<>());
        });

        ModBlockEntities.X_CROSS_BE_MAP.values().forEach(beType -> {
            event.registerBlockEntityRenderer(beType.get(), context -> new RestraintDeviceRenderer<>());
        });

        ModBlockEntities.TRIANGLE_HORSE_BE_MAP.values().forEach(beType -> {
            event.registerBlockEntityRenderer(beType.get(), context -> new RestraintDeviceRenderer<>());
        });

        ModBlockEntities.CAGE_BE_MAP.values().forEach(beType -> {
            event.registerBlockEntityRenderer(beType.get(), context -> new RestraintDeviceRenderer<>());
        });

        ModBlockEntities.DOLL_STAND_BE_MAP.values().forEach(beType -> {
            event.registerBlockEntityRenderer(beType.get(), context -> new RestraintDeviceRenderer<>());
        });
    }
}