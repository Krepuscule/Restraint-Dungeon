package com.twi.restraint_dungeon.client.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.twi.restraint_dungeon.attachment.capability.common_capability.RestraintCapability.PlayerRestraintPart;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RestraintPartArgument implements ArgumentType<PlayerRestraintPart> {
    private static final Collection<String> EXAMPLES = Arrays.stream(PlayerRestraintPart.values()).map(Enum::name).toList();
    private static final DynamicCommandExceptionType INVALID_PART = new DynamicCommandExceptionType(
            name -> Component.literal("无效的部位名称: " + name)
    );

    public static RestraintPartArgument restraintPart() {
        return new RestraintPartArgument();
    }

    public static PlayerRestraintPart getPart(CommandContext<?> context, String name) {
        return context.getArgument(name, PlayerRestraintPart.class);
    }

    @Override
    public PlayerRestraintPart parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString();
        try {
            return PlayerRestraintPart.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw INVALID_PART.create(s);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (PlayerRestraintPart part : PlayerRestraintPart.values()) {
            builder.suggest(part.name());
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}