package com.simibubi.create.content.kinetics.crafter;

import net.minecraft.world.item.crafting.CraftingRecipe;

import net.minecraft.world.item.ItemStackTemplate;

import java.util.Optional;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;


import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class MechanicalCraftingRecipe extends ShapedRecipe {
	private final boolean acceptMirrored;

	/// The result is kept alongside the one the shaped recipe holds privately,
	/// so the codec has something to read back.
	private final ItemStackTemplate result;

	public MechanicalCraftingRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
		ShapedRecipePattern pattern, ItemStackTemplate result, boolean acceptMirrored) {
		super(commonInfo, bookInfo, pattern, result);
		this.acceptMirrored = acceptMirrored;
		this.result = result;
	}

	@Override
	public boolean matches(CraftingInput input, Level worldIn) {
		if (!(input instanceof MechanicalCraftingInput))
			return false;
		if (acceptsMirrored())
			return super.matches(input, worldIn);

		// From ShapedRecipe except the symmetry
		for (int i = 0; i <= input.width() - this.getWidth(); ++i)
			for (int j = 0; j <= input.height() - this.getHeight(); ++j)
				if (this.matchesSpecific(input, i, j))
					return true;
		return false;
	}

	// From ShapedRecipe
	private boolean matchesSpecific(CraftingInput input, int p_77573_2_, int p_77573_3_) {
		List<Optional<Ingredient>> ingredients = getIngredients();
		int width = getWidth();
		int height = getHeight();
		for (int i = 0; i < input.width(); ++i) {
			for (int j = 0; j < input.height(); ++j) {
				int k = i - p_77573_2_;
				int l = j - p_77573_3_;
				// there is no empty ingredient any more; a null cell matches only
				// an empty stack, which is what Ingredient.EMPTY did
				Optional<Ingredient> ingredient = Optional.empty();
				if (k >= 0 && l >= 0 && k < width && l < height)
					ingredient = ingredients.get(k + l * width);
				ItemStack atCell = input.getItem(i + j * input.width());
				if (ingredient.isEmpty() ? !atCell.isEmpty() : !ingredient.get()
					.test(atCell))
					return false;
			}
		}
		return true;
	}

	// CraftingRecipe fixes the type to its own; the mechanical crafter still
	// keeps a type of its own so only its recipes are looked up.
	@Override
	@SuppressWarnings("unchecked")
	public RecipeType<CraftingRecipe> getType() {
		return (RecipeType<CraftingRecipe>) (RecipeType<?>) AllRecipeTypes.MECHANICAL_CRAFTING.getType();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public @NotNull RecipeSerializer<ShapedRecipe> getSerializer() {
		return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) AllRecipeTypes.MECHANICAL_CRAFTING.getSerializer();
	}

	public boolean acceptsMirrored() {
		return acceptMirrored;
	}

	/// A shaped recipe is four pieces rather than one object now, so the codec
	/// spells them out alongside the mirroring flag.
	public static class Serializer {
		public static final MapCodec<MechanicalCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
			CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
			ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
			Codec.BOOL.fieldOf("accept_mirrored").forGetter(MechanicalCraftingRecipe::acceptsMirrored)
		).apply(instance, MechanicalCraftingRecipe::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, MechanicalCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
			CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
			ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
			ItemStackTemplate.STREAM_CODEC, r -> r.result,
			ByteBufCodecs.BOOL, r -> r.acceptMirrored,
			MechanicalCraftingRecipe::new
		);


		public static RecipeSerializer<MechanicalCraftingRecipe> create() {
			return new RecipeSerializer<>(CODEC, STREAM_CODEC);
		}
	}
}
