package com.simibubi.create.api.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.Create;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * A class containing some basic setup for other recipe generators to use.
 * Addons should extend this if they add a custom recipe type that is not
 * a processing recipe type and want to use Create's helpers.
 * For processing recipes extend {@link StandardProcessingRecipeGen}.
 * <p>
 * A recipe provider is built per run now, with the registries and the output
 * already in hand, so what data generation registers is the runner around it.
 * The generators themselves are unchanged: they still collect recipes up front
 * and hand them over when the run asks for them.
 */
public abstract class BaseRecipeProvider extends RecipeProvider.Runner {
	protected final String modid;
	protected final List<GeneratedRecipe> all = new ArrayList<>();

	/// Set for the length of a run, for the generators that need to look items up.
	protected HolderGetter<Item> items;

	public BaseRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries);
		this.modid = defaultNamespace;
	}

	protected Identifier asResource(String path) {
		return Identifier.fromNamespaceAndPath(modid, path);
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		items = registries.lookupOrThrow(Registries.ITEM);
		return new RecipeProvider(registries, output) {
			@Override
			protected void buildRecipes() {
				BaseRecipeProvider.this.buildRecipes(output);
			}
		};
	}

	public void buildRecipes(RecipeOutput recipeOutput) {
		all.forEach(c -> c.register(recipeOutput));
		Create.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@FunctionalInterface
	public interface GeneratedRecipe {
		void register(RecipeOutput recipeOutput);
	}
}
