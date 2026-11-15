package com.github.pandorka132.git;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.nio.file.Path;

import static com.github.pandorka132.git.Git.LOGGER;

public class Events {
    Path savePath;
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        Path savePath = level.getServer()
                .getWorldPath(LevelResource.ROOT);
        Path jgitDir = savePath.resolve(".git");
        GitActions.loadGit(savePath);
        LOGGER.info("Git loaded!");
    }
}
