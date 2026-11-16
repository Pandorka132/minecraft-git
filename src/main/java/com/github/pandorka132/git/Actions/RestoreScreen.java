package com.github.pandorka132.git.Actions;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class RestoreScreen extends GenericMessageScreen {
    private volatile Component message = Component.literal("Saving world...");

    public RestoreScreen() {
        super(Component.literal("Saving world..."));
    }

    public void setMessage(String message) {
        this.message = Component.literal(message);
    }

    public void setFailure(Throwable error) {
        Throwable cause = error;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        this.message = Component.literal(
                "Restore failed: " + cause.getMessage()
        );
    }

    @Override
    public void render(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(
                font,
                message,
                width / 2,
                height / 2,
                0xFFFFFF
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
