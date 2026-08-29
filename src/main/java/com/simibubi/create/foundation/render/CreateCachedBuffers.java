package com.simibubi.create.foundation.render;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperBufferFactory;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.catnip.api.client.render.SuperByteBufferCache.Compartment;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/// catnip's CachedBuffers only deals in BlockStates: its common module has no
/// Flywheel on the classpath, so the PartialModel overloads cannot live there.
/// Create depends on both, so they live here instead.
public class CreateCachedBuffers {

	public static final Compartment<PartialModel> PARTIAL = new Compartment<>();
	public static final Compartment<Pair> DIRECTIONAL_PARTIAL = new Compartment<>();

	/// Key for a partial cached per facing, since the rotation is baked in.
	public record Pair(PartialModel partial, Direction direction) {}

	public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState) {
		return SuperByteBufferCache.getInstance()
			.get(PARTIAL, partial,
				() -> SuperBufferFactory.getInstance()
					.createForBlock(partial.get(), referenceState));
	}

	public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState, Supplier<PoseStack> modelTransform) {
		return SuperByteBufferCache.getInstance()
			.get(PARTIAL, partial,
				() -> SuperBufferFactory.getInstance()
					.createForBlock(partial.get(), referenceState, modelTransform.get()));
	}

	public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState) {
		return partialFacing(partial, referenceState, referenceState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING));
	}

	public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState, Direction facing) {
		return partial(partial, referenceState, CachedBuffers.rotateToFace(facing));
	}

	public static SuperByteBuffer partialFacingVertical(PartialModel partial, BlockState referenceState, Direction facing) {
		return partial(partial, referenceState, CachedBuffers.rotateToFaceVertical(facing));
	}

	public static SuperByteBuffer partialDirectional(PartialModel partial, BlockState referenceState, Direction dir,
		Supplier<PoseStack> modelTransform) {
		return SuperByteBufferCache.getInstance()
			.get(DIRECTIONAL_PARTIAL, new Pair(partial, dir),
				() -> SuperBufferFactory.getInstance()
					.createForBlock(partial.get(), referenceState, modelTransform.get()));
	}

	private CreateCachedBuffers() {}
}
