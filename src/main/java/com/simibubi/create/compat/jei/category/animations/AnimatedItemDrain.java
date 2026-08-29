package com.simibubi.create.compat.jei.category.animations;

import org.joml.Matrix3x2fStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.LightCoordsUtil;

import net.neoforged.neoforge.fluids.FluidStack;

public class AnimatedItemDrain extends AnimatedKinetics {

	private FluidStack fluid;

	public AnimatedItemDrain withFluid(FluidStack fluid) {
		this.fluid = fluid;
		return this;
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		// The isometric tilt needs a 3D rotation on what is now a 2D GUI stack.
		// catnip has not restored that yet, so these draw untilted for now.
		int scale = 20;

		blockElement(AllBlocks.ITEM_DRAIN.getDefaultState())
			.scale(scale)
			.submit(graphics);

		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(scale, scale);
		float from = 2 / 16f;
		float to = 1f - from;
		FluidRenderHelper.renderFluidBox(fluid, from, from, from, to, 3 / 4f, to, graphics.bufferSource(), matrixStack, LightCoordsUtil.FULL_BRIGHT, false, true);
		graphics.flush();

		matrixStack.popMatrix();
	}
}
