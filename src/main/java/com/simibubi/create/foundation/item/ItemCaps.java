package com.simibubi.create.foundation.item;

import com.simibubi.create.foundation.item.ItemCaps;

import net.neoforged.neoforge.transfer.access.ItemAccess;

import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import net.neoforged.neoforge.transfer.item.ItemResource;

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
import net.neoforged.neoforge.items.IItemHandler;

/// The item capability is Capabilities.Item now and hands back a
/// ResourceHandler rather than an IItemHandler. Create's inventory code is
/// written against IItemHandler throughout, and NeoForge supplies
/// IItemHandler.of for exactly this migration, so the conversion lives here.
public class ItemCaps {
	@Nullable
	public static IItemHandler at(Level level, BlockPos pos, @Nullable Direction side) {
		var handler = ItemCaps.at(level, pos, side);
		return handler == null ? null : IItemHandler.of(handler);
	}

	@Nullable
	public static IItemHandler at(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be,
		@Nullable Direction side) {
		var handler = ItemCaps.at(level, pos, state, be, side);
		return handler == null ? null : IItemHandler.of(handler);
	}

	@Nullable
	public static IItemHandler of(Entity entity, @Nullable Direction side) {
		var handler = ItemCaps.of(entity, side);
		return handler == null ? null : IItemHandler.of(handler);
	}

	@Nullable
	/// An item's own inventory is reached through an access to the stack now,
	/// rather than the stack answering directly.
	public static IItemHandler of(ItemStack stack) {
		var handler = ItemAccess.forStack(stack)
			.getCapability(Capabilities.Item.ITEM);
		return handler == null ? null : IItemHandler.of(handler);
	}

	/// The other direction: Create's inventories are item handlers, and the
	/// capability wants a resource handler.
	@Nullable
	public static ResourceHandler<ItemResource> asResourceHandler(@Nullable IItemHandler handler) {
		return ItemHandlerResourceAdapter.of(handler);
	}

	/// Wraps a provider that still hands out item handlers so it can be
	/// registered against the item capability.
	public static <O, C> ICapabilityProvider<O, C, ResourceHandler<ItemResource>> items(
		ICapabilityProvider<O, C, IItemHandler> provider) {
		return (object, context) -> asResourceHandler(provider.getCapability(object, context));
	}
}
