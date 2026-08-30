
package com.simibubi.create.foundation.data;

import com.simibubi.create.foundation.data.VariantModels;

import com.simibubi.create.foundation.data.MultipartModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.chassis.LinearChassisBlock;
import com.simibubi.create.content.contraptions.chassis.RadialChassisBlock;
import com.simibubi.create.content.contraptions.mounted.CartAssembleRailType;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.simibubi.create.content.decoration.steamWhistle.WhistleExtenderBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.math.Pointing;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import com.simibubi.create.foundation.data.VariantModels.ConfiguredModel;


public class BlockStateGen {

	// Functions

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> axisBlockProvider(
		boolean customItem) {
		return (c, p) -> axisBlock(c, p, getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> directionalBlockProvider(
		boolean customItem) {
		return (c, p) -> VariantModels.directionalBlock(p, c.get(), getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> directionalBlockProviderIgnoresWaterlogged(
		boolean customItem) {
		return (c, p) -> directionalBlockIgnoresWaterlogged(c, p, getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> horizontalBlockProvider(
		boolean customItem) {
		return (c, p) -> VariantModels.horizontalBlock(p, c.get(), getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> horizontalAxisBlockProvider(
		boolean customItem) {
		return (c, p) -> horizontalAxisBlock(c, p, getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> simpleCubeAll(
		String path) {
		return (c, p) -> VariantModels.simpleBlock(p, c.get(), VariantModels.models(p)
			.cubeAll(c.getName(), p.modLoc("block/" + path)));
	}

	public static <T extends DirectionalAxisKineticBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> directionalAxisBlockProvider() {
		return (c, p) -> directionalAxisBlock(c, p, ($, vertical) -> VariantModels.models(p)
			.getExistingFile(p.modLoc("block/" + c.getName() + "/" + (vertical ? "vertical" : "horizontal"))));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> horizontalWheelProvider(
		boolean customItem) {
		return (c, p) -> horizontalWheel(c, p, getBlockModel(customItem, c, p));
	}

	// Utility

	private static <T extends Block> Function<BlockState, Identifier> getBlockModel(boolean customItem,
		DataGenContext<Block, T> c, RegistrateBlockModelGenerator p) {
		return $ -> customItem ? AssetLookup.partialBaseModel(c, p) : AssetLookup.standardModel(c, p);
	}

	// Generators

	public static <T extends Block> void directionalBlockIgnoresWaterlogged(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov, Function<BlockState, Identifier> modelFunc) {
		VariantModels.forAllStatesExcept(prov, ctx.getEntry(), state -> {
				Direction dir = state.getValue(BlockStateProperties.FACING);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.rotationX(dir == Direction.DOWN ? 180
						: dir.getAxis()
							.isHorizontal() ? 90 : 0)
					.rotationY(dir.getAxis()
						.isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
					.build();
			}, BlockStateProperties.WATERLOGGED);
	}

	public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		Function<BlockState, Identifier> modelFunc) {
		axisBlock(ctx, prov, modelFunc, false);
	}

	public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		Function<BlockState, Identifier> modelFunc, boolean uvLock) {
		VariantModels.forAllStatesExcept(prov, ctx.getEntry(), state -> {
				Axis axis = state.getValue(BlockStateProperties.AXIS);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.uvLock(uvLock)
					.rotationX(axis == Axis.Y ? 0 : 90)
					.rotationY(axis == Axis.X ? 90 : axis == Axis.Z ? 180 : 0)
					.build();
			}, BlockStateProperties.WATERLOGGED);
	}

	public static <T extends Block> void simpleBlock(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		Function<BlockState, Identifier> modelFunc) {
		VariantModels.forAllStatesExcept(prov, ctx.getEntry(), state -> {
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.build();
			}, BlockStateProperties.WATERLOGGED);
	}

	public static <T extends Block> void horizontalAxisBlock(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov, Function<BlockState, Identifier> modelFunc) {
		VariantModels.forAllStates(prov, ctx.getEntry(), state -> {
				Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.rotationY(axis == Axis.X ? 90 : 0)
					.build();
			});
	}

	public static <T extends DirectionalAxisKineticBlock> void directionalAxisBlock(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov, BiFunction<BlockState, Boolean, Identifier> modelFunc) {
		VariantModels.forAllStates(prov, ctx.getEntry(), state -> {

				boolean alongFirst = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
				Direction direction = state.getValue(DirectionalAxisKineticBlock.FACING);
				boolean vertical = direction.getAxis()
					.isHorizontal() && (direction.getAxis() == Axis.X) == alongFirst;
				int xRot = direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0;
				int yRot = direction.getAxis()
					.isVertical() ? alongFirst ? 0 : 90 : (int) direction.toYRot();

				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state, vertical))
					.rotationX(xRot)
					.rotationY(yRot)
					.build();
			});
	}

	public static <T extends Block> void horizontalWheel(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov, Function<BlockState, Identifier> modelFunc) {
		VariantModels.forAllStates(prov, ctx.get(), state -> ConfiguredModel.builder()
				.modelFile(modelFunc.apply(state))
				.rotationX(90)
				.rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING)
					.toYRot() + 180) % 360)
				.build());
	}

	public static <T extends Block> void cubeAll(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		String textureSubDir) {
		cubeAll(ctx, prov, textureSubDir, ctx.getName());
	}

	public static <T extends Block> void cubeAll(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		String textureSubDir, String name) {
		String texturePath = "block/" + textureSubDir + name;
		VariantModels.simpleBlock(prov, ctx.get(), VariantModels.models(prov)
			.cubeAll(ctx.getName(), prov.modLoc(texturePath)));
	}

	public static NonNullBiConsumer<DataGenContext<Block, CartAssemblerBlock>, RegistrateBlockModelGenerator> cartAssembler() {
		return (c, p) -> VariantModels.forAllStates(p, c.get(), state -> {
				CartAssembleRailType type = state.getValue(CartAssemblerBlock.RAIL_TYPE);
				Boolean powered = state.getValue(CartAssemblerBlock.POWERED);
				Boolean backwards = state.getValue(CartAssemblerBlock.BACKWARDS);
				RailShape shape = state.getValue(CartAssemblerBlock.RAIL_SHAPE);

				int yRotation = shape == RailShape.EAST_WEST ? 270 : 0;
				if (backwards)
					yRotation += 180;

				return ConfiguredModel.builder()
					.modelFile(VariantModels.models(p)
						.getExistingFile(p.modLoc("block/" + c.getName() + "/block_" + type.getSerializedName()
							+ (powered ? "_powered" : ""))))
					.rotationY(yRotation % 360)
					.build();
			});
	}

	public static NonNullBiConsumer<DataGenContext<Block, BlazeBurnerBlock>, RegistrateBlockModelGenerator> blazeHeater() {
		return (c, p) -> ConfiguredModel.builder()
			.modelFile(VariantModels.models(p)
				.getExistingFile(p.modLoc("block/" + c.getName() + "/block")))
			.build();
	}

	public static <B extends LinearChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> linearChassis() {
		return (c, p) -> {
			Identifier side = p.modLoc("block/" + c.getName() + "_side");
			Identifier top = p.modLoc("block/linear_chassis_end");
			Identifier top_sticky = p.modLoc("block/linear_chassis_end_sticky");

			List<Identifier> models = new ArrayList<>(4);
			for (boolean isTopSticky : Iterate.trueAndFalse)
				for (boolean isBottomSticky : Iterate.trueAndFalse)
					models.add(VariantModels.models(p)
						.withExistingParent(
							c.getName() + (isTopSticky ? "_top" : "") + (isBottomSticky ? "_bottom" : ""),
							"block/cube_bottom_top")
						.texture("side", side)
						.texture("bottom", isBottomSticky ? top_sticky : top)
						.texture("top", isTopSticky ? top_sticky : top));
			BiFunction<Boolean, Boolean, Identifier> modelFunc = (t, b) -> models.get((t ? 0 : 2) + (b ? 0 : 1));

			axisBlock(c, p, state -> modelFunc.apply(state.getValue(LinearChassisBlock.STICKY_TOP),
				state.getValue(LinearChassisBlock.STICKY_BOTTOM)));
		};
	}

	public static <B extends RadialChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> radialChassis() {
		return (c, p) -> {
			String path = "block/" + c.getName();
			Identifier side = p.modLoc(path + "_side");
			Identifier side_sticky = p.modLoc(path + "_side_sticky");

			String templateModelPath = "block/radial_chassis";
			Identifier base = VariantModels.models(p)
				.getExistingFile(p.modLoc(templateModelPath + "/base"));
			List<Identifier> faces = new ArrayList<>(3);
			List<Identifier> stickyFaces = new ArrayList<>(3);

			for (Axis axis : Iterate.axes) {
				String suffix = "side_" + axis.getSerializedName();
				faces.add(VariantModels.models(p)
					.withExistingParent("block/" + c.getName() + "_" + suffix,
						p.modLoc(templateModelPath + "/" + suffix))
					.texture("side", side)).build();
			}
			for (Axis axis : Iterate.axes) {
				String suffix = "side_" + axis.getSerializedName();
				stickyFaces.add(VariantModels.models(p)
					.withExistingParent("block/" + c.getName() + "_" + suffix + "_sticky",
						p.modLoc(templateModelPath + "/" + suffix))
					.texture("side", side_sticky)).build();
			}

			MultipartModels.Builder builder = MultipartModels.getMultipartBuilder(p, c.get());
			BlockState propertyGetter = c.get()
				.defaultBlockState()
				.setValue(RadialChassisBlock.AXIS, Axis.Y);

			for (Axis axis : Iterate.axes)
				builder.part()
					.modelFile(base)
					.rotationX(axis != Axis.Y ? 90 : 0)
					.rotationY(axis != Axis.X ? 0 : 90)
					.addModel()
					.condition(RadialChassisBlock.AXIS, axis)
					.end();

			for (Direction face : Iterate.horizontalDirections) {
				for (boolean sticky : Iterate.trueAndFalse) {
					for (Axis axis : Iterate.axes) {
						int horizontalAngle = (int) (face.toYRot());
						int index = axis.ordinal();
						int xRot = 0;
						int yRot = 0;

						if (axis == Axis.X)
							xRot = -horizontalAngle + 180;
						if (axis == Axis.Y)
							yRot = horizontalAngle;
						if (axis == Axis.Z) {
							yRot = -horizontalAngle + 270;

							// blockstates can't have zRot, so here we are
							if (face.getAxis() == Axis.Z) {
								index = 0;
								xRot = horizontalAngle + 180;
								yRot = 90;
							}
						}

						builder.part()
							.modelFile((sticky ? stickyFaces : faces).get(index))
							.rotationX((xRot + 360) % 360)
							.rotationY((yRot + 360) % 360)
							.addModel()
							.condition(RadialChassisBlock.AXIS, axis)
							.condition(c.get()
								.getGlueableSide(propertyGetter, face), sticky)
							.end();
					}
				}
			}
		};
	}

	public static <P extends Block> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> naturalStoneTypeBlock(
		String type) {
		return (c, p) -> {
			ConfiguredModel[] variants = new ConfiguredModel[4];
			for (int i = 0; i < variants.length; i++)
				variants[i] = ConfiguredModel.builder()
					.modelFile(VariantModels.models(p)
						.cubeAll(type + "_natural_" + i, p.modLoc("block/palettes/stone_types/natural/" + type + "_" + i)))
					.buildLast();
			p.getVariantBuilder(c.get())
				.partialState()
				.setModels(variants);
		};
	}

	public static <P extends EncasedPipeBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> encasedPipe() {
		return (c, p) -> {
			Identifier open = AssetLookup.partialBaseModel(c, p, "open");
			Identifier flat = AssetLookup.partialBaseModel(c, p, "flat");
			MultipartModels.Builder builder = MultipartModels.getMultipartBuilder(p, c.get());
			for (boolean flatPass : Iterate.trueAndFalse)
				for (Direction d : Iterate.directions) {
					int verticalAngle = d == Direction.UP ? 90 : d == Direction.DOWN ? -90 : 0;
					builder.part()
						.modelFile(flatPass ? flat : open)
						.rotationX(verticalAngle)
						.rotationY((int) (d.toYRot() + (d.getAxis()
							.isVertical() ? 90 : 0)) % 360)
						.addModel()
						.condition(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(d), !flatPass)
						.end();
				}
		};
	}

	public static <P extends TrapDoorBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> uvLockedTrapdoorBlock(
		P block, Identifier bottom, Identifier top, Identifier open) {
		return (c, p) -> {
			VariantModels.forAllStatesExcept(p, block, state -> {
					int xRot = 0;
					int yRot = ((int) state.getValue(TrapDoorBlock.FACING)
						.toYRot()) + 180;
					boolean isOpen = state.getValue(TrapDoorBlock.OPEN);
					if (!isOpen)
						yRot = 0;
					yRot %= 360;
					return ConfiguredModel.builder()
						.modelFile(isOpen ? open : state.getValue(TrapDoorBlock.HALF) == Half.TOP ? top : bottom)
						.rotationX(xRot)
						.rotationY(yRot)
						.uvLock(!isOpen)
						.build();
				}, TrapDoorBlock.POWERED, TrapDoorBlock.WATERLOGGED);
		};
	}

	public static <P extends WhistleExtenderBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> whistleExtender() {
		return (c, p) -> {
			ModelGenShim models = VariantModels.models(p);
			String basePath = "block/steam_whistle/extension/";
			MultipartModels.Builder builder = MultipartModels.getMultipartBuilder(p, c.get());

			for (WhistleSize size : WhistleSize.values()) {
				String basePathSize = basePath + size.getSerializedName() + "_";
				ExistingModelFile topRim = models.getExistingFile(Create.asResource(basePathSize + "top_rim"));
				ExistingModelFile single = models.getExistingFile(Create.asResource(basePathSize + "single"));
				ExistingModelFile double_ = models.getExistingFile(Create.asResource(basePathSize + "double"));

				builder.part()
					.modelFile(topRim)
					.addModel()
					.condition(WhistleExtenderBlock.SIZE, size)
					.condition(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.DOUBLE)
					.end()
					.part()
					.modelFile(single)
					.addModel()
					.condition(WhistleExtenderBlock.SIZE, size)
					.condition(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.SINGLE)
					.end()
					.part()
					.modelFile(double_)
					.addModel()
					.condition(WhistleExtenderBlock.SIZE, size)
					.condition(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.DOUBLE,
						WhistleExtenderShape.DOUBLE_CONNECTED)
					.end();
			}
		};
	}

	public static <P extends FluidPipeBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> pipe() {
		return (c, p) -> {
			String path = "block/" + c.getName();

			String LU = "lu";
			String RU = "ru";
			String LD = "ld";
			String RD = "rd";
			String LR = "lr";
			String UD = "ud";
			String U = "u";
			String D = "d";
			String L = "l";
			String R = "r";

			List<String> orientations = ImmutableList.of(LU, RU, LD, RD, LR, UD, U, D, L, R);
			Map<String, Pair<Integer, Integer>> uvs = ImmutableMap.<String, Pair<Integer, Integer>>builder()
				.put(LU, Pair.of(12, 4))
				.put(RU, Pair.of(8, 4))
				.put(LD, Pair.of(12, 0))
				.put(RD, Pair.of(8, 0))
				.put(LR, Pair.of(4, 8))
				.put(UD, Pair.of(0, 8))
				.put(U, Pair.of(4, 4))
				.put(D, Pair.of(0, 0))
				.put(L, Pair.of(4, 0))
				.put(R, Pair.of(0, 4))
				.build();

			Map<Axis, Identifier> coreTemplates = new IdentityHashMap<>();
			Map<Pair<String, Axis>, Identifier> coreModels = new HashMap<>();

			for (Axis axis : Iterate.axes)
				coreTemplates.put(axis, p.modLoc(path + "/core_" + axis.getSerializedName()));

			for (Axis axis : Iterate.axes) {
				Identifier parent = coreTemplates.get(axis);
				for (String s : orientations) {
					Pair<String, Axis> key = Pair.of(s, axis);
					String modelName = path + "/" + s + "_" + axis.getSerializedName();
					coreModels.put(key, VariantModels.models(p)
						.withExistingParent(modelName, parent)
						.element()
						.from(4, 4, 4)
						.to(12, 12, 12)
						.face(Direction.get(AxisDirection.POSITIVE, axis))
						.end()
						.face(Direction.get(AxisDirection.NEGATIVE, axis))
						.end()
						.faces((d, builder) -> {
							Pair<Integer, Integer> pair = uvs.get(s);
							float u = pair.getKey();
							float v = pair.getValue();
							if (d == Direction.UP)
								builder.uvs(u + 4, v + 4, u, v);
							if (d == Direction.DOWN)
								builder.uvs(u + 4, v, u, v + 4);
							if (d == Direction.NORTH)
								builder.uvs(u, v, u + 4, v + 4);
							if (d == Direction.SOUTH)
								builder.uvs(u + 4, v, u, v + 4);
							if (d == Direction.EAST)
								builder.uvs(u, v, u + 4, v + 4);
							if (d == Direction.WEST)
								builder.uvs(u + 4, v, u, v + 4);
							builder.texture("#0");
						})
						.end());
				}
			}

			MultipartModels.Builder builder = MultipartModels.getMultipartBuilder(p, c.get());
			for (Axis axis : Iterate.axes) {
				putPart(coreModels, builder, axis, LU, true, false, true, false);
				putPart(coreModels, builder, axis, RU, true, false, false, true);
				putPart(coreModels, builder, axis, LD, false, true, true, false);
				putPart(coreModels, builder, axis, RD, false, true, false, true);
				putPart(coreModels, builder, axis, UD, true, true, false, false);
				putPart(coreModels, builder, axis, U, true, false, false, false);
				putPart(coreModels, builder, axis, D, false, true, false, false);
				putPart(coreModels, builder, axis, LR, false, false, true, true);
				putPart(coreModels, builder, axis, L, false, false, true, false);
				putPart(coreModels, builder, axis, R, false, false, false, true);
			}
		};
	}

	private static void putPart(Map<Pair<String, Axis>, Identifier> coreModels, MultipartModels.Builder builder,
		Axis axis, String s, boolean up, boolean down, boolean left, boolean right) {
		Direction positiveAxis = Direction.get(AxisDirection.POSITIVE, axis);
		Map<Direction, BooleanProperty> propertyMap = FluidPipeBlock.PROPERTY_BY_DIRECTION;

		Direction upD = Pointing.UP.getCombinedDirection(positiveAxis);
		Direction leftD = Pointing.LEFT.getCombinedDirection(positiveAxis);
		Direction rightD = Pointing.RIGHT.getCombinedDirection(positiveAxis);
		Direction downD = Pointing.DOWN.getCombinedDirection(positiveAxis);

		if (axis == Axis.Y || axis == Axis.X) {
			leftD = leftD.getOpposite();
			rightD = rightD.getOpposite();
		}

		builder.part()
			.modelFile(coreModels.get(Pair.of(s, axis)))
			.addModel()
			.condition(propertyMap.get(upD), up)
			.condition(propertyMap.get(leftD), left)
			.condition(propertyMap.get(rightD), right)
			.condition(propertyMap.get(downD), down)
			.end();
	}

	public static Function<BlockState, ConfiguredModel[]> mapToAir(RegistrateBlockModelGenerator p) {
		return state -> ConfiguredModel.builder()
			.modelFile(VariantModels.models(p)
				.getExistingFile(p.mcLoc("block/air")))
			.build();
	}

}
