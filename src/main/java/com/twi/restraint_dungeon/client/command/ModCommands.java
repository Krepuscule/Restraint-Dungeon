package com.twi.restraint_dungeon.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import com.twi.restraint_dungeon.item.restraint_item.ModRestraintItems;
import com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("restraint")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("equip")
                    .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("part", RestraintPartArgument.restraintPart())
                            .executes(context -> equipRope(
                                context.getSource(),
                                EntityArgument.getPlayers(context, "targets").stream().findFirst().orElseThrow(),
                                RestraintPartArgument.getPart(context, "part"),
                                -1
                            ))
                            .then(Commands.argument("index", IntegerArgumentType.integer(0, 7))
                                .executes(context -> equipRope(
                                    context.getSource(),
                                    EntityArgument.getPlayers(context, "targets").stream().findFirst().orElseThrow(),
                                    RestraintPartArgument.getPart(context, "part"),
                                    IntegerArgumentType.getInteger(context, "index")
                                ))
                            )
                        )
                    )
                )
        );
    }

    private static int equipRope(CommandSourceStack source, Player target, PlayerRestraintPart part, int index) {
        ItemStack rope = new ItemStack(ModRestraintItems.ROPE.get());
        
        boolean success;
        if (index == -1) {
            success = RestraintUtils.addRestraintItem(target, part, rope);
        } else {
            success = RestraintUtils.addRestraintItemByIndex(target, part, rope, index);
        }

        if (success) {
            source.sendSuccess(() -> Component.literal("已成功为 " + target.getScoreboardName() + " 的 " + part.name() + " 部位装备了绳子"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("装备失败：部位已满或索引无效"));
            return 0;
        }
    }
}