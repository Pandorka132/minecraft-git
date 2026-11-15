package com.github.pandorka132.git;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.pandorka132.git.Git.jgit;


public class GitActions {

    public static InitCommand init(){
        return null;
    }

    public static void loadGit(Path worldDir){
        Path gitDir = worldDir.resolve(".git");
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
