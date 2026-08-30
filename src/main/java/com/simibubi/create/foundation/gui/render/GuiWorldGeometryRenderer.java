package com.simibubi.create.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

/// Runs the drawing a screen handed over, against a buffer source that submits
/// through the collector for this picture-in-picture pass.
public class GuiWorldGeometryRenderer extends PictureInPictureRenderer<GuiWorldGeometryRenderState> {

	@Override
	public Class<GuiWorldGeometryRenderState> getRenderStateClass() {
		return GuiWorldGeometryRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiWorldGeometryRenderState state, PoseStack poseStack,
		SubmitNodeCollector collector) {
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		buffer.setCollector(collector);
		state.draw()
			.draw(poseStack, collector, buffer);
		buffer.draw();
		buffer.setCollector(null);
	}

	@Override
	protected String getTextureLabel() {
		return "create:gui_world_geometry";
	}
}
