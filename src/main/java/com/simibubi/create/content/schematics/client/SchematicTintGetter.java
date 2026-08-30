package com.simibubi.create.content.schematics.client;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.api.level.wrapper.SchematicLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

/// The client render interfaces moved off Level, and the schematic world lives
/// in catnip's loader-agnostic half, so the pieces a renderer needs are
/// supplied here: flat lighting, and tints from the level being previewed in.
public record SchematicTintGetter(SchematicLevel schematic) implements BlockAndTintGetter {

	@Override
	public CardinalLighting cardinalLighting() {
		return CardinalLighting.DEFAULT;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return schematic.getLightEngine();
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver color) {
		// Tints come from the level the schematic is being previewed in.
		return Minecraft.getInstance().level == null ? -1
			: Minecraft.getInstance().level.getBlockTint(pos, color);
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return schematic.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return schematic.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return schematic.getFluidState(pos);
	}

	@Override
	public int getHeight() {
		return schematic.getHeight();
	}

	@Override
	public int getMinY() {
		return schematic.getMinY();
	}
}
