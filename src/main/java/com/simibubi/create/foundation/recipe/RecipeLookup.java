package com.simibubi.create.foundation.recipe;

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

	public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> find(Level level,
		RecipeType<T> type, I input) {
		return manager(level).flatMap(manager -> manager.getRecipeFor(type, input, level));
	}
}
