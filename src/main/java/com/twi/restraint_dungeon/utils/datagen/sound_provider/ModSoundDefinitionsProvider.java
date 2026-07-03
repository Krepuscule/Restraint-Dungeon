package com.twi.restraint_dungeon.utils.datagen.sound_provider;

import com.twi.restraint_dungeon.client.sound.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, MODID, helper);
    }

    @Override
    public void registerSounds() {

        this.add(
            ModSounds.BELL_SWING.get(),
            SoundDefinition.definition()
                .subtitle("subtitles." + MODID + ".bell_swing")
                .with(sound(ResourceLocation.fromNamespaceAndPath(MODID, "items/bell_swing"), SoundDefinition.SoundType.SOUND))
        );

    }
}