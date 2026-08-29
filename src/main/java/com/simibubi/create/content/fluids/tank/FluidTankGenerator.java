package com.simibubi.create.content.fluids.tank;

import com.simibubi.create.foundation.data.VariantModels;

import net.minecraft.resources.Identifier;

import com.simibubi.create.content.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class FluidTankGenerator extends SpecialBlockStateGen {

	private String prefix;

	public FluidTankGenerator() {
		this("");
	}

	public FluidTankGenerator(String prefix) {
		this.prefix = prefix;
	}

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return 0;
	}

	@Override
	public <T extends Block> Identifier getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		Boolean top = state.getValue(FluidTankBlock.TOP);
		Boolean bottom = state.getValue(FluidTankBlock.BOTTOM);
		Shape shape = state.getValue(FluidTankBlock.SHAPE);

		String shapeName = "middle";
		if (top && bottom)
			shapeName = "single";
		else if (top)
			shapeName = "top";
		else if (bottom)
			shapeName = "bottom";

		String modelName = shapeName + (shape == Shape.PLAIN ? "" : "_" + shape.getSerializedName());

		if (!prefix.isEmpty())
			return VariantModels.models(prov)
				.withExistingParent(prefix + modelName, prov.modLoc("block/fluid_tank/block_" + modelName))
				.texture("0", prov.modLoc("block/" + prefix + "casing"))
				.texture("1", prov.modLoc("block/" + prefix + "fluid_tank"))
				.texture("3", prov.modLoc("block/" + prefix + "fluid_tank_window"))
				.texture("4", prov.modLoc("block/" + prefix + "casing"))
				.texture("5", prov.modLoc("block/" + prefix + "fluid_tank_window_single"))
				.texture("particle", prov.modLoc("block/" + prefix + "fluid_tank"));

		return AssetLookup.partialBaseModel(ctx, prov, modelName);
	}

}
