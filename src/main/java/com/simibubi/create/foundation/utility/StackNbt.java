package com.simibubi.create.foundation.utility;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/// ItemStack.saveOptional and parseOptional went away in 26.x in favour of
/// ItemStack.OPTIONAL_CODEC. Create reads and writes stacks against raw
/// CompoundTags in a lot of places, so these keep that shape rather than
/// spreading codec plumbing across every call site.
public class StackNbt {

	public static Tag save(HolderLookup.Provider registries, ItemStack stack) {
		if (stack.isEmpty())
			return new CompoundTag();
		return ItemStack.OPTIONAL_CODEC.encodeStart(ops(registries), stack)
			.result()
			.orElseGet(CompoundTag::new);
	}

	public static ItemStack parse(HolderLookup.Provider registries, Tag tag) {
		if (tag == null)
			return ItemStack.EMPTY;
		return ItemStack.OPTIONAL_CODEC.parse(ops(registries), tag)
			.result()
			.orElse(ItemStack.EMPTY);
	}

	public static Tag saveFluid(HolderLookup.Provider registries, FluidStack stack) {
		if (stack.isEmpty())
			return new CompoundTag();
		return FluidStack.OPTIONAL_CODEC.encodeStart(ops(registries), stack)
			.result()
			.orElseGet(CompoundTag::new);
	}

	public static FluidStack parseFluid(HolderLookup.Provider registries, Tag tag) {
		if (tag == null)
			return FluidStack.EMPTY;
		return FluidStack.OPTIONAL_CODEC.parse(ops(registries), tag)
			.result()
			.orElse(FluidStack.EMPTY);
	}

	private static RegistryOps<Tag> ops(HolderLookup.Provider registries) {
		return registries.createSerializationContext(NbtOps.INSTANCE);
	}

	private StackNbt() {}
}
