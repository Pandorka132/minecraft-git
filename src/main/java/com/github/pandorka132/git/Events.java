package com.github.pandorka132.git;

import com.github.pandorka132.git.Actions.GitActions;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.nio.file.Path;

import static com.github.pandorka132.git.Git.LOGGER;
import static com.github.pandorka132.git.Git.jgit;

@EventBusSubscriber(value = Dist.CLIENT)
public class Events {
    Path savePath;
    @SubscribeEvent
    public static void onLevelLoad(ServerStartedEvent event) {
        Path relativeSavePath = event.getServer()
                .getWorldPath(LevelResource.ROOT);
        Path savePath = relativeSavePath.toAbsolutePath().normalize();

        if (Git.jgit!=null){
            Path gitPath = Git.jgit.getRepository().getDirectory().toPath().toAbsolutePath().normalize().getParent();
            if (gitPath.equals(savePath)){
                LOGGER.info("Git already loaded!");
                return;
            } else {
                LOGGER.info("Closing other world's git");
                jgit.close();
            }
        }

        GitActions.loadGit(relativeSavePath);
        LOGGER.info("Git loaded!");
    }
}
