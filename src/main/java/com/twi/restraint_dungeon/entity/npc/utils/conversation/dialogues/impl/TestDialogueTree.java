package com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.impl;

import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.*;
import com.twi.restraint_dungeon.entity.npc.base.BaseNPCEntity;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.DialogueTree;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.ConversationNode;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.nodes.TradeNode;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.ConversationOption;
import com.twi.restraint_dungeon.entity.npc.utils.conversation.dialogues.options.TradeOption;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.ArrayList;
import java.util.List;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;
import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;
import static com.twi.restraint_dungeon.utils.restraint_stack.RestraintStackUtils.dropAndClearPartRestraints;

public class TestDialogueTree extends DialogueTree {

    public TestDialogueTree() {

        ConversationNode mainNode = new ConversationNode("main", null, Component.translatable("npc." + MODID + ".test_npc.node.main"));
        ConversationNode player_gagged_mainNode = new ConversationNode("main_player_gagged", null, Component.translatable("npc." + MODID + ".test_npc.node.main_player_gagged"));
        ConversationNode npc_gagged_mainNode = new ConversationNode("main_npc_gagged", null, Component.translatable("npc." + MODID + ".test_npc.node.main_npc_gagged"));
        ConversationNode both_gagged_mainNode = new ConversationNode("main_both_gagged", null, Component.translatable("npc." + MODID + ".test_npc.node.main_both_gagged"));
        ConversationNode ungagged_mainNode = new ConversationNode("main_ungagged", null, Component.translatable("npc." + MODID + ".test_npc.node.main_ungagged"));
        ConversationNode player_gagged_node = new ConversationNode("player_gagged",null,Component.translatable("npc." + MODID + ".test_npc.node.player_gagged"));

        ConversationNode about_update_node = new ConversationNode("about_update", "main", Component.translatable("npc." + MODID + ".test_npc.node.about_update"));
        ConversationNode about_me_node = new ConversationNode("about_me", "main", Component.translatable("npc." + MODID + ".test_npc.node.about_me"));
        ConversationNode about_restraints_node = new ConversationNode("about_restraints", "main", Component.translatable("npc." + MODID + ".test_npc.node.about_restraints"));
        ConversationNode about_restraint_interact_node = new ConversationNode("about_restraint_interact", "main", Component.translatable("npc." + MODID + ".test_npc.node.about_restraint_interact"));
        ConversationNode about_interact_with_block_node = new ConversationNode("about_interact_with_block", "about_restraint_interact", Component.translatable("npc." + MODID + ".test_npc.node.about_interact_with_block"));
        ConversationNode about_change_position_node = new ConversationNode("about_change_position", "about_restraint_interact", Component.translatable("npc." + MODID + ".test_npc.node.about_change_position"));
        ConversationNode about_restraint_move_node = new ConversationNode("about_restraint_move", "about_restraint_interact", Component.translatable("npc." + MODID + ".test_npc.node.about_restraint_move"));
        ConversationNode about_restraints_info_node = new ConversationNode("about_restraints_info", "about_restraints", Component.translatable("npc." + MODID + ".test_npc.node.about_restraints_info"));
        ConversationNode about_target_part_node = new ConversationNode("about_target_part", "about_restraints_info", Component.translatable("npc." + MODID + ".test_npc.node.about_target_part"));
        ConversationNode about_position_node = new ConversationNode("about_position", "about_restraints_info", Component.translatable("npc." + MODID + ".test_npc.node.about_position"));
        ConversationNode about_kidnap_node = new ConversationNode("about_kidnap", "about_restraints", Component.translatable("npc." + MODID + ".test_npc.node.about_kidnap"));
        ConversationNode about_struggle_node = new ConversationNode("about_struggle", "about_restraints", Component.translatable("npc." + MODID + ".test_npc.node.about_struggle"));
        ConversationNode about_release_node = new ConversationNode("about_release", "about_restraints", Component.translatable("npc." + MODID + ".test_npc.node.about_release"));
        ConversationNode about_locks_node = new ConversationNode("about_locks", "about_restraints", Component.translatable("npc." + MODID + ".test_npc.node.about_locks"));
        ConversationNode about_action_node = new ConversationNode("about_action", "main", Component.translatable("npc." + MODID + ".test_npc.node.about_action"));
        ConversationNode about_anim_action_node = new ConversationNode("about_anim_action", "about_action", Component.translatable("npc." + MODID + ".test_npc.node.about_anim_action"));
        ConversationNode about_carry_action_node = new ConversationNode("about_carry_action", "about_action", Component.translatable("npc." + MODID + ".test_npc.node.about_carry_action"));
        ConversationNode about_carrying_action_node = new ConversationNode("about_carrying_action", "about_action", Component.translatable("npc." + MODID + ".test_npc.node.about_carrying_action"));
        ConversationNode about_restraint_tools_node = new ConversationNode("about_restraint_tools", "main", Component.translatable("npc." + MODID + ".test_npc.node.about_restraint_tools"));
        ConversationNode about_add_tools_node = new ConversationNode("about_add_tools", "about_restraint_tools", Component.translatable("npc." + MODID + ".test_npc.node.about_add_tools"));
        ConversationNode about_update_tools_node = new ConversationNode("about_update_tools", "about_restraint_tools", Component.translatable("npc." + MODID + ".test_npc.node.about_update_tools"));
        ConversationNode about_remove_tools_node = new ConversationNode("about_remove_tools", "about_restraint_tools", Component.translatable("npc." + MODID + ".test_npc.node.about_remove_tools"));
        ConversationNode about_restraint_device_node = new ConversationNode("about_restraint_device", "main", Component.translatable("npc." + MODID + ".test_npc.node.about_restraint_device"));
        ConversationNode about_use_device_node = new ConversationNode("about_use_device", "about_restraint_device", Component.translatable("npc." + MODID + ".test_npc.node.about_use_device"));
        ConversationNode about_leave_device_node = new ConversationNode("about_leave_device", "about_restraint_device", Component.translatable("npc." + MODID + ".test_npc.node.about_leave_device"));

        TradeNode tradeNode = new TradeNode(
                "trade",
                "main",
                Component.translatable("npc." + MODID + ".test_npc.node.trade")
        );

        tradeNode.addOption(new TradeOption(
                0,
                "trade",
                List.of(new ItemStack(Items.EMERALD,1)),
                List.of(false),
                List.of(new ItemStack(Items.PRISMARINE_SHARD,5)),
                null
        ){
            @Override
            public Component setNodeText(Player player,BaseNPCEntity npc,boolean success){
                if(success){
                    return Component.translatable("npc." + MODID + ".test_npc.trade.success_01");
                }else{
                    return Component.translatable("npc." + MODID + ".test_npc.trade.fail_01");
                }
            }
        });

        tradeNode.addOption(new TradeOption(
                0,
                "trade",
                List.of(new ItemStack(Items.EMERALD,1)),
                List.of(false),
                List.of(new ItemStack(Items.PRISMARINE_CRYSTALS,3)),
                null
        ){
            @Override
            public Component setNodeText(Player player,BaseNPCEntity npc,boolean success){
                if(success){
                    return Component.translatable("npc." + MODID + ".test_npc.trade.success_02");
                }else{
                    return Component.translatable("npc." + MODID + ".test_npc.trade.fail_02");
                }
            }
        });

        ItemStack bindCurseBook = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack vanishCurseBook = new ItemStack(Items.ENCHANTED_BOOK, 1);

        var lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);

