package com.twi.restraint_dungeon.animation.utils;

import net.minecraft.resources.ResourceLocation;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public enum AnimationLayer {
    ARMS("player_arms_bind", 1000),
    LEGS("player_legs_bind", 1000),
    BASE_BODY("player_base_body", 1000),
    FULL_BODY("player_full_body", 2000);

    private final ResourceLocation location;
    private final int priority;

    AnimationLayer(String path, int priority) {
        this.location = ResourceLocation.fromNamespaceAndPath(MODID, path);
        this.priority = priority;
    }

    public ResourceLocation getLocation() { return location; }

    public int getPriority() {
        return priority;
    }
}