package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.item.ItemHelper;

import net.neoforged.neoforge.common.crafting.CompoundIngredient;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.stream.Stream;

public final class ToolboxColoringRecipeMaker {

	// From JEI's ShulkerBoxColoringRecipeMaker
	public static Stream<RecipeHolder<CraftingRecipe>> createRecipes() {
		String group = "create.toolbox.color";
		ItemStack baseShulkerStack = AllBlocks.TOOLBOXES.get(DyeColor.BROWN)
			.asStack();
		Ingredient baseShulkerIngredient = Ingredient.of(baseShulkerStack);

		return Arrays.stream(DyeColor.values())
			.filter(dc -> dc != DyeColor.BROWN)
			.map(color -> {
				DyeItem dye = DyeItem.byColor(color);
				ItemStack dyeStack = new ItemStack(dye);
				TagKey<Item> colorTag = color.getTag();
				// ingredients are holder sets now; a compound keeps the tag intact
				// rather than flattening it into the recipe
				Ingredient colorIngredient = CompoundIngredient.of(Ingredient.of(dye),
					ItemHelper.ingredientOf(colorTag));
				NonNullList<Ingredient> inputs =
					NonNullList.copyOf(List.of(baseShulkerIngredient, colorIngredient));
				Block coloredShulkerBox = AllBlocks.TOOLBOXES.get(color)
					.get();
				ItemStack output = new ItemStack(coloredShulkerBox);
				ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
				return new RecipeHolder<>(Create.asResource(group + "/" + color), recipe);
			});
	}

	private ToolboxColoringRecipeMaker() {}

}
