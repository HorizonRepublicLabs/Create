package com.simibubi.create.foundation.data;

import com.simibubi.create.content.legacy.ChromaticCompoundColor;

import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;

import net.minecraft.client.resources.model.sprite.Material;

import com.simibubi.create.Create;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModelWrapper;

import java.util.Optional;

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
		// Registrate no longer builds a block-item model by suffix, so the parent
		// is named directly.
		return VariantModels.models(prov)
			.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName() + "/item"));
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
			// An item needs a model definition of its own now, not just a model file
			Identifier model = VariantModels.models(p)
				.withExistingParent(c.getName(), p.modLoc(path))
				.build();
			p.itemModelOutput.accept(c.get(), ItemModelUtils.plainModel(model));
		};
	}

	public static <I extends Item> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelGenerator> customGenericItemModel(
		String... folders) {
		return (c, p) -> {
			String path = "block";
			for (String string : folders)
				path += "/" + ("_".equals(string) ? c.getName() : string);
			// An item needs a model definition of its own now, not just a model file
			Identifier model = VariantModels.models(p)
				.withExistingParent(c.getName(), p.modLoc(path))
				.build();
			p.itemModelOutput.accept(c.get(), ItemModelUtils.plainModel(model));
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
			Identifier baseModel = baseModelFunc.apply(state);
			Integer integer = state.getValue(property);
			return VariantModels.models(prov)
				.withExistingParent(ctx.getName() + "_" + integer, baseModel)
				.texture("indicator", prov.modLoc("block/indicator/" + integer))
				.build();
		};
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> existingItemModel() {
		return (c, p) -> VariantModels.models(p).getExistingFile(p.modLoc("item/" + c.getName()));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModel(String name) {
		return (c, p) -> VariantModels.models(p).getExistingFile(p.modLoc("item/" + name));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModelWithPartials() {
		return (c, p) -> VariantModels.models(p).withExistingParent(c.getName(), p.modLoc("item/" + c.getName() + "/item"));
	}


	/// An item that draws itself names its renderer in its model definition, and
	/// the base model still supplies the transforms, so both are written here.
	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> customItemModel(
		String rendererName) {
		return (c, p) -> {
			Identifier base = p.modLoc("item/" + c.getName() + "/item");
			VariantModels.models(p)
				.withExistingParent(c.getName(), base);
			named(c, p, base, rendererName);
		};
	}

	/// For the hand-drawn items whose base is a plain flat model rather than a
	/// folder of parts.
	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> customFlatItemModel(
		String rendererName) {
		return (c, p) -> {
			Identifier base = p.modLoc("item/" + c.getName());
			// Only the model file: generateFlatItem would also claim the item's model
			// definition, and the custom renderer wants that slot.
			ModelTemplates.FLAT_ITEM.create(base, TextureMapping.layer0(new Material(base)), p.modelOutput);
			named(c, p, base, rendererName);
		};
	}

	private static <T extends Item> void named(DataGenContext<Item, T> c, RegistrateItemModelGenerator p,
		Identifier base, String rendererName) {
		p.itemModelOutput.accept(c.get(),
			new CustomRenderedItemModelWrapper.Unbaked(base, Create.asResource(rendererName), Optional.empty()));
	}

	/// The chromatic compound's three layers each shift colour on their own, and
	/// a model names a tint source per layer.
	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> chromaticItemModel() {
		return (c, p) -> p.itemModelOutput.accept(c.get(),
			ItemModelUtils.tintedModel(p.modLoc("item/" + c.getName()), new ChromaticCompoundColor(0),
				new ChromaticCompoundColor(1), new ChromaticCompoundColor(2)));
	}
}
