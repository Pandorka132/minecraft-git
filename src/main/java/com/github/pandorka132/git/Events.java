package com.github.pandorka132.git;

import com.github.pandorka132.git.Actions.GitActions;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.nio.file.Path;

import static com.github.pandorka132.git.Git.LOGGER;
@EventBusSubscriber(value = Dist.CLIENT)
public class Events {
    Path savePath;
    @SubscribeEvent
    public static void onLevelLoad(ServerStartedEvent event) {

        //if (!(event.getLevel() instanceof ServerLevel)) { return; }
        //LevelAccessor level = event.getLevel();
        Path savePath = event.getServer()
                .getWorldPath(LevelResource.ROOT);

        if (Git.jgit == null) {
            GitActions.loadGit(savePath);
            LOGGER.info("Git loaded!");
        }
    }
}
