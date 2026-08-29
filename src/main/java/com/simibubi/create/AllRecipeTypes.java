package com.simibubi.create;

import net.minecraft.world.item.crafting.CustomRecipe;

import net.minecraft.world.item.crafting.CraftingBookCategory;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.MapCodec;

import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.equipment.toolbox.ToolboxDyeingRecipe;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe;

import net.createmod.catnip.api.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public enum AllRecipeTypes implements IRecipeTypeInfo, StringRepresentable {

	CONVERSION(ConversionRecipe::new),
	CRUSHING(CrushingRecipe::new),
	CUTTING(CuttingRecipe::new),
	MILLING(MillingRecipe::new),
	BASIN(BasinRecipe::new),
	MIXING(MixingRecipe::new),
	COMPACTING(CompactingRecipe::new),
	PRESSING(PressingRecipe::new),
	SANDPAPER_POLISHING(SandPaperPolishingRecipe::new),
	SPLASHING(SplashingRecipe::new),
	HAUNTING(HauntingRecipe::new),
	DEPLOYING(DeployerApplicationRecipe::new),
	FILLING(FillingRecipe::new),
	EMPTYING(EmptyingRecipe::new),
	ITEM_APPLICATION(ManualApplicationRecipe::new),

	MECHANICAL_CRAFTING(MechanicalCraftingRecipe.Serializer::create),
	SEQUENCED_ASSEMBLY(() -> new SequencedAssemblyRecipeSerializer().asSerializer()),

	TOOLBOX_DYEING(() -> new RecipeSerializer<>(
		RecordCodecBuilder.mapCodec(i -> i.group(
			CraftingBookCategory.CODEC.fieldOf("category")
				.orElse(CraftingBookCategory.MISC)
				.forGetter(CustomRecipe::category))
			.apply(i, ToolboxDyeingRecipe::new)),
		StreamCodec.composite(CraftingBookCategory.STREAM_CODEC, CustomRecipe::category, ToolboxDyeingRecipe::new)), () -> RecipeType.CRAFTING, false),
	ITEM_COPYING(() -> new RecipeSerializer<>(
		RecordCodecBuilder.mapCodec(i -> i.group(
			CraftingBookCategory.CODEC.fieldOf("category")
				.orElse(CraftingBookCategory.MISC)
				.forGetter(CustomRecipe::category))
			.apply(i, ItemCopyingRecipe::new)),
		StreamCodec.composite(CraftingBookCategory.STREAM_CODEC, CustomRecipe::category, ItemCopyingRecipe::new)), () -> RecipeType.CRAFTING, false);

	public static final Predicate<RecipeHolder<?>> CAN_BE_AUTOMATED = r -> !r.id()
			.getPath()
			.endsWith("_manual_only");

	public final Identifier id;
	public final Supplier<RecipeSerializer<?>> serializerSupplier;
	private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializerObject;
	@Nullable
	private final DeferredHolder<RecipeType<?>, RecipeType<?>> typeObject;
	private final Supplier<RecipeType<?>> type;

	private boolean isProcessingRecipe;

	public static final Codec<AllRecipeTypes> CODEC = StringRepresentable.fromEnum(AllRecipeTypes::values);

	AllRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
		String name = Lang.asId(name());
		id = Create.asResource(name);
		this.serializerSupplier = serializerSupplier;
		serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
		if (registerType) {
			typeObject = Registers.TYPE_REGISTER.register(name, typeSupplier);
			type = typeObject;
		} else {
			typeObject = null;
			type = typeSupplier;
		}
		isProcessingRecipe = false;
	}

	AllRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
		String name = Lang.asId(name());
		id = Create.asResource(name);
		this.serializerSupplier = serializerSupplier;
		serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
		typeObject = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
		type = typeObject;
		isProcessingRecipe = false;
	}

	AllRecipeTypes(StandardProcessingRecipe.Factory<?> processingFactory) {
		this(() -> StandardProcessingRecipe.serializer(processingFactory));
		isProcessingRecipe = true;
	}

	AllRecipeTypes(ProcessingRecipe.Factory<ItemApplicationRecipeParams, ? extends ItemApplicationRecipe> itemApplicationFactory) {
		this(() -> ItemApplicationRecipe.serializer(itemApplicationFactory));
		isProcessingRecipe = true;
	}

	@Internal
	public static void register(IEventBus modEventBus) {
		ShapedRecipePattern.setCraftingSize(9, 9);
		Registers.SERIALIZER_REGISTER.register(modEventBus);
		Registers.TYPE_REGISTER.register(modEventBus);
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializerObject.get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
		return (RecipeType<R>) type.get();
	}

	/// Recipe lookup left Level in 26.x: RecipeManager lives on the server, and
	/// what a Level exposes now (recipeAccess) only covers property sets and
	/// stonecutting. Clients have no recipe map to consult, so this is empty
	/// there rather than guessing.
	public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> find(I inv, Level world) {
		if (!(world instanceof ServerLevel serverLevel))
			return Optional.empty();
		return serverLevel.recipeAccess()
			.getRecipeFor(getType(), inv, serverLevel);
	}

	public static boolean shouldIgnoreInAutomation(RecipeHolder<?> recipe) {
		RecipeSerializer<?> serializer = recipe.value().getSerializer();
		if (serializer != null && AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.matches(serializer))
			return true;
		return !CAN_BE_AUTOMATED.test(recipe);
	}

	@Override
	public @NotNull String getSerializedName() {
		return id.toString();
	}

	private static class Registers {
		private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Create.ID);
		private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, Create.ID);
	}

}
