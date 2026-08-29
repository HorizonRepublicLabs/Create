package com.simibubi.create.foundation.model;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.BlockPos;

import net.minecraft.client.renderer.block.BlockAndTintGetter;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import com.simibubi.create.foundation.model.DelegateModelPart;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import static net.createmod.catnip.api.client.render.SpriteShiftEntry.getUnInterpolatedU;
import static net.createmod.catnip.api.client.render.SpriteShiftEntry.getUnInterpolatedV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;

public class BakedModelHelper {

	/// 26.x quads are immutable records rather than an int[] of vertex data, so
	/// this takes and returns a quad and collects its edits through the editor.
	public static BakedQuad cropAndMove(BakedQuad quad, TextureAtlasSprite sprite, AABB crop, Vec3 move) {
		BakedQuadHelper.Editor vertexData = BakedQuadHelper.edit(quad);

		Vec3 xyz0 = vertexData.getXYZ(0);
		Vec3 xyz1 = vertexData.getXYZ(1);
		Vec3 xyz2 = vertexData.getXYZ(2);
		Vec3 xyz3 = vertexData.getXYZ(3);

		Vec3 uAxis = xyz3.add(xyz2)
			.scale(.5);
		Vec3 vAxis = xyz1.add(xyz2)
			.scale(.5);
		Vec3 center = xyz3.add(xyz2)
			.add(xyz0)
			.add(xyz1)
			.scale(.25);

		float u0 = vertexData.getU(0);
		float u3 = vertexData.getU(3);
		float v0 = vertexData.getV(0);
		float v1 = vertexData.getV(1);

		float uScale = (float) Math
			.round((getUnInterpolatedU(sprite, u3) - getUnInterpolatedU(sprite, u0)) / xyz3.distanceTo(xyz0));
		float vScale = (float) Math
			.round((getUnInterpolatedV(sprite, v1) - getUnInterpolatedV(sprite, v0)) / xyz1.distanceTo(xyz0));

		if (uScale == 0) {
			float v3 = vertexData.getV(3);
			float u1 = vertexData.getU(1);
			uAxis = xyz1.add(xyz2)
				.scale(.5);
			vAxis = xyz3.add(xyz2)
				.scale(.5);
			uScale = (float) Math
				.round((getUnInterpolatedU(sprite, u1) - getUnInterpolatedU(sprite, u0)) / xyz1.distanceTo(xyz0));
			vScale = (float) Math
				.round((getUnInterpolatedV(sprite, v3) - getUnInterpolatedV(sprite, v0)) / xyz3.distanceTo(xyz0));

		}

		uAxis = uAxis.subtract(center)
			.normalize();
		vAxis = vAxis.subtract(center)
			.normalize();

		Vec3 min = new Vec3(crop.minX, crop.minY, crop.minZ);
		Vec3 max = new Vec3(crop.maxX, crop.maxY, crop.maxZ);

		for (int vertex = 0; vertex < 4; vertex++) {
			Vec3 xyz = vertexData.getXYZ(vertex);
			Vec3 newXyz = VecHelper.componentMin(max, VecHelper.componentMax(xyz, min));
			Vec3 diff = newXyz.subtract(xyz);

			if (diff.lengthSqr() > 0) {
				float u = vertexData.getU(vertex);
				float v = vertexData.getV(vertex);
				float uDiff = (float) uAxis.dot(diff) * uScale;
				float vDiff = (float) vAxis.dot(diff) * vScale;
				vertexData.setU(vertex, sprite.getU(getUnInterpolatedU(sprite, u) + uDiff));
				vertexData.setV(vertex, sprite.getV(getUnInterpolatedV(sprite, v) + vDiff));
			}

			vertexData.setXYZ(vertex, newXyz.add(move));
		}

		return vertexData.build();
	}

	/// Rebuilt around parts: a model hands back BlockStateModelParts now rather
	/// than quad lists per cull face, so swapping sprites means wrapping each
	/// part and swapping the quads it reports.
	public static BlockStateModel generateModel(BlockStateModel template,
		UnaryOperator<TextureAtlasSprite> spriteSwapper) {
		return new DelegateBlockStateModel(template) {
			@Override
			public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
				List<BlockStateModelPart> parts) {
				int first = parts.size();
				super.collectParts(level, pos, state, random, parts);
				for (int i = first; i < parts.size(); i++)
					parts.set(i, new SpriteSwappingPart(parts.get(i), spriteSwapper));
			}
		};
	}

	private static class SpriteSwappingPart extends DelegateModelPart {

		private final UnaryOperator<TextureAtlasSprite> spriteSwapper;

		SpriteSwappingPart(BlockStateModelPart wrapped, UnaryOperator<TextureAtlasSprite> spriteSwapper) {
			super(wrapped);
			this.spriteSwapper = spriteSwapper;
		}

		@Override
		public List<BakedQuad> getQuads(Direction direction) {
			return swapSprites(wrapped.getQuads(direction), spriteSwapper);
		}
	}

	public static List<BakedQuad> swapSprites(List<BakedQuad> quads, UnaryOperator<TextureAtlasSprite> spriteSwapper) {
		List<BakedQuad> newQuads = new ArrayList<>(quads);
		int size = quads.size();
		for (int i = 0; i < size; i++) {
			BakedQuad quad = quads.get(i);
			TextureAtlasSprite sprite = quad.materialInfo().sprite();
			TextureAtlasSprite newSprite = spriteSwapper.apply(sprite);
			if (newSprite == null || sprite == newSprite)
				continue;

			BakedQuadHelper.Editor vertexData = BakedQuadHelper.edit(quad);

			for (int vertex = 0; vertex < 4; vertex++) {
				float u = vertexData.getU(vertex);
				float v = vertexData.getV(vertex);
				vertexData.setU(vertex, newSprite.getU(getUnInterpolatedU(sprite, u)));
				vertexData.setV(vertex, newSprite.getV(getUnInterpolatedV(sprite, v)));
			}

			newQuads.set(i, vertexData.build());
		}
		return newQuads;
	}
}
