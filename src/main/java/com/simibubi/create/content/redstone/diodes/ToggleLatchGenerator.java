package com.simibubi.create.content.redstone.diodes;

import com.simibubi.create.foundation.data.ModelGenShim;

import com.simibubi.create.foundation.data.VariantModels;

import java.util.ArrayList;
import java.util.List;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class ToggleLatchGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> List<Identifier> createModels(DataGenContext<Block, T> ctx, ModelGenShim prov) {
		String name = ctx.getName();
		List<Identifier> models = new ArrayList<>(4);
		Identifier off = existing("latch_off");
		Identifier on = existing("latch_on");

		models.add(prov.getExistingFile(off));
		models.add(prov.withExistingParent(name + "_off_powered", off)
			.texture("top", texture(ctx, "powered")));
		models.add(prov.getExistingFile(on));
		models.add(prov.withExistingParent(name + "_on_powered", on)
			.texture("top", texture(ctx, "powered_powering")));

		return models;
	}

	@Override
	protected int getModelIndex(BlockState state) {
		return (state.getValue(ToggleLatchBlock.POWERING) ? 2 : 0) + (state.getValue(ToggleLatchBlock.POWERED) ? 1 : 0);
	}

}
