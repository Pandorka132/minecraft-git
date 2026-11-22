package com.github.pandorka132.git.command;

import com.github.pandorka132.git.Actions.GitActions;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.eclipse.jgit.revwalk.RevCommit;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class Command {
    private static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

    @SubscribeEvent // on the game event bus
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("git")
                        .then(Commands.literal("commit")
                                .then(Commands.argument("message", StringArgumentType.string())
                                        .executes(context -> {
                                            String message = context.getArgument("message", String.class);
                                            ServerPlayer player = context.getSource().getPlayerOrException();

                                            RevCommit commit = GitActions.commit(message, context.getSource().getServer());

                                            player.sendSystemMessage(Component.literal("New commit: " + commit.getShortMessage()));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("reset")
                                .then(Commands.argument("commit", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                                    String remaining = builder.getRemainingLowerCase();
                                                    StringRange range = StringRange.between(
                                                            builder.getStart(),
                                                            builder.getInput().length()
                                                    );
                                                    List<Suggestion> suggestions = new ArrayList<>();

                                                    for (RevCommit commit : GitActions.getCommits()) {
                                                        String shortHash = commit.getId().getName().substring(0, 6);
                                                        String suggestion = shortHash + ":" + commit.getShortMessage();

                                                        if (suggestion.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                                            suggestions.add(new Suggestion(range, suggestion));
                                                        }
                                                    }

                                                    return CompletableFuture.completedFuture(
                                                            new Suggestions(range, suggestions)
                                                    );
                                                }
                                        ).executes(context -> {
                                            String input = context.getArgument("commit", String.class);
                                            int separator = input.indexOf(':');
                                            if (separator != 6) {
                                                throw new IllegalArgumentException("Invalid commit format! Expected shortHash:message.");
                                            }

                                            String hashPrefix = input.substring(0, separator);
                                            String message = input.substring(separator + 1);
                                            RevCommit found = null;

                                            for (RevCommit commit : GitActions.getCommits()) {
                                                if (commit.getId().getName().startsWith(hashPrefix)
                                                        && commit.getShortMessage().equals(message)) {
                                                    if (found != null) {
                                                        throw new IllegalArgumentException("Ambiguous commit hash!");
                                                    }

                                                    found = commit;
                                                }
                                            }

                                            if (found == null) {
                                                throw new IllegalArgumentException("Commit not found!");
                                            }
                                            //context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Commit: " + found.getShortMessage()));

                                            context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Starting reset"));
                                            GitActions.reset(context.getSource(), found);

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )

                        )
        );
    }
}
