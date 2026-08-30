package com.simibubi.create.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.processing.burner.BlazeBurnerRenderer;
import com.simibubi.create.foundation.render.CreateCachedBuffers;
import com.simibubi.create.foundation.render.CreateRenderTypes;

import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/// Draws the cage and the blaze itself into the screen's picture-in-picture
/// texture, the same pair the stock keeper screen always drew by hand.
public class GuiBlazeBurnerRenderer extends PictureInPictureRenderer<GuiBlazeBurnerRenderState> {

	@Override
	public Class<GuiBlazeBurnerRenderState> getRenderStateClass() {
		return GuiBlazeBurnerRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiBlazeBurnerRenderState state, PoseStack poseStack,
		SubmitNodeCollector collector) {
		Level level = Minecraft.getInstance().level;
		if (level == null)
			return;

		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		buffer.setCollector(collector);

		CreateCachedBuffers.partial(AllPartialModels.BLAZE_CAGE, state.blockState())
			.rotateCentered(state.horizontalAngle() + Mth.PI, Direction.UP)
			.light(LightCoordsUtil.FULL_BRIGHT)
			.renderInto(poseStack, buffer.getBuffer(CreateRenderTypes.cutoutMovingBlock()));

		BlazeBurnerRenderer.renderShared(poseStack, null, buffer, level, state.blockState(), state.heatLevel(),
			state.animation(), state.horizontalAngle(), state.canDrawFlame(), state.drawGoggles(), state.hat(),
			state.hashCode());

		buffer.draw();
		buffer.setCollector(null);
	}

	@Override
	protected String getTextureLabel() {
		return "create:gui_blaze_burner";
	}
}
