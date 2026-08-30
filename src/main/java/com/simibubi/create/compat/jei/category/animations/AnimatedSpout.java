package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.foundation.gui.render.GuiWorldGeometryRenderState;

import org.joml.Matrix3x2fStack;

import java.util.List;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.fluids.FluidStack;

public class AnimatedSpout extends AnimatedKinetics {

	private List<FluidStack> fluids;

	public AnimatedSpout withFluids(List<FluidStack> fluids) {
		this.fluids = fluids;
		return this;
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate((float) (xOffset), (float) (yOffset));
		// The isometric tilt needs a 3D rotation on what is now a 2D GUI stack.
		// catnip has not restored that yet, so these draw untilted for now.
		int scale = 20;

		blockElement(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.submit(graphics);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float squeeze = cycle < 20 ? Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		matrixStack.pushMatrix();

		blockElement(AllPartialModels.SPOUT_TOP)
			.scale(scale)
			.submit(graphics);
		matrixStack.translate((float) (0), (float) (-3 * squeeze / 32f));
		blockElement(AllPartialModels.SPOUT_MIDDLE)
			.scale(scale)
			.submit(graphics);
		matrixStack.translate((float) (0), (float) (-3 * squeeze / 32f));
		blockElement(AllPartialModels.SPOUT_BOTTOM)
			.scale(scale)
			.submit(graphics);
		matrixStack.translate((float) (0), (float) (-3 * squeeze / 32f));

		matrixStack.popMatrix();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.submit(graphics);

		// Screens no longer hold a buffer source, so the tank and the stream are
		// drawn picture-in-picture, where a real 3D stack is on hand again.
		FluidStack fluidStack = fluids.get(0);
		float width = 1 / 128f * squeeze;
		graphics.submitPictureInPictureRenderState(new GuiWorldGeometryRenderState((ms, collector, buffer) -> {
			AnimatedKinetics.DEFAULT_LIGHTING.apply();

			ms.pushPose();
			UIRenderHelper.flipForGuiRender(ms);
			ms.scale(16, 16, 16);
			FluidRenderHelper.submitFluidBox(fluidStack.getFluid()
				.defaultFluidState(), 3f / 16f, 3f / 16f, 3f / 16f, 17f / 16f, 17f / 16f, 17f / 16f, collector, ms,
				LightCoordsUtil.FULL_BRIGHT, false, true);
			ms.popPose();

			ms.translate(scale / 2f, scale * 1.5f, 0);
			UIRenderHelper.flipForGuiRender(ms);
			ms.scale(16, 16, 16);
			ms.translate(-0.5f, 0, 0);
			FluidRenderHelper.submitFluidBox(fluidStack.getFluid()
				.defaultFluidState(), -width / 2 + 0.5f, 0, -width / 2 + 0.5f, width / 2 + 0.5f, 2, width / 2 + 0.5f,
				collector, ms, LightCoordsUtil.FULL_BRIGHT, false, true);
		}, xOffset - 2 * scale, yOffset - 3 * scale, xOffset + 2 * scale, yOffset + 2 * scale, scale, null, null));

		matrixStack.popMatrix();
	}

}
