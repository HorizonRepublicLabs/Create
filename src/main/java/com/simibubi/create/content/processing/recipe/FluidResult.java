package com.simibubi.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/// A fluid a recipe produces. Recipes are read before fluid components bind, and a
/// FluidStack cannot be built until they are, so the recipe keeps what it was written
/// with and builds the stack when a machine asks for it.
public record FluidResult(Holder<Fluid> fluid, int amount, DataComponentPatch components) {
	public static final Codec<FluidResult> CODEC = RecordCodecBuilder.create(i -> i.group(
		BuiltInRegistries.FLUID.holderByNameCodec()
			.fieldOf("id")
			.forGetter(FluidResult::fluid),
		ExtraCodecs.POSITIVE_INT.fieldOf("amount")
			.forGetter(FluidResult::amount),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
			.forGetter(FluidResult::components)
	).apply(i, FluidResult::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidResult> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.holderRegistry(net.minecraft.core.registries.Registries.FLUID), FluidResult::fluid,
		ByteBufCodecs.VAR_INT, FluidResult::amount,
		DataComponentPatch.STREAM_CODEC, FluidResult::components,
		FluidResult::new);

	public static FluidResult of(FluidStack stack) {
		return new FluidResult(stack.typeHolder(), stack.getAmount(), stack.getComponentsPatch());
	}

	public FluidStack toStack() {
		return new FluidStack(fluid, amount, components);
	}
}
