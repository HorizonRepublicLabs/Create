package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

/// Screen's static hasShiftDown and friends went away in 26.x: modifier state
/// rides along on input events now. Code that only wants to know what is held
/// right now asks the window instead.
public class Modifiers {

	public static boolean hasShiftDown() {
		return isDown(InputConstants.KEY_LSHIFT) || isDown(InputConstants.KEY_RSHIFT);
	}

	public static boolean hasControlDown() {
		return isDown(InputConstants.KEY_LCONTROL) || isDown(InputConstants.KEY_RCONTROL);
	}

	public static boolean hasAltDown() {
		return isDown(InputConstants.KEY_LALT) || isDown(InputConstants.KEY_RALT);
	}

	private static boolean isDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow(), key);
	}

	private Modifiers() {}
}
