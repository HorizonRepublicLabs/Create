package com.simibubi.create.foundation.utility;

import com.simibubi.create.foundation.mixin.accessor.TagValueInputAccessor;
import com.simibubi.create.foundation.mixin.accessor.TagValueOutputAccessor;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/// 26.x hands block entities and entities a ValueInput/ValueOutput where they
/// used to get a CompoundTag. Create's serialization is CompoundTag-shaped from
/// the block entities down through every behaviour, so rather than rewrite all
/// of it these convert at the boundary.
public class ValueIOShim {
	/// The tag a ValueInput reads from. Empty for inputs Create did not create,
	/// which only happens for non-tag-backed implementations.
	public static CompoundTag tagOf(ValueInput input) {
		if (input instanceof TagValueInputAccessor accessor)
			return accessor.getInput();
		return new CompoundTag();
	}

	/// The tag a ValueOutput writes into, so callers can merge into it directly.
	public static CompoundTag tagOf(ValueOutput output) {
		if (output instanceof TagValueOutputAccessor accessor)
			return accessor.getOutput();
		return new CompoundTag();
	}

	public static ValueInput inputOf(CompoundTag tag, HolderLookup.Provider registries) {
		return TagValueInput.create(ProblemReporter.DISCARDING, registries, tag);
	}

	public static TagValueOutput output(HolderLookup.Provider registries) {
		return TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
	}
}
