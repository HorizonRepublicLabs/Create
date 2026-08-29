package com.simibubi.create.content.decoration.copycat;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.api.client.render.SpriteShiftEntry;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;

public class CopycatBarsModel extends CopycatModel {

	public CopycatBarsModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Override
	protected List<BakedQuad> getCroppedQuads(BlockState state, Direction side, RandomSource rand, BlockState material,
											  ModelData wrappedData) {
		BlockStateModel model = getModelOf(material);
		List<BakedQuad> superQuads = collectQuads(delegate, rand, side);
		TextureAtlasSprite targetSprite = model.particleMaterial().sprite();

		boolean vertical = state.getValue(CopycatPanelBlock.FACING)
			.getAxis() == Axis.Y;

		if (side != null && (vertical || side.getAxis() == Axis.Y)) {
			List<BakedQuad> templateQuads = collectQuads(model, rand, null);
			for (BakedQuad quad : templateQuads) {
				if (quad.direction() != Direction.UP)
					continue;
				targetSprite = quad.materialInfo().sprite();
				break;
			}
		}

		if (targetSprite == null)
			return superQuads;

		List<BakedQuad> quads = new ArrayList<>();

		for (BakedQuad quad : superQuads) {
			TextureAtlasSprite original = quad.materialInfo().sprite();
			BakedQuadHelper.Editor edit = BakedQuadHelper.edit(quad);
			for (int vertex = 0; vertex < 4; vertex++) {
				edit.setU(vertex, targetSprite
					.getU(SpriteShiftEntry.getUnInterpolatedU(original, edit.getU(vertex))));
				edit.setV(vertex, targetSprite
					.getV(SpriteShiftEntry.getUnInterpolatedV(original, edit.getV(vertex))));
			}
			quads.add(edit.build());
		}

		return quads;
	}

}
