package com.simibubi.create.foundation.data;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.data.VariantModels.ConfiguredModel;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

/// Forge's multipart blockstate builder is gone; vanilla's MultiPartGenerator
/// takes a condition and a variant per part instead of a fluent chain. Create's
/// generators are written against the chain, so this rebuilds that surface on
/// top of it.
public class MultipartModels {
	public static Builder getMultipartBuilder(RegistrateBlockModelGenerator generator, Block block) {
		return new Builder(generator, block);
	}

	public static class Builder {
		private final RegistrateBlockModelGenerator generator;
		private final Block block;
		private final MultiPartGenerator multipart;

		Builder(RegistrateBlockModelGenerator generator, Block block) {
			this.generator = generator;
			this.block = block;
			this.multipart = MultiPartGenerator.multiPart(block);
			// Create's generators have no final registration call; the generator is
			// mutable and accumulates, so hand it over now and keep adding to it.
			generator.blockStateOutput.accept(multipart);
		}

		public PartBuilder part() {
			return new PartBuilder(this);
		}

		void add(ConfiguredModel[] models, ConditionBuilder condition) {
			if (condition == null)
				multipart.with(VariantModels.toMultiVariant(models));
			else
				multipart.with(condition, VariantModels.toMultiVariant(models));
		}

		public void end() {}
	}

	public static class PartBuilder {
		private final Builder owner;
		private final List<ConfiguredModel> models = new ArrayList<>();
		private ConfiguredModel.Builder current = ConfiguredModel.builder();

		PartBuilder(Builder owner) {
			this.owner = owner;
		}

		public PartBuilder modelFile(Identifier model) {
			current.modelFile(model);
			return this;
		}

		public PartBuilder rotationX(int rotation) {
			current.rotationX(rotation);
			return this;
		}

		public PartBuilder rotationY(int rotation) {
			current.rotationY(rotation);
			return this;
		}

		public PartBuilder uvLock(boolean uvLock) {
			current.uvLock(uvLock);
			return this;
		}

		public PartBuilder weight(int weight) {
			current.weight(weight);
			return this;
		}

		/// Closes off one model and starts another, as the Forge builder did.
		public ConditionalPart addModel() {
			models.add(current.build()[0]);
			current = ConfiguredModel.builder();
			return new ConditionalPart(owner, models);
		}
	}

	public static class ConditionalPart {
		private final Builder owner;
		private final List<ConfiguredModel> models;
		private final ConditionBuilder condition = new ConditionBuilder();
		private boolean conditioned;

		ConditionalPart(Builder owner, List<ConfiguredModel> models) {
			this.owner = owner;
			this.models = models;
		}

		public <T extends Comparable<T>> ConditionalPart condition(Property<T> property, T value) {
			condition.term(property, value);
			conditioned = true;
			return this;
		}

		@SafeVarargs
		public final <T extends Comparable<T>> ConditionalPart condition(Property<T> property, T first, T... rest) {
			condition.term(property, first, rest);
			conditioned = true;
			return this;
		}

		public Builder end() {
			owner.add(models.toArray(new ConfiguredModel[0]), conditioned ? condition : null);
			return owner;
		}
	}
}
