import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

modSettings {
    clientOptions {
        fov = 100
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }

    variableReplacements =
        mapOf(
            "minecraftVersionVirtual" to stonecutter.current.version,
            "forgeLoaderVersion" to
                if (project.mod.isForge) {
                    project.mod
                        .prop("forge_version")
                        .substringAfter("-")
                        .substringBefore(".")
                } else {
                    ""
                },
        )
}

publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
        environment.set(CLIENT_OR_SERVER_PREFERS_BOTH)
    }

    curseforge {
        client = true
        server = true
        if (mod.isFabric) requires("fabric-api")
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.8.1.202611041320-r")
    forgeRuntimeLibrary("org.eclipse.jgit:org.eclipse.jgit:7.8.1.202611041320-r")
    include("org.eclipse.jgit:org.eclipse.jgit:7.8.1.202611041320-r")
}
