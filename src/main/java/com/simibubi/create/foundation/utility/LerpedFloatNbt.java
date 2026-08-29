package com.simibubi.create.foundation.utility;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.minecraft.nbt.CompoundTag;

/// catnip's LerpedFloat dropped its NBT methods upstream. Create stores several
/// of them in block entity tags, and only the current value and chase target
/// need to survive a save -- the interpolator and chase function are set up in
/// the constructor either way.
public class LerpedFloatNbt {
	public static CompoundTag write(LerpedFloat lerped) {
		CompoundTag tag = new CompoundTag();
		tag.putFloat("Value", lerped.getValue());
		tag.putFloat("Target", lerped.getChaseTarget());
		return tag;
	}

	public static void read(LerpedFloat lerped, CompoundTag tag) {
		lerped.startWithValue(tag.getFloatOr("Value", 0));
		lerped.chase(tag.getFloatOr("Target", 0), 0, LerpedFloat.Chaser.EXP);
	}
}
