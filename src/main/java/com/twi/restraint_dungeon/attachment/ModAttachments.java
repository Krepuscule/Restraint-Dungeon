package com.twi.restraint_dungeon.attachment;

import com.twi.restraint_dungeon.attachment.capability.NPCCapability.NPCData;
import com.twi.restraint_dungeon.attachment.capability.common_capability.*;
import com.twi.restraint_dungeon.attachment.capability.player_capability.*;
import com.twi.restraint_dungeon.attachment.restraint_stack.RestraintStack;
import com.twi.restraint_dungeon.attachment.restraint_stack.RestraintTools;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<RestraintCapability>> ENTITY_RESTRAINT =
            ATTACHMENT_TYPES.register("entity_restraint", () -> AttachmentType.builder(RestraintCapability::new)
                    .serialize(RestraintCapability.CODEC)
                    .sync(RestraintCapability.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<PleasantCapability>> ENTITY_PLEASANT =
            ATTACHMENT_TYPES.register("entity_pleasant", () -> AttachmentType.builder(PleasantCapability::new)
                    .serialize(PleasantCapability.CODEC)
                    .sync(PleasantCapability.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<StruggleCapability>> ENTITY_STRUGGLE =
            ATTACHMENT_TYPES.register("entity_struggle", () -> AttachmentType.builder(StruggleCapability::new)
                    .serialize(StruggleCapability.CODEC)
                    .sync(StruggleCapability.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<KidnapCapability>> ENTITY_KIDNAP =
            ATTACHMENT_TYPES.register("entity_kidnap", () -> AttachmentType.builder(KidnapCapability::new)
                    .serialize(KidnapCapability.CODEC)
                    .sync(KidnapCapability.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<SelfBondageCapability>> ENTITY_SELF_BONDAGE =
            ATTACHMENT_TYPES.register("entity_self_bondage", () -> AttachmentType.builder(SelfBondageCapability::new)
                    .serialize(SelfBondageCapability.CODEC)
                    .sync(SelfBondageCapability.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<ReleaseCapability>> ENTITY_RELEASE =
            ATTACHMENT_TYPES.register("entity_release", () -> AttachmentType.builder(ReleaseCapability::new)
                    .serialize(ReleaseCapability.CODEC)
                    .sync(ReleaseCapability.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<PlayerActionCapability>> PLAYER_ACTION =
            ATTACHMENT_TYPES.register("player_action", () -> AttachmentType.builder(PlayerActionCapability::new)
                    .serialize(PlayerActionCapability.CODEC)
                    .sync(PlayerActionCapability.STREAM_CODEC)
                    .build());

    public static final Supplier<AttachmentType<PlayerCarryCapability>> PLAYER_CARRY = ATTACHMENT_TYPES.register(
            "player_carry",
            () -> AttachmentType.builder(PlayerCarryCapability::new)
                    .serialize(PlayerCarryCapability.CODEC)
                    .sync(PlayerCarryCapability.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<AttachmentType<PlayerLeashData>> PLAYER_LEASH = ATTACHMENT_TYPES.register(
            "player_leash",
            () -> AttachmentType.builder(PlayerLeashData::new)
                    .serialize(PlayerLeashData.CODEC)
                    .sync(PlayerLeashData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<PlayerAnimationData>> PLAYER_ANIMATION = ATTACHMENT_TYPES.register(
            "player_animation",
            () -> AttachmentType.builder(PlayerAnimationData::new)
                    .serialize(PlayerAnimationData.CODEC)
                    .build()
    );


    public static final Supplier<AttachmentType<PlayerRestraintOptions>> PLAYER_OPTION = ATTACHMENT_TYPES.register(
            "render_offsets",
            () -> AttachmentType.builder(PlayerRestraintOptions::new)
                    .serialize(PlayerRestraintOptions.CODEC)
                    .sync(PlayerRestraintOptions.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<RestraintStack>> RESTRAINT_STACK =
            ATTACHMENT_TYPES.register("restraint_stack", () -> AttachmentType.builder(RestraintStack::new)
                    .serialize(RestraintStack.CODEC)
                    .sync(RestraintStack.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<RestraintTools>> RESTRAINT_TOOLS =
            ATTACHMENT_TYPES.register("restraint_tools", () -> AttachmentType.builder(RestraintTools::new)
                    .serialize(RestraintTools.CODEC)
                    .sync(RestraintTools.STREAM_CODEC)
                    .copyOnDeath()
                    .build());



    /* ---------------------------------------- NPC 专用 ------------------------------------------------*/


    public static final Supplier<AttachmentType<NPCData>> NPC_DATA = ATTACHMENT_TYPES.register(
            "npc_data",
            () -> AttachmentType.builder(NPCData::new)
                    .sync(NPCData.STREAM_CODEC)
                    .serialize(NPCData.CODEC)
                    .copyOnDeath()
                    .build()
    );
}
