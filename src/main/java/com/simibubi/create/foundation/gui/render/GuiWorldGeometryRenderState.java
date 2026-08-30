package com.simibubi.create.foundation.gui.render;

import net.minecraft.client.renderer.SubmitNodeCollector;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

/// Screens no longer hold a buffer source to draw world geometry into. Anything
/// that wants to goes through a picture-in-picture renderer, so what to draw
/// travels here and runs when the render state is submitted.
public record GuiWorldGeometryRenderState(GuiWorldGeometryRenderState.Draw draw, int x0, int y0, int x1, int y1,
	float scale, @Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {

	/// Some of this geometry submits render states of its own and some buffers
	/// vertices, so both ways in are handed over.
	@FunctionalInterface
	public interface Draw {
		void draw(PoseStack poseStack, SubmitNodeCollector collector, SuperRenderTypeBuffer buffer);
	}
}
