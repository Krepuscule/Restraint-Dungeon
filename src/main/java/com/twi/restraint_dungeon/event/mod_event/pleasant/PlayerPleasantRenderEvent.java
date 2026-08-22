package com.twi.restraint_dungeon.event.mod_event.pleasant;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.twi.restraint_dungeon.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class PlayerPleasantRenderEvent {

    private static final List<HeartParticle> hearts = new ArrayList<>();

    private static final ResourceLocation HEART_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/climax_heart.png");

    private static final float ZONE_WIDTH_PCT = 0.05f;
    private static final float ZONE_HEIGHT_PCT = 0.05f;
    private static final int RENDER_DENSITY = 40;
    private static long lastSpawnTime = 0;


    private static final float BASE_SHAKE_INTENSITY = 0.25f;
    private static final float VIGNETTE_DEPTH_RATIO = 1.0f / 8.0f;
    private static final int BASE_PINK_COLOR = 0xFFB6C1;
    private static final float BASE_VIGNETTE_ALPHA = 0.18f;



    private static int lastTriggerTick = 0;
    private static float currentShakeStrength = 0.0f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasEffect(ModEffects.CLIMAX)) return;

        MobEffectInstance effect = mc.player.getEffect(ModEffects.CLIMAX);
        int amplifier = effect != null ? effect.getAmplifier() : 0;
        int effectLevel = Math.clamp(amplifier + 1, 1, 10);

        int interval = Math.max(1, Math.round(100.0f - (effectLevel - 1.0f) * 11.0f));

        int currentTick = mc.player.tickCount;

        if (currentTick - lastTriggerTick >= interval) {
            lastTriggerTick = currentTick;
            float mappedLevel = 1.0f + (effectLevel - 1.0f) * (3.0f / 9.0f);
            currentShakeStrength = mappedLevel * BASE_SHAKE_INTENSITY * 1.5f;
        } else {
            currentShakeStrength *= 0.82f;
        }

        long time = System.currentTimeMillis();
        float speed = 0.08f + effectLevel * 0.005f;

        float shakeYaw = (float) (Math.sin(time * speed) * 0.45 * currentShakeStrength);
        float shakePitch = (float) (Math.cos(time * (speed * 1.33f)) * 0.45 * currentShakeStrength);
        float shakeRoll = (float) (Math.sin(time * (speed * 0.67f)) * 0.25 * currentShakeStrength);

        event.setYaw(event.getYaw() + shakeYaw);
        event.setPitch(event.getPitch() + shakePitch);
        event.setRoll(event.getRoll() + shakeRoll);
    }


    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
        if (event.getName() != VanillaGuiLayers.PLAYER_HEALTH) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasEffect(ModEffects.CLIMAX)) return;

        MobEffectInstance effect = mc.player.getEffect(ModEffects.CLIMAX);
        int amplifier = effect != null ? effect.getAmplifier() : 0;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        renderPinkVignette(guiGraphics, screenWidth, screenHeight, amplifier);

        for (HeartParticle heart : hearts) {
            heart.render(guiGraphics, HEART_TEXTURE);
        }
    }


    private static void renderPinkVignette(GuiGraphics gui, int width, int height, int amplifier) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int steps = 40;
        int level = amplifier + 1;
        float progressFactor = Math.min(1.0f, (level - 1) / 9.0f);

        int maxThickness = Math.max(width, height) / 2;

        float targetRatio = VIGNETTE_DEPTH_RATIO + (0.5f - VIGNETTE_DEPTH_RATIO) * progressFactor;

        int baseR = (BASE_PINK_COLOR >> 16) & 0xFF;
        int baseG = (BASE_PINK_COLOR >> 8) & 0xFF;
        int baseB = BASE_PINK_COLOR & 0xFF;

        int r = baseR;
        int g = (int) (baseG * (1.0f - 0.5f * progressFactor));
        int b = (int) (baseB * (1.0f - 0.4f * progressFactor));
        int dynamicPinkColor = (r << 16) | (g << 8) | b;

        for (int i = 0; i < steps; i++) {
            float progress = (float) i / steps;
            float nextProgress = (float) i / steps + (1.0f / steps);

            float currentRatio = progress * 0.5f;

            if (currentRatio > targetRatio) {
                continue;
            }

            float alphaProgress = currentRatio / targetRatio;
            float alpha = (1.0f - alphaProgress) * BASE_VIGNETTE_ALPHA;
            int color = ((int)(alpha * 255) << 24) | dynamicPinkColor;

            int t1 = (int) (maxThickness * progress);
            int t2 = (int) (maxThickness * nextProgress);

            gui.fill(0, t1, width, t2, color);
            gui.fill(0, height - t2, width, height - t1, color);
            gui.fill(t1, 0, t2, height, color);
            gui.fill(width - t2, 0, width - t1, height, color);
        }

        RenderSystem.disableBlend();
    }


    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        MobEffectInstance effectInstance = mc.player.getEffect(ModEffects.CLIMAX);
        if (effectInstance == null) {
            if (!hearts.isEmpty()) hearts.clear();
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        final int zoneWidth = (int) (screenWidth * ZONE_WIDTH_PCT);
        final int zoneHeight = (int) (screenHeight * ZONE_HEIGHT_PCT);

        long currentTime = System.currentTimeMillis();
        int effectLevel = effectInstance.getAmplifier() + 1;
        int spawnIntervalMs = Math.max(50, 2000 / RENDER_DENSITY);

        if (currentTime - lastSpawnTime > spawnIntervalMs) {
            lastSpawnTime = currentTime;
            for (int i = 0; i < effectLevel; i++) {
                float startX = 0, startY = 0;
                int side = mc.level.random.nextInt(4);
                final float s = HeartParticle.SIZE;

                switch (side) {
                    case 0 -> { startX = mc.level.random.nextFloat() * screenWidth; startY = zoneHeight; }
                    case 1 -> { startX = mc.level.random.nextFloat() * screenWidth; startY = screenHeight - s - 1; }
                    case 2 -> { startX = 0; startY = mc.level.random.nextFloat() * screenHeight; }
                    case 3 -> { startX = screenWidth - zoneWidth; startY = mc.level.random.nextFloat() * screenHeight; }
                }
                hearts.add(new HeartParticle(startX, startY, screenWidth, screenHeight, zoneWidth, zoneHeight, side));
            }
        }

        for (int i = hearts.size() - 1; i >= 0; i--) {
            HeartParticle heart = hearts.get(i);
            heart.update();

            if (heart.isDying) {
                if (heart.isDead()) hearts.remove(i);
                continue;
            }

            boolean reached = false;
            final float s = HeartParticle.SIZE;
            switch (heart.originSide) {
                case 0 -> { if (heart.y <= s) reached = true; }
                case 1 -> { if (heart.y <= screenHeight - zoneHeight - s) reached = true; }
                case 2 -> { if (heart.x >= zoneWidth - s) reached = true; }
                case 3 -> { if (heart.x >= screenWidth - s) reached = true; }
            }

            if (reached || heart.x < -s || heart.x > screenWidth || heart.y < -s || heart.y > screenHeight) {
                heart.startDying();
            }
        }
    }

    private static class HeartParticle {
        public static final int SIZE = 16;
        float x, y;
        long startTime;
        long lastUpdate;
        final int originSide;
        final int screenWidth, screenHeight, zoneWidth, zoneHeight;

        private boolean isDying = false;
        private long dieStartTime = 0;
        private final long fadeTimeMs = 600;

        public HeartParticle(float x, float y, int sw, int sh, int zw, int zh, int side) {
            this.x = x; this.y = y;
            this.screenWidth = sw; this.screenHeight = sh;
            this.zoneWidth = zw; this.zoneHeight = zh;
            this.originSide = side;
            this.startTime = System.currentTimeMillis();
            this.lastUpdate = this.startTime;
        }

        public void update() {
            long now = System.currentTimeMillis();
            float delta = (now - lastUpdate) / 1000.0f;
            lastUpdate = now;

            float speed = 25.0f;
            this.x += speed * 0.7f * delta;
            this.y -= speed * 1.0f * delta;
        }

        public void startDying() {
            if (!isDying) {
                isDying = true;
                dieStartTime = System.currentTimeMillis();
            }
        }

        public boolean isDead() {
            return isDying && (System.currentTimeMillis() - dieStartTime >= fadeTimeMs);
        }

        public void render(GuiGraphics gui, ResourceLocation texture) {
            long now = System.currentTimeMillis();
            float alpha;

            if (isDying) {
                alpha = 1.0f - ((float) (now - dieStartTime) / fadeTimeMs);
            } else {
                long age = now - startTime;
                alpha = age < 500 ? (float) age / 500f : 1.0f;
            }

            alpha = Mth.clamp(alpha, 0.0f, 1.0f);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

            gui.blit(texture, (int) x, (int) y, 0, 0, SIZE, SIZE, SIZE, SIZE);

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }
}