        if (lookup != null) {
            Holder.Reference<Enchantment> bindCurseHolder = lookup.getOrThrow(Enchantments.BINDING_CURSE);
            Holder.Reference<Enchantment> vanishCurseHolder = lookup.getOrThrow(Enchantments.VANISHING_CURSE);

            EnchantmentHelper.updateEnchantments(bindCurseBook, mutable -> {
                mutable.upgrade(bindCurseHolder, 1);
            });
            EnchantmentHelper.updateEnchantments(vanishCurseBook, mutable -> {
                mutable.upgrade(vanishCurseHolder, 1);
            });
        }

        tradeNode.addOption(new TradeOption(
                0,
                "trade",
                List.of(new ItemStack(Items.DIAMOND_SWORD),new ItemStack(Items.EMERALD,5)),
                List.of(true,false),
                List.of(new ItemStack(Items.TRIDENT)),
                null
        ){
            @Override
            public Component setNodeText(Player player,BaseNPCEntity npc,boolean success){
                if(success){
                    return Component.translatable("npc." + MODID + ".test_npc.trade.success_03");
                }else{
                    return Component.translatable("npc." + MODID + ".test_npc.trade.fail_03");
                }
            }
        });

        tradeNode.addOption(new TradeOption(
                0,
                "trade",
                List.of(new ItemStack(Items.BOOK),new ItemStack(Items.EMERALD,5)),
                List.of(false,false),
                List.of(bindCurseBook),
                null
        ){
            @Override
            public Component setNodeText(Player player,BaseNPCEntity npc,boolean success){
                if(success){
                    return Component.translatable("npc." + MODID + ".test_npc.trade.success_04");
                }else{
                    return Component.translatable("npc." + MODID + ".test_npc.trade.fail_04");
                }
            }
        });

