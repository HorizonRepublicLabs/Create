package com.simibubi.create.foundation.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;

public abstract class BakedModelWrapperWithData extends DelegateBlockStateModel {

	public BakedModelWrapperWithData(BlockStateModel originalModel) {
		super(originalModel);
	}

	/// Nothing hands a model its data ahead of time any more: the level reaches
	/// collectParts, so a model builds what it needs there and asks for this.
	public final ModelData buildModelData(BlockAndTintGetter world, BlockPos pos, BlockState state,
		ModelData blockEntityData) {
		Builder builder = ModelData.builder();
		if (delegate instanceof BakedModelWrapperWithData wrapper)
			wrapper.gatherModelData(builder, world, pos, state, blockEntityData);
		gatherModelData(builder, world, pos, state, blockEntityData);
		return builder.build();
	}

	protected abstract ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world,
		BlockPos pos, BlockState state, ModelData blockEntityData);

}
