package com.twi.restraint_dungeon.attachment.capability.player_capability;

import com.mojang.serialization.Codec;
import com.twi.restraint_dungeon.animation.utils.AnimationLayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerAnimationData {
    private final Map<AnimationLayer, List<String>> layerSequences = new EnumMap<>(AnimationLayer.class);

    public PlayerAnimationData() {
        for (AnimationLayer layer : AnimationLayer.values()) {
            layerSequences.put(layer, new ArrayList<>());
        }
    }

    public List<String> getSequence(AnimationLayer layer) {
        return layerSequences.computeIfAbsent(layer, k -> new ArrayList<>());
    }

    public void setSequence(AnimationLayer layer, List<String> anims) {
        layerSequences.put(layer, new ArrayList<>(anims));
    }

    public void popAnimation(AnimationLayer layer) {
        List<String> list = layerSequences.get(layer);
        if (list != null && !list.isEmpty()) {
            list.remove(0);
        }
    }

    public boolean isEmpty() {
        return layerSequences.values().stream().allMatch(List::isEmpty);
    }

    public static final Codec<PlayerAnimationData> CODEC = Codec.unboundedMap(
            Codec.string(0, 32).xmap(AnimationLayer::valueOf, AnimationLayer::name),
            Codec.STRING.listOf()
    ).xmap(map -> {
        PlayerAnimationData data = new PlayerAnimationData();
        map.forEach(data::setSequence);
        return data;
    }, data -> {
        Map<AnimationLayer, List<String>> map = new EnumMap<>(AnimationLayer.class);
        for (AnimationLayer layer : AnimationLayer.values()) {
            map.put(layer, data.getSequence(layer));
        }
        return map;
    });
}