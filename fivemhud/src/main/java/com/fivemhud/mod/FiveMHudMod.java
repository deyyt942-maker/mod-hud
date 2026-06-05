package com.fivemhud.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FiveMHudMod.MOD_ID)
public class FiveMHudMod {
    public static final String MOD_ID = "fivemhud";
    public static final Logger LOGGER = LogManager.getLogger();

    public FiveMHudMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new HudRenderer());
        LOGGER.info("[FiveMHUD] HUD înregistrat cu succes!");
    }
}
