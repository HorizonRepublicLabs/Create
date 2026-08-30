package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.client.gui.components.SubtitleOverlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Hud;


/// The subtitle overlay and tool highlight moved from the gui to the hud.
@Mixin(Hud.class)
public interface GuiAccessor {
	@Accessor("subtitleOverlay")
	SubtitleOverlay create$getSubtitleOverlay();

	@Accessor("toolHighlightTimer")
	int create$getToolHighlightTimer();
}