        tradeNode.addOption(new TradeOption(
                0,
                "trade",
                List.of(new ItemStack(Items.BOOK),new ItemStack(Items.EMERALD,5)),
                List.of(false,false),
                List.of(vanishCurseBook),
                null
        ){
            @Override
            public Component setNodeText(Player player,BaseNPCEntity npc,boolean success){
                if(success){
                    return Component.translatable("npc." + MODID + ".test_npc.trade.success_04");
                }else{
                    return Component.translatable("npc." + MODID + ".test_npc.trade.fail_04");
                }
            }
        });

        tradeNode.addOption(new TradeOption(
                -1,
                "main",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                Component.translatable("npc." + MODID + ".test_npc.option.return_main")
        ));

        tradeNode.addOption(new TradeOption(
                -2,
                "trade",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                Component.translatable("npc." + MODID + ".test_npc.option.trade_leave")
        ));


        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_update"), 0, "about_update"));
        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_me"), 0, "about_me"));
        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.trade"), 0, "trade"));
        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraints"), 0, "about_restraints"));
        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraint_interact"), 0, "about_restraint_interact"));
        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_action"), 0, "about_action"));
        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraint_tools"), 0, "about_restraint_tools"));
        mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraint_device"), 0, "about_restraint_device"));
        mainNode.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        player_gagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_01"), 0, "player_gagged"));
        player_gagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_02"), 0, "player_gagged"));
        player_gagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_03"), 0, "player_gagged"));
        player_gagged_mainNode.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_leave"),
                -2,
                ""));

        player_gagged_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_01"), 0, "player_gagged"));
        player_gagged_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_02"), 0, "player_gagged"));
        player_gagged_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_03"), 0, "player_gagged"));
        player_gagged_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_leave"),
                -2,
                ""));

        npc_gagged_mainNode.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.npc_ungag"),
                1,
                "main_ungagged"){
            @Override
            protected boolean setCanUse(ServerPlayer player, BaseNPCEntity npc) {
                return !isBeenBindArms(player) && !isBeenBindHands(player);
            }

            @Override
            protected String setNextNode(ServerPlayer player, BaseNPCEntity npc) {
                if(isBeenGag(player)){
                    return "main_player_gagged";
                }else{
                    return "main_ungagged";
                }
            }

            @Override
            public void onClick(Player player, BaseNPCEntity npc) {
                if (!npc.level().isClientSide && !isBeenBindArms(player) && !isBeenBindHands(player)) {
                    dropAndClearPartRestraints(npc, PlayerRestraintPart.restraint_gag);
                }
            }
        });
        npc_gagged_mainNode.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.npc_gagged_leave"),
                -2,
                ""
        ));

        both_gagged_mainNode.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.npc_ungag"),
                1,
                "main_ungagged"){
            @Override
            protected boolean setCanUse(ServerPlayer player, BaseNPCEntity npc) {
                return !isBeenBindArms(player) && !isBeenBindHands(player);
            }

            @Override
            protected String setNextNode(ServerPlayer player, BaseNPCEntity npc) {
                if(isBeenGag(player)){
                    return "main_player_gagged";
                }else{
                    return "main_ungagged";
                }
            }

            @Override
            public void onClick(Player player, BaseNPCEntity npc) {
                if (!npc.level().isClientSide && !isBeenBindArms(player) && !isBeenBindHands(player)) {
                    dropAndClearPartRestraints(npc, PlayerRestraintPart.restraint_gag);
                }
            }
        });
        both_gagged_mainNode.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.player_gagged_leave"),
                -2,
                ""
        ));

        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_update"), 0, "about_update"));
        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_me"), 0, "about_me"));
        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.trade"), 0, "trade"));
        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraints"), 0, "about_restraints"));
        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraint_interact"), 0, "about_restraint_interact"));
        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_action"), 0, "about_action"));
        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraint_tools"), 0, "about_restraint_tools"));
        ungagged_mainNode.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraint_device"), 0, "about_restraint_device"));
        ungagged_mainNode.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_update_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_main"),
                -1,
                ""
        ));
        about_update_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_me_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_main"),
                -1,
                ""
        ));
        about_me_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_restraints_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraints_info"), 0, "about_restraints_info"));
        about_restraints_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_kidnap"), 0, "about_kidnap"));
        about_restraints_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_struggle"), 0, "about_struggle"));
        about_restraints_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_release"), 0, "about_release"));
        about_restraints_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_locks"), 0, "about_locks"));
        about_restraints_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_main"),
                -1,
                ""
        ));
        about_restraints_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_restraints_info_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_target_part"), 0, "about_target_part"));
        about_restraints_info_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_position"), 0, "about_position"));
        about_restraints_info_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraints"),
                -1,
                ""
        ));
        about_restraints_info_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_target_part_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraints_info"),
                -1,
                ""
        ));
        about_target_part_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_position_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraints_info"),
                -1,
                ""
        ));
        about_position_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_kidnap_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraints"),
                -1,
                ""
        ));
        about_kidnap_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_struggle_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraints"),
                -1,
                ""
        ));
        about_struggle_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_release_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraints"),
                -1,
                ""
        ));
        about_release_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_locks_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraints"),
                -1,
                ""
        ));
        about_locks_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_restraint_interact_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_interact_with_block"), 0, "about_interact_with_block"));
        about_restraint_interact_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_change_position"), 0, "about_change_position"));
        about_restraint_interact_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_restraint_move"), 0, "about_restraint_move"));
        about_restraint_interact_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_main"),
                -1,
                ""
        ));
        about_restraint_interact_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_interact_with_block_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_interact"),
                -1,
                ""
        ));
        about_interact_with_block_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_change_position_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_interact"),
                -1,
                ""
        ));
        about_change_position_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_restraint_move_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_interact"),
                -1,
                ""
        ));
        about_restraint_move_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_action_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_anim_action"), 0, "about_anim_action"));
        about_action_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_carry_action"), 0, "about_carry_action"));
        about_action_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_carrying_action"), 0, "about_carrying_action"));
        about_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_main"),
                -1,
                ""
        ));
        about_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_anim_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_action"),
                -1,
                ""
        ));
        about_anim_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_carry_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_action"),
                -1,
                ""
        ));
        about_carry_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_carrying_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_action"),
                -1,
                ""
        ));
        about_carrying_action_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_restraint_tools_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_add_tools"), 0, "about_add_tools"));
        about_restraint_tools_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_update_tools"), 0, "about_update_tools"));
        about_restraint_tools_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_remove_tools"), 0, "about_remove_tools"));
        about_restraint_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_main"),
                -1,
                ""
        ));
        about_restraint_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_add_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_tools"),
                -1,
                ""
        ));
        about_add_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_update_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_tools"),
                -1,
                ""
        ));
        about_update_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_remove_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_tools"),
                -1,
                ""
        ));
        about_remove_tools_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_restraint_device_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_use_device"), 0, "about_use_device"));
        about_restraint_device_node.addOption(new ConversationOption(Component.translatable("npc." + MODID + ".test_npc.option.about_leave_device"), 0, "about_leave_device"));
        about_restraint_device_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_main"),
                -1,
                ""
        ));
        about_restraint_device_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        about_use_device_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_device"),
                -1,
                ""
        ));
        about_use_device_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));
        about_leave_device_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.return_restraint_device"),
                -1,
                ""
        ));
        about_leave_device_node.addOption(new ConversationOption(
                Component.translatable("npc." + MODID + ".test_npc.option.leave"),
                -2,
                ""
        ));

        this.registerNode(mainNode);
        this.registerNode(player_gagged_mainNode);
        this.registerNode(npc_gagged_mainNode);
        this.registerNode(both_gagged_mainNode);
        this.registerNode(player_gagged_node);
        this.registerNode(ungagged_mainNode);
        this.registerNode(about_update_node);
        this.registerNode(about_me_node);
        this.registerNode(about_restraints_node);
        this.registerNode(about_restraint_interact_node);
        this.registerNode(about_interact_with_block_node);
        this.registerNode(about_change_position_node);
        this.registerNode(about_restraint_move_node);
        this.registerNode(about_restraints_info_node);
        this.registerNode(about_target_part_node);
        this.registerNode(about_position_node);
        this.registerNode(about_kidnap_node);
        this.registerNode(about_struggle_node);
        this.registerNode(about_release_node);
        this.registerNode(about_locks_node);
        this.registerNode(about_action_node);
        this.registerNode(about_anim_action_node);
        this.registerNode(about_carry_action_node);
        this.registerNode(about_carrying_action_node);
        this.registerNode(about_restraint_tools_node);
        this.registerNode(about_add_tools_node);
        this.registerNode(about_update_tools_node);
        this.registerNode(about_remove_tools_node);
        this.registerNode(about_restraint_device_node);
        this.registerNode(about_use_device_node);
        this.registerNode(about_leave_device_node);

        this.registerNode(tradeNode);
    }

    @Override
    public String getFirstNodeName(ServerPlayer player, BaseNPCEntity npc) {
        if(isBeenGag(player) && isBeenGag(npc)){
            return "main_both_gagged";
        }else if(isBeenGag(player)){
            return "main_player_gagged";
        }else if(isBeenGag(npc)){
            return "main_npc_gagged";
        }else{
            return "main";
        }
    }
}