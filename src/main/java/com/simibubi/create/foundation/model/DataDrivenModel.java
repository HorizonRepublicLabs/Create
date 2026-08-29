package com.simibubi.create.foundation.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

/// Shared shape for Create's models that decorate a delegate's geometry with
/// something worked out from the world.
///
/// Before 26.x these gathered into ModelData during a separate pass and read it
/// back in getQuads. Geometry comes from BlockStateModelParts now and the level
/// is handed to collectParts, so the data is gathered there and each part is
/// wrapped in one that rewrites its quads.
public abstract class DataDrivenModel<D> extends DelegateBlockStateModel {

	protected DataDrivenModel(BlockStateModel delegate) {
		super(delegate);
	}

	/// Null means nothing to decorate; the delegate's parts pass through.
	@Nullable
	protected abstract D gatherData(BlockAndTintGetter level, BlockPos pos, BlockState state);

	protected abstract List<BakedQuad> transformQuads(List<BakedQuad> quads, D data, BlockState state,
		RandomSource random, @Nullable Direction side);

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
		List<BlockStateModelPart> parts) {
		int first = parts.size();
		super.collectParts(level, pos, state, random, parts);

		D data = gatherData(level, pos, state);
		if (data == null)
			return;

		for (int i = first; i < parts.size(); i++)
			parts.set(i, new DecoratedPart(parts.get(i), data, state, random));
	}

	private class DecoratedPart extends DelegateModelPart {

		private final D data;
		private final BlockState state;
		private final RandomSource random;

		DecoratedPart(BlockStateModelPart wrapped, D data, BlockState state, RandomSource random) {
			super(wrapped);
			this.data = data;
			this.state = state;
			this.random = random;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable Direction direction) {
			return transformQuads(wrapped.getQuads(direction), data, state, random, direction);
		}
	}
}
