package com.simibubi.create.content.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

/// Models hand out parts rather than quads, and the tilt no longer travels as
/// model data, so it is read off the track itself when the parts are collected.
public class TrackModel extends DelegateBlockStateModel {

	public TrackModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
		List<BlockStateModelPart> parts) {
		List<BlockStateModelPart> collected = new ArrayList<>();
		super.collectParts(level, pos, state, random, collected);

		UnaryOperator<Vec3> transform = tiltOf(level, pos, state);
		if (transform == null) {
			parts.addAll(collected);
			return;
		}

		for (BlockStateModelPart part : collected)
			parts.add(new TiltedPart(part, transform));
	}

	@Nullable
	private static UnaryOperator<Vec3> tiltOf(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof TrackBlockEntity track) || !track.isTilted())
			return null;

		double angleIn = track.tilt.smoothingAngle.orElse(0d);
		double angle = Math.abs(angleIn);
		boolean flip = angleIn < 0;

		TrackShape trackShape = state.getValue(TrackBlock.SHAPE);
		double hAngle = switch (trackShape) {
			case XO -> 0;
			case PD -> 45;
			case ZO -> 90;
			case ND -> 135;
			default -> 0;
		};

		Vec3 verticalOffset = new Vec3(0, -0.25, 0);
		Vec3 diagonalRotationPoint =
			(trackShape == TrackShape.ND || trackShape == TrackShape.PD) ? new Vec3((Mth.SQRT_OF_TWO - 1) / 2, 0, 0)
				: Vec3.ZERO;

		return v -> {
			v = v.add(verticalOffset);
			v = VecHelper.rotateCentered(v, hAngle, Axis.Y);
			v = v.add(diagonalRotationPoint);
			v = VecHelper.rotate(v, angle, Axis.Z);
			v = v.subtract(diagonalRotationPoint);
			v = VecHelper.rotateCentered(v, -hAngle + (flip ? 180 : 0), Axis.Y);
			v = v.subtract(verticalOffset);
			return v;
		};
	}

	private record TiltedPart(BlockStateModelPart delegate,
		UnaryOperator<Vec3> transform) implements BlockStateModelPart {

		@Override
		public List<BakedQuad> getQuads(@Nullable Direction direction) {
			List<BakedQuad> quads = new ArrayList<>();
			for (BakedQuad templateQuad : delegate.getQuads(direction)) {
				BakedQuadHelper.Editor editor = BakedQuadHelper.edit(templateQuad);
				for (int j = 0; j < 4; j++)
					editor.setXYZ(j, transform.apply(editor.getXYZ(j)));
				quads.add(editor.build());
			}
			return quads;
		}

		@Override
		public boolean useAmbientOcclusion() {
			return delegate.useAmbientOcclusion();
		}

		@Override
		public Material.Baked particleMaterial() {
			return delegate.particleMaterial();
		}

		@Override
		public int materialFlags() {
			return delegate.materialFlags();
		}
	}
}
