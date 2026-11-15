package com.github.pandorka132.git.command;

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

                                            RevCommit commit = GitActions.commit(message);

                                            player.sendSystemMessage(Component.literal("New commit: " + commit.getShortMessage()));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }
}
