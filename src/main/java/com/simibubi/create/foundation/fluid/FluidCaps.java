package com.simibubi.create.foundation.fluid;

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
	public static IFluidHandler of(ItemStack stack) {
		var handler = FluidCaps.of(stack);
		return handler == null ? null : IFluidHandler.of(handler);
	}
}
