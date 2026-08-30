package com.simibubi.create.content.decoration.copycat;

import net.minecraft.world.level.BlockGetter;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import com.simibubi.create.foundation.model.DelegateModelPart;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
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

public abstract class CopycatModel extends BakedModelWrapperWithData {

	public static final ModelProperty<BlockState> MATERIAL_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<OcclusionData> OCCLUSION_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<ModelData> WRAPPED_DATA_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<Boolean> IS_EMISSIVE_PROPERTY = new ModelProperty<>();

	public CopycatModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		ModelData blockEntityData) {
		BlockState material = getMaterial(blockEntityData);
		builder.with(MATERIAL_PROPERTY, material);

		if (!(state.getBlock() instanceof CopycatBlock copycatBlock))
			return builder;

		OcclusionData occlusionData = new OcclusionData();
		gatherOcclusionData(world, pos, state, material, occlusionData, copycatBlock);
		builder.with(OCCLUSION_PROPERTY, occlusionData);

		ModelData wrappedData = world.getModelData(pos);
		builder.with(WRAPPED_DATA_PROPERTY, wrappedData);

		boolean isEmissive = material.emissiveRendering();
		builder.with(IS_EMISSIVE_PROPERTY, isEmissive);

		return builder;
	}

	private void gatherOcclusionData(BlockAndTintGetter level, BlockPos pos, BlockState state, BlockState material,
		OcclusionData occlusionData, CopycatBlock copycatBlock) {
		MutableBlockPos mutablePos = new MutableBlockPos();
		for (Direction face : Iterate.directions) {

			// Rubidium: Run an additional IForgeBlock.hidesNeighborFace check because it
			// seems to be missing in Block.shouldRenderFace
			MutableBlockPos neighbourPos = mutablePos.setWithOffset(pos, face);
			BlockState neighbourState = level.getBlockState(neighbourPos);
			if (state.supportsExternalFaceHiding()
				&& neighbourState.hidesNeighborFace(level, neighbourPos, state, face.getOpposite())) {
				occlusionData.occlude(face);
				continue;
			}

			if (!copycatBlock.canFaceBeOccluded(state, face))
				continue;
			if (!Block.shouldRenderFace(level, pos, material, level.getBlockState(neighbourPos), face))
				occlusionData.occlude(face);
		}
	}

	/// Copycats draw the material block's geometry cropped to their own shape.
	/// Geometry is parts now, so this collects the delegate's and replaces each
	/// with one that reports the cropped quads instead.
	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
		List<BlockStateModelPart> parts) {
		ModelData data = buildModelData(level, pos, state, level.getModelData(pos));
		BlockState material = getMaterial(data);
		if (material == null) {
			super.collectParts(level, pos, state, random, parts);
			return;
		}

		int first = parts.size();
		super.collectParts(level, pos, state, random, parts);
		for (int i = first; i < parts.size(); i++)
			parts.set(i, new CroppedPart(parts.get(i), state, material, data, random));
	}

	private class CroppedPart extends DelegateModelPart {

		private final BlockState state;
		private final BlockState material;
		private final ModelData data;
		private final RandomSource random;

		CroppedPart(BlockStateModelPart wrapped, BlockState state, BlockState material, ModelData data,
			RandomSource random) {
			super(wrapped);
			this.state = state;
			this.material = material;
			this.data = data;
			this.random = random;
		}

		@Override
		public List<BakedQuad> getQuads(Direction side) {
			// Rubidium: see below
			if (side != null && state.getBlock() instanceof CopycatBlock ccb
				&& ccb.shouldFaceAlwaysRender(state, side))
				return List.of();

			OcclusionData occlusionData = data.get(OCCLUSION_PROPERTY);
			if (occlusionData != null && occlusionData.isOccluded(side))
				return wrapped.getQuads(side);

			ModelData wrappedData = data.get(WRAPPED_DATA_PROPERTY);
			if (wrappedData == null)
				wrappedData = ModelData.EMPTY;

			List<BakedQuad> croppedQuads = getCroppedQuads(state, side, random, material, wrappedData);

			// Rubidium: render the side != null versions during side == null so they
			// are not culled away
			if (side == null && state.getBlock() instanceof CopycatBlock ccb) {
				boolean immutable = true;
				for (Direction nonOcclusionSide : Iterate.directions)
					if (ccb.shouldFaceAlwaysRender(state, nonOcclusionSide)) {
						if (immutable) {
							croppedQuads = new ArrayList<>(croppedQuads);
							immutable = false;
						}
						croppedQuads.addAll(
							getCroppedQuads(state, nonOcclusionSide, random, material, wrappedData));
					}
			}

			return croppedQuads;
		}
	}

	/**
	 * The returned list must not be mutated.
	 */
	protected abstract List<BakedQuad> getCroppedQuads(BlockState state, Direction side, RandomSource rand,
		BlockState material, ModelData wrappedData);

	public TextureAtlasSprite getParticleIconFor(ModelData data) {
		BlockState material = getMaterial(data);

		ModelData wrappedData = data.get(WRAPPED_DATA_PROPERTY);
		if (wrappedData == null)
			wrappedData = ModelData.EMPTY;

		return getModelOf(material).particleMaterial()
			.sprite();
	}

	@NotNull
	public static BlockState getMaterial(ModelData data) {
		BlockState material = data == null ? null : data.get(MATERIAL_PROPERTY);
		return material == null ? AllBlocks.COPYCAT_BASE.getDefaultState() : material;
	}

	/// Geometry comes from parts; this flattens a model's quads for one side.
	public static List<BakedQuad> collectQuads(BlockStateModel model, RandomSource rand, Direction side) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(rand, parts);
		List<BakedQuad> quads = new ArrayList<>();
		for (BlockStateModelPart part : parts)
			quads.addAll(part.getQuads(side));
		return quads;
	}

	public static BlockStateModel getModelOf(BlockState state) {
		return Minecraft.getInstance()
			.getModelManager()
			.getBlockStateModelSet()
			.get(state);
	}

	private static class OcclusionData {
		private final boolean[] occluded;

		public OcclusionData() {
			occluded = new boolean[6];
		}

		public void occlude(Direction face) {
			occluded[face.get3DDataValue()] = true;
		}

		public boolean isOccluded(Direction face) {
			return face != null && occluded[face.get3DDataValue()];
		}
	}

}
