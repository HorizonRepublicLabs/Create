package com.simibubi.create.foundation.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

/// A model part that forwards everything to another one. 26.x builds block
/// geometry from parts rather than handing back a quad list, so a model that
/// wants to rewrite quads wraps the parts it was given instead of overriding
/// getQuads.
public abstract class DelegateModelPart implements BlockStateModelPart {

	protected final BlockStateModelPart wrapped;

	protected DelegateModelPart(BlockStateModelPart wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public abstract List<BakedQuad> getQuads(@Nullable Direction direction);

	@Override
	@SuppressWarnings("deprecation")
	public boolean useAmbientOcclusion() {
		return wrapped.useAmbientOcclusion();
	}

	@Override
	public Material.Baked particleMaterial() {
		return wrapped.particleMaterial();
	}

	@Override
	public int materialFlags() {
		return wrapped.materialFlags();
	}
}
