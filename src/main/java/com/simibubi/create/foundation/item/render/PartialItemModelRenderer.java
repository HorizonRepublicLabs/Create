package com.simibubi.create.foundation.item.render;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.CreateRenderTypes;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/// Models hand out their parts and each part its quads, rather than the item
/// renderer walking a model for a render type, so the partial pieces of
/// Create's hand-drawn items are put together here.
public class PartialItemModelRenderer {

	private static final PartialItemModelRenderer INSTANCE = new PartialItemModelRenderer();

	private final RandomSource random = RandomSource.create();
	private final QuadInstance quadInstance = new QuadInstance();
	private final List<BlockStateModelPart> parts = new ArrayList<>();

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
		render(model, Sheets.translucentBlockItemSheet(), light);
	}

	/// There is no solid item sheet any more; cutout is what the item pipeline
	/// draws opaque item geometry with.
	public void renderSolid(BlockStateModel model, int light) {
		render(model, Sheets.cutoutBlockItemSheet(), light);
	}

	public void renderGlowing(BlockStateModel model, int light) {
		render(model, CreateRenderTypes.itemGlowingTranslucent(), light);
	}

	public void renderSolidGlowing(BlockStateModel model, int light) {
		render(model, CreateRenderTypes.itemGlowingSolid(), light);
	}

	public void render(BlockStateModel model, RenderType type, int light) {
		if (stack.isEmpty())
			return;

		ms.pushPose();
		ms.translate(-0.5D, -0.5D, -0.5D);

		VertexConsumer vc = buffer.getBuffer(type);
		quadInstance.setLightCoords(light);
		quadInstance.setOverlayCoords(overlay);
		quadInstance.setColor(-1);

		random.setSeed(42L);
		parts.clear();
		model.collectParts(random, parts);

		PoseStack.Pose pose = ms.last();
		for (BlockStateModelPart part : parts) {
			for (Direction direction : Iterate.directions)
				for (BakedQuad quad : part.getQuads(direction))
					vc.putBakedQuad(pose, quad, quadInstance);
			for (BakedQuad quad : part.getQuads(null))
				vc.putBakedQuad(pose, quad, quadInstance);
		}

		parts.clear();
		ms.popPose();
	}

	public ItemDisplayContext getTransformType() {
		return transformType;
	}
}
