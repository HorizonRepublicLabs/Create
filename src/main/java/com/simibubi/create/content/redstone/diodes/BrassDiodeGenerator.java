package com.simibubi.create.content.redstone.diodes;

import com.simibubi.create.foundation.data.ModelGenShim;

import com.simibubi.create.foundation.data.VariantModels;

import java.util.ArrayList;
import java.util.List;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class BrassDiodeGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> List<Identifier> createModels(DataGenContext<Block, T> ctx, ModelGenShim prov) {
		List<Identifier> models = new ArrayList<>(4);
		String name = ctx.getName();
		Identifier template = existing(name);

		models.add(VariantModels.models(prov).getExistingFile(template));
		models.add(VariantModels.models(prov).withExistingParent(name + "_powered", template)
			.texture("top", texture(ctx, "powered")));
		models.add(VariantModels.models(prov).withExistingParent(name + "_powering", template)
			.texture("torch", poweredTorch())
			.texture("top", texture(ctx, "powering")));
		models.add(VariantModels.models(prov).withExistingParent(name + "_powered_powering", template)
			.texture("torch", poweredTorch())
			.texture("top", texture(ctx, "powered_powering")));

		return models;
	}

	@Override
	protected int getModelIndex(BlockState state) {
		return (state.getValue(BrassDiodeBlock.POWERING) ^ state.getValue(BrassDiodeBlock.INVERTED) ? 2 : 0)
			+ (state.getValue(BrassDiodeBlock.POWERED) ? 1 : 0);
	}

}
