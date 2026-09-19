package com.twi.restraint_dungeon.entity.mob;

import com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.VivifiedArmorEntity;
import com.twi.restraint_dungeon.entity.mob.impl.vivifed_armor.renderer.VivifiedArmorRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID)
public class ModMobsRegisterEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModMobs.VIVIFIED_ARMOR.get(), VivifiedArmorEntity.createAttributes().build());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModMobs.VIVIFIED_ARMOR.get(), VivifiedArmorRenderer::new);
    }
}