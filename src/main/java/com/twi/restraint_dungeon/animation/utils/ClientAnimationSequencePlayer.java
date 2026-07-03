package com.twi.restraint_dungeon.animation.utils;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

@EventBusSubscriber(modid = MODID,value = Dist.CLIENT)

public class ClientAnimationSequencePlayer {

    private static final Map<AnimationLayer, Map<UUID, ClientAnimationSequencePlayer>> LAYER_MAP = new EnumMap<>(AnimationLayer.class);



    private final UUID playerUUID;

    private final AnimationLayer layer;

    private List<String> currentSequence;

    private int currentIndex = 0;

    private KeyframeAnimationPlayer currentPlayer;



    public ClientAnimationSequencePlayer(UUID playerUUID, AnimationLayer layer) {

        this.playerUUID = playerUUID;

        this.layer = layer;

    }



    public static void play(UUID uuid, List<String> anims, AnimationLayer layer) {

        Map<UUID, ClientAnimationSequencePlayer> players = LAYER_MAP.computeIfAbsent(layer, k -> new WeakHashMap<>());

        ClientAnimationSequencePlayer instance = players.computeIfAbsent(uuid, k -> new ClientAnimationSequencePlayer(uuid, layer));



        instance.start(anims);

    }



    private void start(List<String> anims) {

        this.currentSequence = anims;

        this.currentIndex = 0;

        playNext();

    }



    private void playNext() {

        if (currentSequence == null || currentIndex >= currentSequence.size()) return;



        Player player = Minecraft.getInstance().level.getPlayerByUUID(playerUUID);

        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;



        String name = currentSequence.get(currentIndex);

        KeyframeAnimation anim = (KeyframeAnimation) PlayerAnimationRegistry.getAnimation(

                ResourceLocation.fromNamespaceAndPath(MODID, name));



        if (anim == null) {

            currentIndex++;

            playNext();

            return;

        }



        this.currentPlayer = new KeyframeAnimationPlayer(anim);

        var data = PlayerAnimationAccess.getPlayerAssociatedData(clientPlayer);

        ModifierLayer<IAnimation> animLayer = (ModifierLayer<IAnimation>) data.get(layer.getLocation());



        if (animLayer != null) {

            animLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(0, Ease.LINEAR), currentPlayer);

        }

    }



    public void tick() {

        if (currentPlayer != null && currentPlayer.getCurrentTick() >= currentPlayer.getStopTick() - 4) {

            currentIndex++;

            playNext();

        }

    }



    @SubscribeEvent

    public static void onClientTick(ClientTickEvent.Post event) {

        LAYER_MAP.values().forEach(map -> map.values().forEach(ClientAnimationSequencePlayer::tick));

    }

}
