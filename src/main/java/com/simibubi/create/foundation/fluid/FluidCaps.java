package com.simibubi.create.foundation.fluid;

import com.simibubi.create.foundation.fluid.FluidCaps;

import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import net.neoforged.neoforge.transfer.access.ItemAccess;

import net.neoforged.neoforge.transfer.fluid.FluidResource;

import net.neoforged.neoforge.transfer.ResourceHandler;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/// The fluid capability hands back a ResourceHandler now. Create's plumbing is
/// written against IFluidHandler, and NeoForge supplies IFluidHandler.of for
/// the migration, so the conversion lives here. Mirrors ItemCaps.
public class FluidCaps {
	@Nullable
	public static IFluidHandler at(Level level, BlockPos pos, @Nullable Direction side) {
		var handler = FluidCaps.at(level, pos, side);
		return handler == null ? null : IFluidHandler.of(handler);
	}

	@Nullable
	public static IFluidHandler at(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be,
		@Nullable Direction side) {
		var handler = FluidCaps.at(level, pos, state, be, side);
		return handler == null ? null : IFluidHandler.of(handler);
	}

	@Nullable
	public static IFluidHandler of(Entity entity, @Nullable Direction side) {
		var handler = FluidCaps.of(entity, side);
		return handler == null ? null : IFluidHandler.of(handler);
	}

	@Nullable
	/// An item's own tank is reached through an access to the stack now, rather
	/// than the stack answering directly.
	public static IFluidHandler of(ItemStack stack) {
		var handler = ItemAccess.forStack(stack)
			.getCapability(Capabilities.Fluid.ITEM);
		return handler == null ? null : IFluidHandler.of(handler);
	}

	/// The other direction: Create's tanks are fluid handlers, and the
	/// capability wants a resource handler.
	@Nullable
	public static ResourceHandler<FluidResource> asResourceHandler(@Nullable IFluidHandler handler) {
		return FluidHandlerResourceAdapter.of(handler);
	}

	/// Wraps a provider that still hands out fluid handlers so it can be
	/// registered against the fluid capability.
	public static <O, C> ICapabilityProvider<O, C, ResourceHandler<FluidResource>> fluids(
		ICapabilityProvider<O, C, IFluidHandler> provider) {
		return (object, context) -> asResourceHandler(provider.getCapability(object, context));
	}
}
