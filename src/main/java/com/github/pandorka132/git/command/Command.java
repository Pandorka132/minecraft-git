package com.github.pandorka132.git.command;

import com.github.pandorka132.git.GitActions;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class Command {
    private static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

    @SubscribeEvent // on the game event bus
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("git")
                        .then(Commands.literal("commit")
                                .then(Commands.argument("message", StringArgumentType.string())
                                        .executes(context -> {
                                            GitActions.commit(String.valueOf(context));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }
}
