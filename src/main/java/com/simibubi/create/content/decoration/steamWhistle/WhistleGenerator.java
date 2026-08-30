package com.simibubi.create.content.decoration.steamWhistle;

import com.simibubi.create.foundation.data.VariantModels;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class WhistleGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(WhistleBlock.FACING));
	}

	@Override
	public <T extends Block> Identifier getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		String wall = state.getValue(WhistleBlock.WALL) ? "wall" : "floor";
		String size = state.getValue(WhistleBlock.SIZE)
			.getSerializedName();
		boolean powered = state.getValue(WhistleBlock.POWERED);
		Identifier model = AssetLookup.partialBaseModel(ctx, prov, size, wall);
		if (!powered)
			return model;
		// A model is an identifier now.
		Identifier parentLocation = model;
		return VariantModels.models(prov)
			.withExistingParent(parentLocation.getPath() + "_powered", parentLocation)
			.texture("2", Create.asResource("block/copper_redstone_plate_powered")).build();
	}

}
