package com.simibubi.create.foundation.data;

import java.util.function.Function;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;


public class AssetLookup {

	/**
	 * Custom block models packaged with other partials. Example:
	 * models/block/schematicannon/block.json <br>
	 * <br>
	 * Adding "powered", "vertical" will look for /block_powered_vertical.json
	 */
	public static Identifier partialBaseModel(DataGenContext<?, ?> ctx, RegistrateBlockModelGenerator prov,
		String... suffix) {
		String string = "/block";
		for (String suf : suffix)
			if (!suf.isEmpty())
				string += "_" + suf;
		final String location = "block/" + ctx.getName() + string;
		return VariantModels.models(prov)
			.getExistingFile(prov.modLoc(location));
	}

	/**
	 * Custom block model from models/block/x.json
	 */
	public static Identifier standardModel(DataGenContext<?, ?> ctx, RegistrateBlockModelGenerator prov) {
		return VariantModels.models(prov)
			.getExistingFile(prov.modLoc("block/" + ctx.getName()));
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/x/item.json
	 */
	public static <I extends BlockItem> ItemModelGenShim.Builder customItemModel(DataGenContext<Item, I> ctx,
		RegistrateItemModelGenerator prov) {
		return prov.blockItem(() -> ctx.getEntry()
			.getBlock(), "/item");
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/folders[0]/folders[1]/.../item.json "_" will be replaced by the
	 * item name
	 */
	public static <I extends BlockItem> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelGenerator> customBlockItemModel(
		String... folders) {
		return (c, p) -> {
			String path = "block";
			for (String string : folders)
				path += "/" + ("_".equals(string) ? c.getName() : string);
			VariantModels.models(p).withExistingParent(c.getName(), p.modLoc(path));
		};
	}

	public static <I extends Item> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelGenerator> customGenericItemModel(
		String... folders) {
		return (c, p) -> {
			String path = "block";
			for (String string : folders)
				path += "/" + ("_".equals(string) ? c.getName() : string);
			VariantModels.models(p).withExistingParent(c.getName(), p.modLoc(path));
		};
	}

	public static Function<BlockState, Identifier> forPowered(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov) {
		return state -> state.getValue(BlockStateProperties.POWERED) ? partialBaseModel(ctx, prov, "powered")
			: partialBaseModel(ctx, prov);
	}

	public static Function<BlockState, Identifier> forPowered(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov, String path) {
		return state -> VariantModels.models(prov)
			.getExistingFile(
				prov.modLoc("block/" + path + (state.getValue(BlockStateProperties.POWERED) ? "_powered" : "")));
	}

	public static Function<BlockState, Identifier> withIndicator(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov, Function<BlockState, Identifier> baseModelFunc, IntegerProperty property) {
		return state -> {
			Identifier baseModel = baseModelFunc.apply(state)
				.getLocation();
			Integer integer = state.getValue(property);
			return VariantModels.models(prov)
				.withExistingParent(ctx.getName() + "_" + integer, baseModel)
				.texture("indicator", "block/indicator/" + integer);
		};
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> existingItemModel() {
		return (c, p) -> VariantModels.models(p).getExistingFile(p.modLoc("item/" + c.getName()));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModel(String name) {
		return (c, p) -> VariantModels.models(p).getExistingFile(p.modLoc("item/" + name));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModelWithPartials() {
		return (c, p) -> VariantModels.models(p).withExistingParent("item/" + c.getName(), p.modLoc("item/" + c.getName() + "/item"));
	}

}
