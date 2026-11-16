package com.github.pandorka132.git.Actions;

import com.github.pandorka132.git.Git;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(
        modid = Git.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.GAME
)
public final class ClientReset {
    private static volatile Pending pending;

    private record Pending(
            MinecraftServer server,
            String worldName,
            Runnable resetOperation,
            RestoreScreen screen
    ) {}

    private ClientReset() {}

    public static synchronized void begin(
            MinecraftServer server,
            String worldName,
            Runnable resetOperation
    ) {
        if (pending != null) {
            throw new IllegalStateException("A world reset is already running.");
        }

        RestoreScreen screen = new RestoreScreen();
        Pending operation = new Pending(server, worldName, resetOperation, screen);
        pending = operation;

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            minecraft.setScreen(screen);
            server.execute(() -> server.halt(false));
        });
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Pending operation = pending;
        if (operation != null && event.getNewScreen() != operation.screen()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        Pending operation = pending;
        if (operation == null || operation.server() != event.getServer()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            minecraft.setScreen(operation.screen());
            operation.screen().setMessage("Restoring world...");

            CompletableFuture.runAsync(() -> {
                try {
                    operation.resetOperation().run();

                    minecraft.execute(() -> {
                        if (pending != operation) {
                            return;
                        }

                        pending = null;
                        minecraft.createWorldOpenFlows().openWorld(
                                operation.worldName(),
                                () -> {}
                        );
                    });
                } catch (Throwable error) {
                    Git.LOGGER.error("World restore failed", error);

                    minecraft.execute(() -> {
                        if (pending != operation) {
                            return;
                        }

                        operation.screen().setFailure(error);
                        minecraft.setScreen(operation.screen());
                        pending = null;
                    });
                }
            });
        });
    }
}
