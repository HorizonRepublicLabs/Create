package com.simibubi.create.foundation.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/// The item capability is Capabilities.Item now and hands back a
/// ResourceHandler rather than an IItemHandler. Create's inventory code is
/// written against IItemHandler throughout, and NeoForge supplies
/// IItemHandler.of for exactly this migration, so the conversion lives here.
public class ItemCaps {
	@Nullable
	public static IItemHandler at(Level level, BlockPos pos, @Nullable Direction side) {
		var handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
		return handler == null ? null : IItemHandler.of(handler);
	}

	@Nullable
	public static IItemHandler at(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be,
		@Nullable Direction side) {
		var handler = level.getCapability(Capabilities.Item.BLOCK, pos, state, be, side);
		return handler == null ? null : IItemHandler.of(handler);
	}

	@Nullable
	public static IItemHandler of(Entity entity, @Nullable Direction side) {
		var handler = entity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, side);
		return handler == null ? null : IItemHandler.of(handler);
	}

	@Nullable
	public static IItemHandler of(ItemStack stack) {
		var handler = stack.getCapability(Capabilities.Item.ITEM);
		return handler == null ? null : IItemHandler.of(handler);
	}
}
