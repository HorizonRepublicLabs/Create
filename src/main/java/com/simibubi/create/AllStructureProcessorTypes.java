package com.simibubi.create;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

import com.mojang.serialization.MapCodec;

import com.simibubi.create.content.schematics.SchematicProcessor;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllStructureProcessorTypes {
	private static final DeferredRegister<MapCodec<? extends StructureProcessor>> REGISTER = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Create.ID);

	/// A processor type is no longer parameterised; the processor names its own
	/// codec, and the type is just the registry entry.
	public static final DeferredHolder<MapCodec<? extends StructureProcessor>, MapCodec<SchematicProcessor>> SCHEMATIC =
		REGISTER.register("schematic", () -> SchematicProcessor.CODEC);

	@Internal
	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
