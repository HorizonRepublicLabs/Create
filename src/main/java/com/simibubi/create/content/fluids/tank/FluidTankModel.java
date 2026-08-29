package com.simibubi.create.content.fluids.tank;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import com.simibubi.create.foundation.model.DelegateModelPart;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;
import net.neoforged.neoforge.model.data.ModelProperty;

public class FluidTankModel extends CTModel {

	protected static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

	public static FluidTankModel standard(BlockStateModel originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.FLUID_TANK, AllSpriteShifts.FLUID_TANK_TOP,
			AllSpriteShifts.FLUID_TANK_INNER);
	}

	public static FluidTankModel creative(BlockStateModel originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.CREATIVE_FLUID_TANK, AllSpriteShifts.CREATIVE_CASING,
			AllSpriteShifts.CREATIVE_CASING);
	}

	private FluidTankModel(BlockStateModel originalModel, CTSpriteShiftEntry side, CTSpriteShiftEntry top,
		CTSpriteShiftEntry inner) {
		super(originalModel, new FluidTankCTBehaviour(side, top, inner));
	}

	/// Faces shared with a connected neighbour are dropped. Culling used to
	/// happen by asking the delegate for one side at a time and skipping the
	/// culled ones; parts report their quads per side, so the same choice is
	/// made in the wrapper.
	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
		List<BlockStateModelPart> parts) {
		CullData cullData = new CullData();
		for (Direction d : Iterate.horizontalDirections)
			cullData.setCulled(d, ConnectivityHandler.isConnected(level, pos, pos.relative(d)));

		int first = parts.size();
		super.collectParts(level, pos, state, random, parts);
		for (int i = first; i < parts.size(); i++)
			parts.set(i, new CulledPart(parts.get(i), cullData));
	}

	private static class CulledPart extends DelegateModelPart {

		private final CullData cullData;

		CulledPart(BlockStateModelPart wrapped, CullData cullData) {
			super(wrapped);
			this.cullData = cullData;
		}

		@Override
		public List<BakedQuad> getQuads(Direction direction) {
			if (direction != null && cullData.isCulled(direction))
				return List.of();
			return wrapped.getQuads(direction);
		}
	}

	private static class CullData {
		boolean[] culledFaces;

		public CullData() {
			culledFaces = new boolean[4];
			Arrays.fill(culledFaces, false);
		}

		void setCulled(Direction face, boolean cull) {
			if (face.getAxis()
				.isVertical())
				return;
			culledFaces[face.get2DDataValue()] = cull;
		}

		boolean isCulled(Direction face) {
			if (face.getAxis()
				.isVertical())
				return false;
			return culledFaces[face.get2DDataValue()];
		}
	}

}
