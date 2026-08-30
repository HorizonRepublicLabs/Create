package com.simibubi.create.foundation.recipe;

import net.minecraft.resources.Identifier;

import java.util.List;

import java.util.Collection;

import java.util.Optional;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/// Level.recipeAccess no longer offers recipe lookup; the RecipeManager lives
/// on the server. Create asks for recipes from both sides, so this returns
/// nothing on a client that has no integrated server rather than throwing.
public class RecipeLookup {
	public static Optional<RecipeManager> manager(Level level) {
		MinecraftServer server = level.getServer();
		if (server == null)
			server = ServerLifecycleHooks.getCurrentServer();
		return Optional.ofNullable(server)
			.map(MinecraftServer::getRecipeManager);
	}

	/// The client no longer holds the full recipe set -- it only receives
	/// displays -- so listing recipes goes through the server as well.
	public static Collection<RecipeHolder<?>> allRecipes(Level level) {
		return manager(level).map(RecipeManager::getRecipes)
			.orElse(List.of());
	}

	/// getAllRecipesFor is gone too, so the type filter happens here.
	@SuppressWarnings("unchecked")
	public static <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> allOfType(Level level,
		RecipeType<T> type) {
		return allRecipes(level).stream()
			.filter(holder -> holder.value()
				.getType() == type)
			.map(holder -> (RecipeHolder<T>) holder)
			.toList();
	}

	/// RecipeAccess no longer looks recipes up by id, so this walks what the
	/// client was sent instead.
	public static Optional<RecipeHolder<?>> byId(Level level, Identifier id) {
		return allRecipes(level).stream()
			.filter(holder -> holder.id()
				.identifier()
				.equals(id))
			.findFirst();
	}

	public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> find(Level level,
		RecipeType<T> type, I input) {
		return manager(level).flatMap(manager -> manager.getRecipeFor(type, input, level));
	}
}
