package com.simibubi.create.foundation.fluid;

import com.simibubi.create.foundation.render.CreateRenderTypes;

import com.simibubi.create.foundation.fluid.FluidAppearance;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

@OnlyIn(Dist.CLIENT)
public class FluidRenderer {
	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, SuperRenderTypeBuffer buffer, PoseStack ms, int light) {
		renderFluidStream(fluidStack, direction, radius, progress, inbound,
			buffer.getBuffer(CreateRenderTypes.translucentMovingBlock()), ms, light);
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, VertexConsumer builder, PoseStack ms, int light) {
		Fluid fluid = fluidStack.getFluid();
		FluidType fluidAttributes = fluid.getFluidType();
		TextureAtlasSprite flowTexture = FluidAppearance.flowingTexture(fluidStack);
		TextureAtlasSprite stillTexture = FluidAppearance.stillTexture(fluidStack);

		int color = FluidAppearance.tintColor(fluidStack);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLightLevel(fluidStack));
		light = (light & 0xF00000) | luminosity << 4;

		if (inbound)
			direction = direction.getOpposite();

		var msr = TransformStack.of(ms);
		ms.pushPose();
		msr.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(direction))
			.rotateXDegrees(direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)
			.uncenter();
		ms.translate(.5, 0, .5);

		float h = radius;
		float hMin = -radius;
		float hMax = radius;
		float y = inbound ? 1 : .5f;
		float yMin = y - Mth.clamp(progress * .5f, 0, 1);
		float yMax = y;

		for (int i = 0; i < 4; i++) {
			ms.pushPose();
			renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, hMax, yMax, h, builder, ms, light, color, flowTexture);
			ms.popPose();
			msr.rotateYDegrees(90);
		}

		if (progress != 1)
			FluidRenderHelper.renderStillTiledFace(Direction.DOWN, hMin, hMin, hMax, hMax, yMin, builder, ms.last(),
				light, color, stillTexture);

		ms.popPose();
	}

	public static void renderFlowingTiledFace(Direction dir, float left, float down, float right, float up,
		float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		FluidRenderHelper.renderTiledFace(dir, left, down, right, up, depth, builder, ms.last(), light, color, texture,
			0.5f);
	}

	/// catnip draws a fluid box into a vertex consumer now, so the render type
	/// is picked here rather than inside the helper.
	public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, SuperRenderTypeBuffer buffer, PoseStack ms, int light, boolean renderBottom,
		boolean invertGasses) {
		FluidRenderHelper.renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax,
			buffer.getBuffer(CreateRenderTypes.translucentMovingBlock()), ms, light, renderBottom, invertGasses);
	}

}
