package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueOutput;

/// Counterpart to TagValueInputAccessor; see ValueIOShim.
@Mixin(TagValueOutput.class)
public interface TagValueOutputAccessor {
	@Accessor("output")
	CompoundTag getOutput();
}
