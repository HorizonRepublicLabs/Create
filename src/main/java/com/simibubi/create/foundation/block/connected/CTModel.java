package com.simibubi.create.foundation.block.connected;

import net.minecraft.world.level.BlockGetter;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import com.simibubi.create.foundation.model.DelegateModelPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;
import net.neoforged.neoforge.model.data.ModelProperty;

public class CTModel extends DelegateBlockStateModel {

	private final ConnectedTextureBehaviour behaviour;

	public CTModel(BlockStateModel originalModel, ConnectedTextureBehaviour behaviour) {
		super(originalModel);
		this.behaviour = behaviour;
	}

	protected CTData createCTData(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		CTData data = new CTData();
		MutableBlockPos mutablePos = new MutableBlockPos();
		for (Direction face : Iterate.directions) {
			BlockState actualState = world.getBlockState(pos);
			if (!behaviour.buildContextForOccludedDirections()
				&& !Block.shouldRenderFace(world, pos, state, world.getBlockState(mutablePos.setWithOffset(pos, face)), face)
				&& !(actualState.getBlock()instanceof CopycatBlock ufb
					&& !ufb.canFaceBeOccluded(actualState, face)))
				continue;
			CTType dataType = behaviour.getDataType(world, pos, state, face);
			if (dataType == null)
				continue;
			CTContext context = behaviour.buildContext(world, pos, state, face, dataType.getContextRequirement());
			data.put(face, dataType.getTextureIndex(context));
		}
		return data;
	}

	/// 26.x assembles block geometry from parts, and the level is handed in
	/// here, so the connection data is worked out at collect time rather than
	/// being stashed on ModelData beforehand.
	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
		List<BlockStateModelPart> parts) {
		int first = parts.size();
		super.collectParts(level, pos, state, random, parts);
		CTData data = createCTData(level, pos, state);
		for (int i = first; i < parts.size(); i++)
			parts.set(i, new ShiftedPart(parts.get(i), data, state, random));
	}

	/// Shifts each quad's UVs onto the connected-texture tile its face resolved
	/// to. Quads whose sprite is not the one the shift entry was built for are
	/// left alone.
	private class ShiftedPart extends DelegateModelPart {

		private final CTData data;
		private final BlockState state;
		private final RandomSource random;

		ShiftedPart(BlockStateModelPart wrapped, CTData data, BlockState state, RandomSource random) {
			super(wrapped);
			this.data = data;
			this.state = state;
			this.random = random;
		}

		@Override
		public List<BakedQuad> getQuads(Direction direction) {
			List<BakedQuad> quads = wrapped.getQuads(direction);
			List<BakedQuad> result = null;

			for (int i = 0; i < quads.size(); i++) {
				BakedQuad quad = quads.get(i);
				int index = data.get(quad.direction());
				if (index == -1)
					continue;

				CTSpriteShiftEntry spriteShift = behaviour.getShift(state, random, quad.direction(), quad.materialInfo().sprite());
				if (spriteShift == null || quad.materialInfo().sprite() != spriteShift.getOriginal())
					continue;

				float[] us = new float[4];
				float[] vs = new float[4];
				for (int vertex = 0; vertex < 4; vertex++) {
					us[vertex] = spriteShift.getTargetU(BakedQuadHelper.getU(quad, vertex), index);
					vs[vertex] = spriteShift.getTargetV(BakedQuadHelper.getV(quad, vertex), index);
				}
				BakedQuad newQuad = BakedQuadHelper.withUVs(quad, us, vs);

				if (result == null)
					result = new ArrayList<>(quads);
				result.set(i, newQuad);
			}

			return result == null ? quads : result;
		}
	}

	private static class CTData {
		private final int[] indices;

		public CTData() {
			indices = new int[6];
			Arrays.fill(indices, -1);
		}

		public void put(Direction face, int texture) {
			indices[face.get3DDataValue()] = texture;
		}

		public int get(Direction face) {
			return indices[face.get3DDataValue()];
		}
	}

}
