package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.gui;

import com.twi.restraint_dungeon.network.payload.npc.conversation.StopTalkingPayload;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class DialogueScreen extends Screen {
    protected boolean isTransitioning = false;
    protected LivingEntity npcEntity;

    protected DialogueScreen(Component title, LivingEntity npc) {
        super(title);
        this.npcEntity = npc;
    }

    public void setTransitioning(boolean value,LivingEntity npc) {

        this.isTransitioning = value;
        this.npcEntity = npc;
    }

    @Override
    public void removed() {
        super.removed();
        if (!isTransitioning) {
            PacketDistributor.sendToServer(new StopTalkingPayload(this.npcEntity.getId()));
        }
    }
}