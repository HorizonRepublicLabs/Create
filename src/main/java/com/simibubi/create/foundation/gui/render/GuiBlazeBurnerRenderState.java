package com.simibubi.create.foundation.gui.render;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.level.block.state.BlockState;

/// The stock keeper's blaze used to be drawn straight into the screen's buffer
/// source. Screens hand 3D content to a picture-in-picture renderer now, so the
/// pose the blaze is drawn with travels here instead.
public record GuiBlazeBurnerRenderState(BlockState blockState, HeatLevel heatLevel, float animation,
	float horizontalAngle, boolean canDrawFlame, boolean drawGoggles, @Nullable PartialModel hat, int hashCode,
	int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
}
