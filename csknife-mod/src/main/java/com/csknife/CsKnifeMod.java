package com.csknife;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsKnifeMod implements ModInitializer {
    public static final String MOD_ID = "csknife";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Client-only mod — no server-side registration needed.
        // All logic happens in CsKnifeClientMod (rendering + keybinds).
        LOGGER.info("CS Karambit Knife loaded (client-only, server-safe) 🔪");
    }
}
