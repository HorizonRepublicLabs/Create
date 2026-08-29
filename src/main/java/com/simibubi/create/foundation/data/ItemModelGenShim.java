package com.simibubi.create.foundation.data;

import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/// The item model provider used to be reached the same way as the block one,
/// building models by name off a parent. Registrate's generator works from
/// templates and writes straight to the item, so this keeps the old shape.
public class ItemModelGenShim {

	private final RegistrateItemModelGenerator generator;

	public ItemModelGenShim(RegistrateItemModelGenerator generator) {
		this.generator = generator;
	}

	public Builder withExistingParent(String name, Identifier parent) {
		return new Builder(name, parent);
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
