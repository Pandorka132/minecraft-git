package com.github.pandorka132.git.Actions;

import com.github.pandorka132.git.Git;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static com.github.pandorka132.git.Git.LOGGER;
import static com.github.pandorka132.git.Git.jgit;


public class GitActions {
    public static RevCommit commit(String message, MinecraftServer server) {
        server.saveEverything(false, true, false);

        long start = System.nanoTime();
        addFiles();
        long elapsed = System.nanoTime() - start;
        LOGGER.info("Addfiles took {} ms", elapsed / 1_000_000.0);

        start = System.nanoTime();

        RevCommit commit;
        try {
            jgit.add()
                    .addFilepattern(".")
                    .setUpdate(true)
                    .call();

            commit = Git.jgit.commit().setMessage(message).call();
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to commit! ", e);
        }
        elapsed = System.nanoTime() - start;
        LOGGER.info("Commit took {} ms", elapsed / 1_000_000.0);

        return commit;
    }

    public static void reset(CommandSourceStack source, RevCommit commit) {
        MinecraftServer server = source.getServer();

        if (!server.isSingleplayer()) {
            throw new IllegalStateException(
                    "World reset is only supported in singleplayer."
            );
        }

        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new IllegalStateException(
                    "World reset requires a client."
            );
        }

        String worldName = server.getWorldPath(LevelResource.ROOT)
                .getParent()
                .getFileName()
                .toString();
        String commitId = commit.getId().getName();

        ClientReset.begin(server, worldName, () -> {
            try {
                jgit.reset()
                        .setMode(ResetCommand.ResetType.HARD)
                        .setRef(commitId)
                        .call();
            } catch (GitAPIException e) {
                throw new RuntimeException("Failed to reset!", e);
            }
        });
    }

    public static void addFiles() {
        try {
            Set<String> untrackedFiles = getUntrackedFiles();
            if (untrackedFiles.isEmpty()) {return;}
            jgit.add().addFilepatterns(untrackedFiles).call();
            //jgit.add().addFilepattern(".").call();
        } catch (GitAPIException e) {throw new RuntimeException(e);}
    }

    public static void loadGit(Path worldDir){
        Path gitDir = worldDir.resolve(".git");
        if(!(jgit == null)) {
            return;
        }
        if (Files.isDirectory(gitDir)) {
            try {
                jgit = org.eclipse.jgit.api.Git.open(worldDir.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                jgit = org.eclipse.jgit.api.Git.init()
                        .setDirectory(worldDir.toFile())
                        .call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Set<String> getUntrackedFiles() { //jgit's addFilepattern(".") is stupidly slow, so we need our own
        Set<String> untrackedFiles = new HashSet<>();

        try (TreeWalk walk = new TreeWalk(jgit.getRepository())) {
            walk.addTree(new DirCacheIterator(jgit.getRepository().readDirCache()));
            walk.addTree(new FileTreeIterator(jgit.getRepository()));
            walk.setRecursive(true);

            while (walk.next()) {
                DirCacheIterator index = walk.getTree(0, DirCacheIterator.class);
                WorkingTreeIterator workingTree = walk.getTree(1, WorkingTreeIterator.class);

                if (index == null && workingTree != null && !workingTree.isEntryIgnored()) {
                    untrackedFiles.add(walk.getPathString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return untrackedFiles;
    }
}
