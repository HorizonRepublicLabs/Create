package com.simibubi.create.foundation.utility;

import com.simibubi.create.foundation.mixin.accessor.TagValueInputAccessor;
import com.simibubi.create.foundation.mixin.accessor.TagValueOutputAccessor;

import com.mojang.serialization.Codec;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
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

	/// NeoForge's INBTSerializable became ValueIOSerializable, which no longer
	/// speaks CompoundTag. Create stores these inside its own tags, so round
	/// trip through the tag-backed implementations.
	public static CompoundTag save(ValueIOSerializable serializable, HolderLookup.Provider registries) {
		TagValueOutput out = output(registries);
		serializable.serialize(out);
		return out.buildResult();
	}

	public static void load(ValueIOSerializable serializable, HolderLookup.Provider registries, CompoundTag tag) {
		serializable.deserialize(inputOf(tag, registries));
	}

	/// Entity lost its INBTSerializable pair too. saveWithoutId matches what the
	/// old serializeNBT wrote, since Create always loads these back onto an
	/// entity it already has.
	public static CompoundTag saveEntity(Entity entity, HolderLookup.Provider registries) {
		TagValueOutput out = output(registries);
		entity.saveWithoutId(out);
		return out.buildResult();
	}

	public static void loadEntity(Entity entity, HolderLookup.Provider registries, CompoundTag tag) {
		entity.load(inputOf(tag, registries));
	}

	/// Encodes a value through its codec into a CompoundTag, for the catnip
	/// types that swapped hand-written NBT for codecs.
	public static <T> CompoundTag encode(Codec<T> codec, T value, HolderLookup.Provider registries) {
		return (CompoundTag) codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value)
			.getOrThrow();
	}

	public static <T> T decode(Codec<T> codec, CompoundTag tag, HolderLookup.Provider registries) {
		return codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
			.getOrThrow();
	}
}
