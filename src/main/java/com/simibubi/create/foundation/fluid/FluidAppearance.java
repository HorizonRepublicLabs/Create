package com.simibubi.create.foundation.fluid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.fluids.FluidStack;

/// Fluid appearance moved off IClientFluidTypeExtensions and onto data-driven
/// fluid models in 26.x: the still and flowing textures are baked materials and
/// the colour comes from a tint source rather than getTintColor.
public class FluidAppearance {
	public static FluidModel modelOf(FluidStack stack) {
		return Minecraft.getInstance()
			.getModelManager()
			.getFluidStateModelSet()
			.get(stack.getFluid()
				.defaultFluidState());
	}

	public static TextureAtlasSprite stillTexture(FluidStack stack) {
		return modelOf(stack).stillMaterial()
			.sprite();
	}

	public static TextureAtlasSprite flowingTexture(FluidStack stack) {
		return modelOf(stack).flowingMaterial()
			.sprite();
	}

	/// Opaque white when the fluid declares no tint, matching the old default.
	public static int tintColor(FluidStack stack) {
		FluidModel model = modelOf(stack);
		return model.fluidTintSource() == null ? 0xFFFFFFFF
			: model.fluidTintSource()
				.colorAsStack(stack);
	}
}
