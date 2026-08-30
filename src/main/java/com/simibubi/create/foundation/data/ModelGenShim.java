package com.simibubi.create.foundation.data;

import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;

import java.util.function.Consumer;

import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateLegacyBlockModelBuilder;

import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/// The old block model provider was reached through VariantModels.models(prov) and built
/// models by name. Registrate's generator builds them through templates
/// instead, so this keeps the old shape over the top: a model is just an
/// Identifier now, so getExistingFile hands one straight back.
public class ModelGenShim {

	private final RegistrateBlockModelGenerator generator;

	public ModelGenShim(RegistrateBlockModelGenerator generator) {
		this.generator = generator;
	}

	/// Models are named rather than wrapped, so an existing one is its own id.
	public Identifier getExistingFile(Identifier location) {
		return location;
	}

	public Builder withExistingParent(String name, Identifier parent) {
		return new Builder(name, ExtendedModelTemplateBuilder.builder()
			.parent(parent));
	}

	public Identifier cubeAll(String name, Identifier texture) {
		return withExistingParent(name, mc("block/cube_all")).texture("all", texture)
			.build();
	}

	public Identifier cubeColumn(String name, Identifier side, Identifier end) {
		return withExistingParent(name, mc("block/cube_column")).texture("side", side)
			.texture("end", end)
			.build();
	}

	public Identifier cubeColumnHorizontal(String name, Identifier side, Identifier end) {
		return withExistingParent(name, mc("block/cube_column_horizontal")).texture("side", side)
			.texture("end", end)
			.build();
	}

	public Identifier cubeBottomTop(String name, Identifier side, Identifier bottom, Identifier top) {
		return withExistingParent(name, mc("block/cube_bottom_top")).texture("side", side)
			.texture("bottom", bottom)
			.texture("top", top)
			.build();
	}

	public Identifier slab(String name, Identifier side, Identifier bottom, Identifier top) {
		return withExistingParent(name, mc("block/slab")).texture("side", side)
			.texture("bottom", bottom)
			.texture("top", top)
			.build();
	}

	public Identifier slabTop(String name, Identifier side, Identifier bottom, Identifier top) {
		return withExistingParent(name, mc("block/slab_top")).texture("side", side)
			.texture("bottom", bottom)
			.texture("top", top)
			.build();
	}

	private static Identifier mc(String path) {
		return Identifier.withDefaultNamespace(path);
	}

	/// Collects textures then builds, so a chain of texture() calls can end
	/// wherever an Identifier is expected.
	public class Builder {
		private final String name;
		private final ExtendedModelTemplateBuilder template;
		private final net.minecraft.client.data.models.model.TextureMapping mapping =
			new net.minecraft.client.data.models.model.TextureMapping();

		Builder(String name, ExtendedModelTemplateBuilder template) {
			this.name = name;
			this.template = template;
		}

		public Builder texture(String slot, Identifier texture) {
			TextureSlot key = TextureSlot.create(slot);
			template.requiredTextureSlot(key);
			mapping.put(key, new Material(texture));
			return this;
		}

		/// The template builds its elements through a consumer rather than a
		/// chain that has to be ended.
		public Builder element(Consumer<ElementBuilder> action) {
			template.element(action);
			return this;
		}

		public Identifier build() {
			RegistrateLegacyBlockModelBuilder builder = generator.withBuilder(template, mapping);
			return builder.build(generator.modLoc("block/" + name));
		}
	}
}
