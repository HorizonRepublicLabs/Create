package com.simibubi.create.content.processing.recipe;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

@ParametersAreNonnullByDefault
public abstract class StandardProcessingRecipe<T extends RecipeInput> extends ProcessingRecipe<T, ProcessingRecipeParams> {
	public StandardProcessingRecipe(IRecipeTypeInfo typeInfo, ProcessingRecipeParams params) {
		super(typeInfo, params);
	}

	@FunctionalInterface
	public interface Factory<R extends StandardProcessingRecipe<?>> extends ProcessingRecipe.Factory<ProcessingRecipeParams, R> {
		R create(ProcessingRecipeParams params);
	}

	public static class Builder<R extends StandardProcessingRecipe<?>>
		extends ProcessingRecipeBuilder<ProcessingRecipeParams, R, Builder<R>> {

		public Builder(Factory<R> factory, Identifier recipeId) {
			super(factory, recipeId);
		}

		@Override
		protected ProcessingRecipeParams createParams() {
			return new ProcessingRecipeParams();
		}

		@Override
		public Builder<R> self() {
			return this;
		}
	}

	/// RecipeSerializer is a record of (codec, streamCodec) in 26.x rather than
	/// an interface, so serializers are built rather than implemented.
	public static <R extends StandardProcessingRecipe<?>> RecipeSerializer<R> serializer(Factory<R> factory) {
		return new RecipeSerializer<>(ProcessingRecipe.codec(factory, ProcessingRecipeParams.CODEC),
			ProcessingRecipe.streamCodec(factory, ProcessingRecipeParams.STREAM_CODEC));
	}
}
