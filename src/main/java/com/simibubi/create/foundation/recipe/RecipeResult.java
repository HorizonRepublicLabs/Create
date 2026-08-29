package com.simibubi.create.foundation.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/// Recipe.getResultItem went away in 26.x; a recipe describes its output
/// through display entries, and only assemble() produces a stack, which needs
/// an input Create does not always have. Create's own recipes still expose a
/// result directly, so this reaches it where one exists.
public class RecipeResult {
	public static ItemStack of(Recipe<?> recipe, HolderLookup.Provider registries) {
		if (recipe instanceof ProcessingRecipe<?, ?> processing)
			return processing.getResultItem(registries);
		if (recipe instanceof SequencedAssemblyRecipe sequenced)
			return sequenced.getResultItem(registries);
		return ItemStack.EMPTY;
	}

	public static ItemStack of(RecipeHolder<?> holder, HolderLookup.Provider registries) {
		return of(holder.value(), registries);
	}
}
