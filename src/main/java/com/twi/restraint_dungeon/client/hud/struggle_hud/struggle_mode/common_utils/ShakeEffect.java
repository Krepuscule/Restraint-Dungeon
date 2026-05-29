package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.common_utils;

import net.minecraft.resources.ResourceLocation;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ShakeEffect {

    public static final ResourceLocation LOCK_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/restraint_lock.png");


    private long shakeStartTime = 0;
    private long shakeDuration = 0;
    private float intensity = 2.0f;
    private boolean isShaking = false;

    public void startShake(long duration) {
        this.shakeStartTime = System.currentTimeMillis();
        this.shakeDuration = duration;
        this.isShaking = true;
    }

    public void startShakeWithCount(int count, long singleDuration) {
        this.shakeStartTime = System.currentTimeMillis();
        this.shakeDuration = (long) singleDuration * count;
        this.isShaking = true;
    }

    public void stopShake() {
        this.isShaking = false;
        this.shakeStartTime = 0;
    }

    public boolean isActive() {
        if (!isShaking) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - shakeStartTime > shakeDuration) {
            isShaking = false;
            return false;
        }
        return true;
    }

    public float[] getShakeOffset() {
        if (!isActive()) {
            return new float[]{0, 0};
        }

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        // 频率系数，越大震动越快
        float frequency = 20.0f;
        
        float xOffset = (float) Math.sin(elapsed * 0.001f * Math.PI * frequency) * intensity;
        float yOffset = (float) Math.cos(elapsed * 0.001f * Math.PI * (frequency * 0.75f)) * intensity * 0.5f;

        return new float[]{xOffset, yOffset};
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}