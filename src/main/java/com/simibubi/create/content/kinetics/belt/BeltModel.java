package com.simibubi.create.content.kinetics.belt;

import net.minecraft.client.resources.model.sprite.Material;

import net.minecraft.core.BlockPos;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import net.minecraft.client.renderer.block.BlockAndTintGetter;

import com.simibubi.create.foundation.model.DataDrivenModel;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.api.client.render.SpriteShiftEntry;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class BeltModel extends DataDrivenModel<ModelData> {

	public static final ModelProperty<CasingType> CASING_PROPERTY = new ModelProperty<>();
	public static final ModelProperty<Boolean> COVER_PROPERTY = new ModelProperty<>();

	private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

	public BeltModel(BlockStateModel template) {
		super(template);
	}

	/// getParticleIcon(ModelData) became particleMaterial, which is handed the
	/// level and can look the data up itself.
	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		ModelData data = level.getModelData(pos);
		if (!data.has(CASING_PROPERTY))
			return super.particleMaterial(level, pos, state);
		CasingType type = data.get(CASING_PROPERTY);
		if (type == CasingType.NONE || type == CasingType.BRASS)
			return super.particleMaterial(level, pos, state);
		return new Material.Baked(AllSpriteShifts.ANDESITE_CASING.getOriginal(), false);
	}

	@Override
	protected ModelData gatherData(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		ModelData data = level.getModelData(pos);
		return data.has(CASING_PROPERTY) ? data : null;
	}

	@Override
	protected List<BakedQuad> transformQuads(List<BakedQuad> quads, ModelData extraData, BlockState state,
		RandomSource rand, Direction side) {
		boolean cover = extraData.get(COVER_PROPERTY);
		CasingType type = extraData.get(CASING_PROPERTY);
		boolean brassCasing = type == CasingType.BRASS;

		if (type == CasingType.NONE || brassCasing && !cover)
			return quads;

		quads = new ArrayList<>(quads);

		if (cover) {
			boolean alongX = state.getValue(BeltBlock.HORIZONTAL_FACING)
				.getAxis() == Axis.X;
			BlockStateModel coverModel =
				(brassCasing ? alongX ? AllPartialModels.BRASS_BELT_COVER_X : AllPartialModels.BRASS_BELT_COVER_Z
					: alongX ? AllPartialModels.ANDESITE_BELT_COVER_X : AllPartialModels.ANDESITE_BELT_COVER_Z).get();
			List<BlockStateModelPart> coverParts = new ArrayList<>();
			coverModel.collectParts(rand, coverParts);
			for (BlockStateModelPart part : coverParts)
				quads.addAll(part.getQuads(side));
		}

		if (brassCasing)
			return quads;

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);
			if (quad.materialInfo().sprite() != SPRITE_SHIFT.getOriginal())
				continue;

			BakedQuadHelper.Editor edit = BakedQuadHelper.edit(quad);
			for (int vertex = 0; vertex < 4; vertex++) {
				edit.setU(vertex, SPRITE_SHIFT.getTargetU(edit.getU(vertex)));
				edit.setV(vertex, SPRITE_SHIFT.getTargetV(edit.getV(vertex)));
			}
			quads.set(i, edit.build());
		}

		return quads;
	}

}
