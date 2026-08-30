package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/// Hand-drawn items are reached through the item model that names them rather
/// than through a block entity renderer, so this is a plain base class now.
public abstract class CustomRenderedItemModelRenderer {

	private final CustomRenderedItemModel model;

	protected CustomRenderedItemModelRenderer(Identifier baseModel) {
		this.model = new CustomRenderedItemModel(baseModel);
	}

	public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack ms,
		SuperRenderTypeBuffer buffer, int light, int overlay) {
		PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);

		ms.pushPose();
		ms.translate(0.5F, 0.5F, 0.5F);
		render(stack, model, renderer, transformType, ms, buffer, light, overlay);
		ms.popPose();
	}

	protected abstract void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
		ItemDisplayContext transformType, PoseStack ms, SuperRenderTypeBuffer buffer, int light, int overlay);
}
