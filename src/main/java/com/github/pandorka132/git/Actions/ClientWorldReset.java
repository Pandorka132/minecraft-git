package com.github.pandorka132.git.Actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.concurrent.CompletableFuture;

public final class ClientWorldReset {
    private static PendingReset pending;

    private ClientWorldReset() {}

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PendingReset reset = pending;

        if (reset == null || reset.server() != event.getServer()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        reset.screen().setMessage("Restoring world...");

        // A világ már leállt, ezért most biztonságosan módosíthatók a fájljai.
        CompletableFuture.runAsync(reset.resetOperation())
                .whenComplete((unused, error) -> minecraft.execute(() -> {
                    if (pending != reset) {
                        return;
                    }

                    if (error != null) {
                        reset.screen().setFailure(error);
                        pending = null;
                        return;
                    }

                    pending = null;

                    // A vanilla betölti ugyanazt a világot, és új integrált
                    // szervert indít.
                    minecraft.createWorldOpenFlows().openWorld(
                            reset.worldName(),
                            () -> minecraft.setScreen(new TitleScreen())
                    );
                }));
    }

    private record PendingReset(
            MinecraftServer server,
            String worldName,
            Runnable resetOperation,
            RestoreScreen screen
    ) {}
}
