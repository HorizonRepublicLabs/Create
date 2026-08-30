package com.simibubi.create.foundation.gui.render;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.api.client.render.CatnipRenderPipelines;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

/// The radial menu's pointer used to go straight to a Tesselator. Screens now
/// hand geometry to the gui render state instead, so the fan lives here.
public record DirectionIndicatorRenderState(Matrix3x2f pose, float r, float g, float b)
	implements GuiElementRenderState {

	@Override
	public RenderPipeline pipeline() {
		return CatnipRenderPipelines.TRIANGLE_FAN;
	}

	@Override
	public void buildVertices(VertexConsumer consumer) {
		consumer.addVertexWith2DPose(pose, 0, 0)
			.setColor(r, g, b, 0.75f);

		consumer.addVertexWith2DPose(pose, 5, -5)
			.setColor(r, g, b, 0.4f);
		consumer.addVertexWith2DPose(pose, 3, -4.5f)
			.setColor(r, g, b, 0.4f);
		consumer.addVertexWith2DPose(pose, 0, -4.2f)
			.setColor(r, g, b, 0.4f);
		consumer.addVertexWith2DPose(pose, -3, -4.5f)
			.setColor(r, g, b, 0.4f);
		consumer.addVertexWith2DPose(pose, -5, -5)
			.setColor(r, g, b, 0.4f);
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.noTexture();
	}

	@Override
	public @Nullable ScreenRectangle scissorArea() {
		return null;
	}

	@Override
	public ScreenRectangle bounds() {
		return new ScreenRectangle(-6, -6, 12, 12).transformMaxBounds(pose);
	}
}
