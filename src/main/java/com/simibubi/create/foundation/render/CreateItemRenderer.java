package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/// ItemRenderer is gone in 26.x: an item is resolved into an
/// ItemStackRenderState during the extract pass and submitted during the
/// submit pass. Create draws items inline from block entity and GUI
/// renderers, so this does both halves back to back against the collector
/// catnip's buffer is holding.
public class CreateItemRenderer {

	public static void render(ItemStack stack, ItemDisplayContext context, PoseStack ms,
		SuperRenderTypeBuffer buffer, int light, int overlay, int seed) {
		if (stack.isEmpty())
			return;
		SubmitNodeCollector collector = buffer.getCollector();
		if (collector == null)
			return;

		Minecraft minecraft = Minecraft.getInstance();
		Level level = minecraft.level;
		ItemStackRenderState state = new ItemStackRenderState();
		minecraft.getItemModelResolver()
			.updateForTopItem(state, stack, context, level, null, seed);
		state.submit(ms, collector, light, overlay, 0);
	}

	public static void render(ItemStack stack, ItemDisplayContext context, PoseStack ms,
		SuperRenderTypeBuffer buffer, int light, int overlay) {
		render(stack, context, ms, buffer, light, overlay, 0);
	}

	private CreateItemRenderer() {}
}
