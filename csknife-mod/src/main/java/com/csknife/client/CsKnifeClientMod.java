package com.csknife.client;

import com.csknife.CsKnifeMod;
import com.csknife.client.animation.KarambithAnimationController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import org.lwjgl.glfw.GLFW;

public class CsKnifeClientMod implements ClientModInitializer {

    public static KeyBinding INSPECT_KEY;
    public static KeyBinding TRICK_KEY;

    public static final KarambithAnimationController ANIMATION = new KarambithAnimationController();

    /** Returns true for any vanilla or modded sword (SwordItem subclass). */
    public static boolean isSword(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public void onInitializeClient() {
        INSPECT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.csknife.inspect",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.csknife"
        ));

        TRICK_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.csknife.trick",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.csknife"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean holdingSword = isSword(client.player.getMainHandStack())
                || isSword(client.player.getOffHandStack());

            if (!holdingSword) {
                ANIMATION.reset();
                return;
            }

            while (INSPECT_KEY.wasPressed()) ANIMATION.startInspect();
            while (TRICK_KEY.wasPressed())   ANIMATION.startTrick();

            ANIMATION.tick();
        });

        CsKnifeMod.LOGGER.info("CS Karambit client ready — all swords become Karambits! V=inspect, B=trick");
    }
}

