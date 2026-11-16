package com.github.pandorka132.git;

import com.github.pandorka132.git.command.Command;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jgit.api.*;

/*? if fabric {*/
/*import net.fabricmc.api.ModInitializer;
*//*?}*/

/*? if neoforge {*/
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import java.nio.file.Path;
/*?}*/

/*? if neoforge {*/
@Mod(Git.MOD_ID)
/*?}*/
/*? if forge {*/
/*@Mod(Git.MOD_ID)
*//*?}*/
public class Git /*? if fabric {*/ /*implements ModInitializer *//*?}*/ {
    public static final String MOD_ID = "git";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static org.eclipse.jgit.api.Git jgit;

    /*? if neoforge {*/
    public Git(IEventBus modEventBus) {
        LOGGER.info("Hello NeoForge world!");
        NeoForge.EVENT_BUS.register(Command.class);
        NeoForge.EVENT_BUS.register(Events.class);
    }
    /*?}*/


    /*? if fabric {*/
    /*@Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }
    *//*?}*/

    void commonInit(){

    }
}
