package com.twi.restraint_dungeon.client.hud.struggle_hud.struggle_mode.common_utils;

import net.minecraft.resources.ResourceLocation;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ShakeEffect {

    public static final ResourceLocation LOCK_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icon/restraint_lock.png");
    
    public static final int LOCK_ICON_SIZE = 15;

    private long shakeStartTime = 0;
    private long shakeDuration = 0;
    private float intensity = 3.0f; 
    private boolean isShaking = false;
    private int shakeCount = 0;

    public void startShake(long duration) {
        this.shakeStartTime = System.currentTimeMillis();
        this.shakeDuration = duration;
        this.isShaking = true;
    }

    public void startShakeWithCount(int count, long singleDuration) {
        this.shakeStartTime = System.currentTimeMillis();
        this.shakeDuration = (long) singleDuration * count;
        this.isShaking = true;
        this.shakeCount = count;
    }

    public void stopShake() {
        this.isShaking = false;
        this.shakeStartTime = 0;
        this.shakeCount = 0;
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

        // 使用当前系统时间计算，保证在渲染帧之间也能平滑过渡
        long elapsed = System.currentTimeMillis() - shakeStartTime;
        // 增加频率系数使震动看起来更“急促”
        float frequency = 40.0f; 
        
        float xOffset = (float) Math.sin(elapsed * 0.001f * Math.PI * frequency) * intensity;
        float yOffset = (float) Math.cos(elapsed * 0.001f * Math.PI * (frequency * 0.75f)) * intensity * 0.5f;

        return new float[]{xOffset, yOffset};
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}