package com.simibubi.create.content.logistics.tableCloth;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import com.simibubi.create.foundation.model.DataDrivenModel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.SpriteShiftEntry;
import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;
import net.neoforged.neoforge.model.data.ModelProperty;

public class TableClothModel extends DataDrivenModel<EnumSet<Direction>> {


	private static final Map<TableClothBlock, List<List<BakedQuad>>> CORNERS = new HashMap<>();

	public TableClothModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	public static void reload() {
		CORNERS.clear();
	}



	private List<BakedQuad> getCorner(TableClothBlock block, int corner, @NotNull RandomSource rand,
		@Nullable RenderType renderType) {
		if (!CORNERS.containsKey(block)) {
			TextureAtlasSprite targetSprite = delegate.particleMaterial()
				.sprite();
			List<List<BakedQuad>> list = new ArrayList<>();

			for (PartialModel pm : List.of(AllPartialModels.TABLE_CLOTH_SW, AllPartialModels.TABLE_CLOTH_NW,
				AllPartialModels.TABLE_CLOTH_NE, AllPartialModels.TABLE_CLOTH_SE))
				list.add(getCornerQuads(rand, renderType, targetSprite, pm));

			CORNERS.put(block, list);
		}

		return CORNERS.get(block)
			.get(corner);
	}

	private List<BakedQuad> getCornerQuads(RandomSource rand, RenderType renderType, TextureAtlasSprite targetSprite,
		PartialModel pm) {
		List<BakedQuad> quads = new ArrayList<>();

		List<BlockStateModelPart> cornerParts = new ArrayList<>();
		pm.get()
			.collectParts(rand, cornerParts);
		List<BakedQuad> sourceQuads = new ArrayList<>();
		for (BlockStateModelPart part : cornerParts)
			sourceQuads.addAll(part.getQuads(null));
		for (BakedQuad quad : sourceQuads) {
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

	@Override
	protected EnumSet<Direction> gatherData(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		EnumSet<Direction> culledSides = EnumSet.noneOf(Direction.class);
		for (Direction side : Iterate.horizontalDirections)
			if (level.getBlockState(pos.relative(side))
				.getBlock() instanceof TableClothBlock)
				culledSides.add(side);
		return culledSides;
	}

	@Override
	protected List<BakedQuad> transformQuads(List<BakedQuad> mainQuads, EnumSet<Direction> culled,
		BlockState state, RandomSource rand, Direction side) {
		if (side == null || side.getAxis() == Axis.Y)
			return mainQuads;
		if (culled.contains(side.getClockWise()))
			return mainQuads;
		if (!(state.getBlock() instanceof TableClothBlock dcb))
			return mainQuads;

		List<BakedQuad> copyOf = new ArrayList<>(mainQuads);
		copyOf.addAll(getCorner(dcb, side.get2DDataValue(), rand, null));
		return copyOf;
	}

}
