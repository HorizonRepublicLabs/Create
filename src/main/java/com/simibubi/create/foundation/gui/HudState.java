package com.simibubi.create.foundation.gui;

import net.minecraft.client.Minecraft;

/// The F1 hud toggle moved off Options and onto the gui render state.
public class HudState {
	public static boolean isHidden(Minecraft mc) {
		return mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden;
	}
}
