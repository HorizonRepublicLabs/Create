package com.simibubi.create.foundation.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.math.Quadrant;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/// 26.x replaced Forge's blockstate provider with vanilla's model generators,
/// which dispatch per property rather than per state. Create's generators are
/// written the other way round -- a function from BlockState to models -- so
/// this restores that shape on top of the new BlockModelDefinitionGenerator,
/// which is itself just a block plus a map from variant key to model.
public class VariantModels {

	/// Stand-in for Forge's ConfiguredModel.
	public record ConfiguredModel(Identifier model, int rotationX, int rotationY, boolean uvLock, int weight) {

		public static Builder builder() {
			return new Builder();
		}

		public static ConfiguredModel of(Identifier model) {
			return new ConfiguredModel(model, 0, 0, false, 1);
		}

		public ConfiguredModel[] toArray() {
			return new ConfiguredModel[] { this };
		}

		public static class Builder {
			private Identifier model;
			private int rotationX;
			private int rotationY;
			private boolean uvLock;
			private int weight = 1;

			public Builder modelFile(Identifier model) {
				this.model = model;
				return this;
			}

			public Builder rotationX(int rotationX) {
				this.rotationX = rotationX;
				return this;
			}

			public Builder rotationY(int rotationY) {
				this.rotationY = rotationY;
				return this;
			}

			public Builder uvLock(boolean uvLock) {
				this.uvLock = uvLock;
				return this;
			}

			public Builder weight(int weight) {
				this.weight = weight;
				return this;
			}

			public ConfiguredModel[] build() {
				return new ConfiguredModel[] { new ConfiguredModel(model, rotationX, rotationY, uvLock, weight) };
			}
		}
	}

	public static void forAllStates(RegistrateBlockModelGenerator generator, Block block,
		Function<BlockState, ConfiguredModel[]> modelFunc) {
		forAllStatesExcept(generator, block, modelFunc);
	}

	/// Properties listed in `ignored` are left out of the variant key, so every
	/// state that differs only in those collapses onto one entry -- what
	/// forAllStatesExcept did for WATERLOGGED.
	public static void forAllStatesExcept(RegistrateBlockModelGenerator generator, Block block,
		Function<BlockState, ConfiguredModel[]> modelFunc, Property<?>... ignored) {
		Set<Property<?>> skip = Set.copyOf(Arrays.asList(ignored));
		Map<String, BlockStateModel.Unbaked> variants = new LinkedHashMap<>();

		for (BlockState state : block.getStateDefinition()
			.getPossibleStates()) {
			String key = variantKey(state, skip);
			if (variants.containsKey(key))
				continue;
			variants.put(key, toMultiVariant(modelFunc.apply(state)).toUnbaked());
		}

		accept(generator, block, variants);
	}

	/// The old provider had simpleBlock/directionalBlock/horizontalBlock and
	/// friends taking a model file. Registrate's generator renamed and reshaped
	/// them, so these keep the original call shape on top of forAllStatesExcept.
	public static void simpleBlock(RegistrateBlockModelGenerator generator, Block block, Identifier model) {
		forAllStatesExcept(generator, block, state -> ConfiguredModel.of(model).toArray(),
			BlockStateProperties.WATERLOGGED);
	}

	public static void directionalBlock(RegistrateBlockModelGenerator generator, Block block, Identifier model) {
		forAllStatesExcept(generator, block, state -> {
			Direction dir = state.getValue(BlockStateProperties.FACING);
			return ConfiguredModel.builder()
				.modelFile(model)
				.rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
				.rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
				.build();
		}, BlockStateProperties.WATERLOGGED);
	}

	public static void horizontalBlock(RegistrateBlockModelGenerator generator, Block block, Identifier model) {
		forAllStatesExcept(generator, block, state -> ConfiguredModel.builder()
			.modelFile(model)
			.rotationY((((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()) + 180) % 360)
			.build(), BlockStateProperties.WATERLOGGED);
	}

	public static void horizontalFaceBlock(RegistrateBlockModelGenerator generator, Block block, Identifier model) {
		forAllStatesExcept(generator, block, state -> {
			AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
			return ConfiguredModel.builder()
				.modelFile(model)
				.rotationX(face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180)
				.rotationY((((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot())
					+ (face == AttachFace.CEILING ? 0 : 180)) % 360)
				.build();
		}, BlockStateProperties.WATERLOGGED);
	}

	public static void simpleBlock(RegistrateBlockModelGenerator generator, Block block,
		Function<BlockState, Identifier> modelFunc) {
		forAllStatesExcept(generator, block, state -> ConfiguredModel.of(modelFunc.apply(state)).toArray(),
			BlockStateProperties.WATERLOGGED);
	}

	public static void directionalBlock(RegistrateBlockModelGenerator generator, Block block,
		Function<BlockState, Identifier> modelFunc) {
		forAllStatesExcept(generator, block, state -> {
			Direction dir = state.getValue(BlockStateProperties.FACING);
			return ConfiguredModel.builder()
				.modelFile(modelFunc.apply(state))
				.rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
				.rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
				.build();
		}, BlockStateProperties.WATERLOGGED);
	}

	public static void horizontalBlock(RegistrateBlockModelGenerator generator, Block block,
		Function<BlockState, Identifier> modelFunc) {
		forAllStatesExcept(generator, block, state -> ConfiguredModel.builder()
			.modelFile(modelFunc.apply(state))
			.rotationY((((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()) + 180) % 360)
			.build(), BlockStateProperties.WATERLOGGED);
	}

	public static void accept(RegistrateBlockModelGenerator generator, Block block,
		Map<String, BlockStateModel.Unbaked> variants) {
		generator.blockStateOutput.accept(new BlockModelDefinitionGenerator() {
			@Override
			public Block block() {
				return block;
			}

			@Override
			public BlockStateModelDispatcher create() {
				return new BlockStateModelDispatcher(
					Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(new HashMap<>(variants))),
					Optional.empty());
			}
		});
	}

	public static MultiVariant toMultiVariant(ConfiguredModel... models) {
		List<Weighted<Variant>> entries = new ArrayList<>();
		for (ConfiguredModel model : models)
			entries.add(new Weighted<>(toVariant(model), Math.max(1, model.weight())));
		return new MultiVariant(WeightedList.of(entries));
	}

	public static Variant toVariant(ConfiguredModel model) {
		Variant variant = new Variant(model.model());
		if (model.rotationX() != 0)
			variant = variant.withXRot(quadrant(model.rotationX()));
		if (model.rotationY() != 0)
			variant = variant.withYRot(quadrant(model.rotationY()));
		if (model.uvLock())
			variant = variant.withUvLock(true);
		return variant;
	}

	private static Quadrant quadrant(int degrees) {
		return switch (Math.floorMod(degrees, 360)) {
			case 90 -> Quadrant.R90;
			case 180 -> Quadrant.R180;
			case 270 -> Quadrant.R270;
			default -> Quadrant.R0;
		};
	}

	/// Same shape vanilla writes: name=value pairs, comma separated, sorted by
	/// property name so the key is stable.
	private static String variantKey(BlockState state, Set<Property<?>> skip) {
		return state.getValues()
			.filter(value -> !skip.contains(value.property()))
			.map(Property.Value::toString)
			.sorted()
			.collect(Collectors.joining(","));
	}

	private VariantModels() {}
}
