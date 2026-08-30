package com.simibubi.create.foundation.data;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;

/**
 * For registering fluids with no buckets/blocks
 */
public class VirtualFluidBuilder<T extends BaseFlowingFluid, P> extends FluidBuilder<T, P> {

	/// Textures no longer travel with the builder; they are registered against
	/// the fluid itself.
	public VirtualFluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<Properties, T> sourceFactory,
		NonNullFunction<Properties, T> flowingFactory) {
		super(owner, parent, name, callback, typeFactory, flowingFactory::apply);
		source(sourceFactory::apply);
	}

	@Override
	public NonNullSupplier<T> asSupplier() {
		return this::getEntry;
	}

}
