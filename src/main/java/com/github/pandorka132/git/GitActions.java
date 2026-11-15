package com.github.pandorka132.git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.pandorka132.git.Git.jgit;


public class GitActions {

    public static RevCommit commit(String message) {
        addFiles();
        RevCommit commit;
        try {
            commit = Git.jgit.commit().setMessage(message).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        return commit;
    }

    public static void addFiles() {
        try {
            jgit.add().addFilepattern(".").call();
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
}
