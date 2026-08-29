package com.simibubi.create.foundation.item.render;

import com.simibubi.create.foundation.render.CreateItemRenderer;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.RenderTypes;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.model.data.ModelData;

public class PartialItemModelRenderer {

	private static final PartialItemModelRenderer INSTANCE = new PartialItemModelRenderer();

	private final RandomSource random = RandomSource.create();

	private ItemStack stack;
	private ItemDisplayContext transformType;
	private PoseStack ms;
	private SuperRenderTypeBuffer buffer;
	private int overlay;

	public static PartialItemModelRenderer of(ItemStack stack, ItemDisplayContext transformType,
		PoseStack ms, SuperRenderTypeBuffer buffer, int overlay) {
		PartialItemModelRenderer instance = INSTANCE;
		instance.stack = stack;
		instance.transformType = transformType;
		instance.ms = ms;
		instance.buffer = buffer;
		instance.overlay = overlay;
		return instance;
	}

	public void render(BlockStateModel model, int light) {
		render(model, Sheets.translucentCullBlockSheet(), light);
	}

	public void renderSolid(BlockStateModel model, int light) {
		render(model, Sheets.solidBlockSheet(), light);
	}

	public void renderGlowing(BlockStateModel model, int light) {
		render(model, RenderTypes.itemGlowingTranslucent(), light);
	}

	public void renderSolidGlowing(BlockStateModel model, int light) {
		render(model, RenderTypes.itemGlowingSolid(), light);
	}

	public void render(BlockStateModel model, RenderType type, int light) {
		if (stack.isEmpty())
			return;

		ms.pushPose();
		ms.translate(-0.5D, -0.5D, -0.5D);

		if (!model.isCustomRenderer()) {
			VertexConsumer vc = ItemRenderer.getFoilBufferDirect(buffer, type, true, stack.hasFoil());
			for (BlockStateModel pass : model.getRenderPasses(stack, false)) {
				renderBakedItemModel(pass, light, ms, vc);
			}
		} else
			IClientItemExtensions.of(stack)
				.getCustomRenderer()
				.renderByItem(stack, transformType, ms, buffer, light, overlay);

		ms.popPose();
	}

	private void renderBakedItemModel(BlockStateModel model, int light, PoseStack ms, VertexConsumer buffer) {
		ModelData data = ModelData.EMPTY;

		for (RenderType renderType : model.getRenderTypes(stack, false)) {
			for (Direction direction : Iterate.directions) {
				random.setSeed(42L);
				ir.renderQuadList(ms, buffer, model.getQuads(null, direction, random, data, renderType), stack, light,
					overlay);
			}

			random.setSeed(42L);
			ir.renderQuadList(ms, buffer, model.getQuads(null, null, random, data, renderType), stack, light, overlay);
		}
	}

}
