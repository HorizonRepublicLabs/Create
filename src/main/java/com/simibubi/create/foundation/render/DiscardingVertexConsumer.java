package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.resources.model.geometry.BakedQuad;

/// Blocks are buffered for every layer in one pass, so the layers a structure
/// buffer is not being built for need somewhere to go.
public class DiscardingVertexConsumer implements VertexConsumer {

	public static final DiscardingVertexConsumer INSTANCE = new DiscardingVertexConsumer();

	@Override
	public void putBakedQuad(Pose pose, BakedQuad quad, QuadInstance instance) {
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		return this;
	}

	@Override
	public VertexConsumer setColor(int color) {
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		return this;
	}
}
