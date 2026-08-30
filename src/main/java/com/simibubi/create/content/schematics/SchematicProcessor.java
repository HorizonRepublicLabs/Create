package com.simibubi.create.content.schematics;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllStructureProcessorTypes;

import net.createmod.catnip.api.nbt.NBTProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SchematicProcessor implements StructureProcessor {
	public static final SchematicProcessor INSTANCE = new SchematicProcessor();
	public static final MapCodec<SchematicProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

	private SchematicProcessor() {
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos anotherPos, StructureTemplate.StructureBlockInfo rawInfo,
			StructureTemplate.StructureBlockInfo info, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
		if (info.nbt() != null && info.state().hasBlockEntity()) {
			BlockEntity be = ((EntityBlock) info.state().getBlock()).newBlockEntity(info.pos(), info.state());
			if (be != null) {
				CompoundTag nbt = NBTProcessors.process(info.state(), be, info.nbt(), false);
				if (nbt != info.nbt())
					return new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), nbt);
			}
		}
		return info;
	}

	@Nullable
	@Override
	public StructureTemplate.StructureEntityInfo processEntity(LevelReader world, BlockPos pos, StructureTemplate.StructureEntityInfo rawInfo,
			StructureTemplate.StructureEntityInfo info, StructurePlaceSettings settings, StructureTemplate template) {
		return EntityType.by(info.nbt).flatMap(type -> {
			// The restriction is a property of the type rather than a question
			// asked of an instance, so nothing has to be spawned to check it.
			if (!type.onlyOpCanSetNbt())
				return Optional.of(info);
			return Optional.empty();
		}).orElse(null);
	}

	/// The processor names its codec directly rather than a type that carries it.
	@Override
	public MapCodec<? extends StructureProcessor> codec() {
		return CODEC;
	}
}
