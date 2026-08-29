package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueInput;

/// ValueInput hides the tag it wraps, but Create's block entities and
/// behaviours are written against CompoundTag throughout. This lets
/// ValueIOShim hand that tag back so the existing read/write API survives.
@Mixin(TagValueInput.class)
public interface TagValueInputAccessor {
	@Accessor("input")
	CompoundTag getInput();
}
