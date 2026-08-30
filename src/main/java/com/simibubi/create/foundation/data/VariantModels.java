package com.simibubi.create.foundation.data;

import net.minecraft.world.level.block.TrapDoorBlock;

import net.minecraft.world.level.block.DoorBlock;

import net.minecraft.client.data.models.MultiVariant;

import net.minecraft.client.data.models.BlockModelGenerators;

import net.createmod.catnip.api.registry.RegisteredObjectsHelper;

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
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

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
			private final java.util.List<ConfiguredModel> finished = new java.util.ArrayList<>();

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

			/// Finishes the variant being built and starts another, so a weighted
			/// set of models can be described in one chain.
			public Builder nextModel() {
				finished.add(new ConfiguredModel(model, rotationX, rotationY, uvLock, weight));
				model = null;
				rotationX = 0;
				rotationY = 0;
				uvLock = false;
				weight = 1;
				return this;
			}

			/// The old builder could finish a single variant; keeping the name
			/// means the callers that end on one variant do not have to change.
			public ConfiguredModel buildLast() {
				return build()[0];
			}

			public ConfiguredModel[] build() {
				java.util.List<ConfiguredModel> all = new java.util.ArrayList<>(finished);
				all.add(new ConfiguredModel(model, rotationX, rotationY, uvLock, weight));
				return all.toArray(new ConfiguredModel[0]);
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
	/// Forge's provider had a two-argument form that used the block's own model.
	/// The generator takes variants rather than model names now, and Create only
	/// ever hands it models it has already written.
	public static void trapdoorBlock(RegistrateBlockModelGenerator generator, TrapDoorBlock block, Identifier bottom,
		Identifier top, Identifier open, boolean orientable) {
		generator.generateTrapdoorBlock(block, BlockModelGenerators.plainVariant(bottom),
			BlockModelGenerators.plainVariant(top), BlockModelGenerators.plainVariant(open), orientable);
	}

	public static void doorBlock(RegistrateBlockModelGenerator generator, DoorBlock block, Identifier bottom,
		Identifier top) {
		MultiVariant bottomVariant = BlockModelGenerators.plainVariant(bottom);
		MultiVariant topVariant = BlockModelGenerators.plainVariant(top);
		generator.generateDoorBlock(block, bottomVariant, bottomVariant, bottomVariant, bottomVariant, topVariant,
			topVariant, topVariant, topVariant);
	}

	public static void simpleBlock(RegistrateBlockModelGenerator generator, Block block) {
		simpleBlock(generator, block, generator.modLoc("block/" + RegisteredObjectsHelper.getKeyOrThrow(block)
			.getPath()));
	}

	/// For blocks whose one state picks between several weighted models.
	public static void simpleBlock(RegistrateBlockModelGenerator generator, Block block, ConfiguredModel[] models) {
		forAllStatesExcept(generator, block, state -> models, BlockStateProperties.WATERLOGGED);
	}

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

	/// Forge's provider took an angle offset on these; a few blocks pass one.
	public static void horizontalBlock(RegistrateBlockModelGenerator generator, Block block, Identifier model,
		int angleOffset) {
		forAllStatesExcept(generator, block, state -> ConfiguredModel.builder()
			.modelFile(model)
			.rotationY((((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()) + angleOffset
				+ 180) % 360)
			.build(), BlockStateProperties.WATERLOGGED);
	}

	public static void horizontalBlock(RegistrateBlockModelGenerator generator, Block block,
		ModelGenShim.Builder model, int angleOffset) {
		horizontalBlock(generator, block, model.build(), angleOffset);
	}

	public static void horizontalFaceBlock(RegistrateBlockModelGenerator generator, Block block,
		ModelGenShim.Builder model, int angleOffset) {
		horizontalFaceBlock(generator, block, model.build());
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

	/// For the blocks that pick a different model per state.
	public static void horizontalFaceBlock(RegistrateBlockModelGenerator generator, Block block,
		Function<BlockState, Identifier> modelFunc) {
		forAllStatesExcept(generator, block, state -> {
			AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
			return ConfiguredModel.builder()
				.modelFile(modelFunc.apply(state))
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

	/// VariantModels.models(prov) used to reach the block model provider; that is a template
	/// generator now, so this hands back the shim that keeps the old shape.
	public static ModelGenShim models(RegistrateBlockModelGenerator generator) {
		return new ModelGenShim(generator);
	}

	/// Same entry point for the item side; the overload picks the shim that
	/// matches whichever generator the datagen callback was handed.
	public static ItemModelGenShim models(RegistrateItemModelGenerator generator) {
		return new ItemModelGenShim(generator);
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

	/// The model chains end in a builder where the old provider handed back a
	/// ModelFile, so these build it for the caller.
	public static void simpleBlock(RegistrateBlockModelGenerator generator, Block block,
		ModelGenShim.Builder model) {
		simpleBlock(generator, block, model.build());
	}

	public static void horizontalBlock(RegistrateBlockModelGenerator generator, Block block,
		ModelGenShim.Builder model) {
		horizontalBlock(generator, block, model.build());
	}

	public static void directionalBlock(RegistrateBlockModelGenerator generator, Block block,
		ModelGenShim.Builder model) {
		directionalBlock(generator, block, model.build());
	}

	public static void horizontalFaceBlock(RegistrateBlockModelGenerator generator, Block block,
		ModelGenShim.Builder model) {
		horizontalFaceBlock(generator, block, model.build());
	}
}
