package com.simibubi.create.foundation.item.render;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/// Bridges Create's hand-drawn item renderers into the item pipeline: they draw
/// through a buffer source, which submits through the collector it is given.
public class CustomRenderedSpecialRenderer implements SpecialModelRenderer<ItemStack> {

	private final CustomRenderedItemModelRenderer renderer;

	public CustomRenderedSpecialRenderer(CustomRenderedItemModelRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
		int overlayCoords, boolean hasFoil, int outlineColor) {
		if (stack == null)
			return;

		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		buffer.setCollector(collector);
		renderer.renderByItem(stack, ItemDisplayContext.NONE, poseStack, buffer, lightCoords, overlayCoords);
		buffer.draw();
		buffer.setCollector(null);
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		output.accept(new Vector3f(0, 0, 0));
		output.accept(new Vector3f(1, 1, 1));
	}

	@Override
	public ItemStack extractArgument(ItemStack stack) {
		return stack.copy();
	}
}
