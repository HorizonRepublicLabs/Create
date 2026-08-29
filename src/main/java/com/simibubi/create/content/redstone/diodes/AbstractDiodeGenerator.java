package com.simibubi.create.content.redstone.diodes;

import com.simibubi.create.foundation.data.ModelGenShim;

import com.simibubi.create.foundation.data.VariantModels;

import java.util.List;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;

import net.neoforged.neoforge.client.model.generators.Identifier.ExistingModelFile;

public abstract class AbstractDiodeGenerator extends SpecialBlockStateGen {

	private List<Identifier> models;

	public static <I extends BlockItem> void diodeItemModel(DataGenContext<Item, I> c, RegistrateItemModelGenerator p) {
		String name = c.getName();
		String path = "block/diodes/";
		ItemModelBuilder builder = VariantModels.models(p).withExistingParent(name, p.modLoc(path + name));
		builder.texture("top", path + name + "/item");
	}

	@Override
	protected final int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected final int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(AbstractDiodeBlock.FACING));
	}

	protected abstract <T extends Block> List<Identifier> createModels(DataGenContext<Block, T> ctx,
																	  ModelGenShim prov);

	protected abstract int getModelIndex(BlockState state);

	@Override
	public final <T extends Block> Identifier getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		if (models == null)
			models = createModels(ctx, VariantModels.models(prov));
		return models.get(getModelIndex(state));
	}

	protected ExistingModelFile existingModel(ModelGenShim prov, String name) {
		return VariantModels.models(prov).getExistingFile(existing(name));
	}

	protected Identifier existing(String name) {
		return Create.asResource("block/diodes/" + name);
	}

	protected <T extends Block> Identifier texture(DataGenContext<Block, T> ctx, String name) {
		return Create.asResource("block/diodes/" + ctx.getName() + "/" + name);
	}

	protected Identifier poweredTorch() {
		return Identifier.withDefaultNamespace("block/redstone_torch");
	}

}
