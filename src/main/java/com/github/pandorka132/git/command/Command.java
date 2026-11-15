package com.github.pandorka132.git.command;

import com.github.pandorka132.git.Git;
import com.github.pandorka132.git.GitActions;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

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

                                            RevCommit commit = GitActions.commit(message, context.getSource().getPlayerOrException());

                                            player.sendSystemMessage(Component.literal("New commit: " + commit.getShortMessage()));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("reset")
                                .then(Commands.argument("commit", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                                    try {
                                                        for (RevCommit commit : Git.jgit.log().call()) {
                                                            String fullHash = commit.getId().getName();
                                                            String shortHash = fullHash.substring(0, 6);

                                                            String name = commit.getShortMessage();
                                                            if (name.length() > 20) {
                                                                name = name.substring(0, 20);
                                                            }

                                                            builder.suggest(
                                                                    shortHash + "_" + commit.getShortMessage()
                                                            );
                                                        }
                                                    } catch (GitAPIException e) {
                                                        throw new RuntimeException(e);
                                                    }

                                                    return builder.buildFuture();
                                                }
                                        ).executes(context -> {
                                            String input = context.getArgument("commit", String.class);
                                            String hashPrefix = input.split("_")[0];

                                            RevCommit found = null;

                                            try {
                                                for (RevCommit commit : Git.jgit.log().call()) {
                                                    if (commit.getId().getName().startsWith(hashPrefix)) {
                                                        if (found != null) {
                                                            throw new IllegalArgumentException("Ambiguous commit hash!");
                                                        }

                                                        found = commit;
                                                    }
                                                }
                                            } catch (GitAPIException e) {
                                                throw new RuntimeException(e);
                                            }

                                            if (found == null) {
                                                throw new IllegalArgumentException("Commit not found!");
                                            }
                                            context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Commit: " + found.getShortMessage()));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )

                        )
        );
    }
}
