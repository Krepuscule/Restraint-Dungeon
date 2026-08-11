package com.twi.restraint_dungeon.api;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public final class RestraintApiProvider {
    private static IRestraintApi instance;

    private RestraintApiProvider() {}


    public static IRestraintApi get() {
        return Objects.requireNonNull(instance, "Restraint API hasn't been initialized!");
    }


    @ApiStatus.Internal
    public static void register(IRestraintApi apiInstance) {
        if (instance != null) {
            throw new IllegalStateException("Restraint API has been registered");
        }
        instance = apiInstance;
    }
}