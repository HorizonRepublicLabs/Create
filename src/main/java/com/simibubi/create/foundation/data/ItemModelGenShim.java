package com.simibubi.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;

import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.minecraft.world.item.Item;

/// The item model provider used to be reached the same way as the block one,
/// building models by name off a parent. Registrate's generator works from
/// templates and writes straight to the item, so this keeps the old shape.
public class ItemModelGenShim {

	private final RegistrateItemModelGenerator generator;

	public ItemModelGenShim(RegistrateItemModelGenerator generator) {
		this.generator = generator;
	}

	/// A few item models reuse the block cube_column shape.
	public Identifier cubeColumn(String name, Identifier side, Identifier end) {
		return withExistingParent(name, Identifier.withDefaultNamespace("block/cube_column"))
			.texture("side", side)
			.texture("end", end)
			.build();
	}

	public Builder withExistingParent(String name, Identifier parent) {
		return new Builder(name, parent);
	}

	/// Forge's provider had generated(ctx, texture) for a flat item model;
	/// Registrate spells it generateFlatItem.
	public void generated(DataGenContext<Item, ?> ctx, Identifier texture) {
		generator.generateFlatItem(ctx.get(), new Material(texture));
	}

	public Identifier getExistingFile(Identifier location) {
		return location;
	}

	public Identifier modLoc(String path) {
		return generator.modLoc(path);
	}

	public Identifier mcLoc(String path) {
		return generator.mcLoc(path);
	}

	public class Builder {

		private final String name;
		private final ExtendedModelTemplateBuilder template;
		private final TextureMapping mapping = new TextureMapping();

		Builder(String name, Identifier parent) {
			this.name = name;
			this.template = ExtendedModelTemplateBuilder.builder()
				.parent(parent);
		}

		public Builder texture(String slot, Identifier texture) {
			TextureSlot key = TextureSlot.create(slot);
			template.requiredTextureSlot(key);
			mapping.put(key, new Material(texture));
			return this;
		}

		public Identifier build() {
			return template.build()
				.create(generator.modLoc("item/" + name), mapping, generator.modelOutput);
		}
	}
}
