package com.simibubi.create.content.redstone.diodes;

import com.simibubi.create.foundation.data.VariantModels;

import java.util.ArrayList;
import java.util.List;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;


public class PoweredLatchGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> List<Identifier> createModels(DataGenContext<Block, T> ctx, BlockModelProvider prov) {
		List<Identifier> models = new ArrayList<>(2);
		String name = ctx.getName();
		Identifier off = existing("latch_off");
		Identifier on = existing("latch_on");

		models.add(VariantModels.models(prov).withExistingParent(name, off)
			.texture("top", texture(ctx, "idle")));
		models.add(VariantModels.models(prov).withExistingParent(name + "_powered", on)
			.texture("top", texture(ctx, "powering")));

		return models;
	}

	@Override
	protected int getModelIndex(BlockState state) {
		return state.getValue(PoweredLatchBlock.POWERING) ? 1 : 0;
	}

}
