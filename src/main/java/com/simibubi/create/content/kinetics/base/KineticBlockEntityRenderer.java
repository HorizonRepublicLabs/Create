package com.simibubi.create.content.kinetics.base;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import java.util.List;

import java.util.ArrayList;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import com.simibubi.create.foundation.render.CreateRenderTypes;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.neoforged.neoforge.model.data.ModelData;

public class KineticBlockEntityRenderer<T extends KineticBlockEntity> extends SafeBlockEntityRenderer<T> {

	public static final SuperByteBufferCache.Compartment<BlockState> KINETIC_BLOCK = new SuperByteBufferCache.Compartment<>();
	public static boolean rainbowMode = false;

	public KineticBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(T be, float partialTicks, PoseStack ms, SuperRenderTypeBuffer buffer,
							  int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		BlockState state = getRenderedBlockState(be);
		RenderType type = getRenderType(be, state);
		renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
	}

	protected BlockState getRenderedBlockState(T be) {
		return be.getBlockState();
	}

	/// A model no longer names its chunk layers; its quads carry the material,
	/// so a translucent part is what decides the moving block's render type.
	protected RenderType getRenderType(T be, BlockState state) {
		BlockStateModel model = Minecraft.getInstance()
			.getModelManager()
			.getBlockStateModelSet()
			.get(state);

		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(42L), parts);
		for (BlockStateModelPart part : parts)
			if ((part.materialFlags() & BakedQuad.FLAG_TRANSLUCENT) != 0)
				return CreateRenderTypes.translucentMovingBlock();

		return CreateRenderTypes.cutoutMovingBlock();
	}

	protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
		return CachedBuffers.block(KINETIC_BLOCK, state);
	}

	public static void renderRotatingKineticBlock(KineticBlockEntity be, BlockState renderedState, PoseStack ms,
												  VertexConsumer buffer, int light) {
		SuperByteBuffer superByteBuffer = CachedBuffers.block(KINETIC_BLOCK, renderedState);
		renderRotatingBuffer(be, superByteBuffer, ms, buffer, light);
	}

	public static void renderRotatingBuffer(KineticBlockEntity be, SuperByteBuffer superBuffer, PoseStack ms,
											VertexConsumer buffer, int light) {
		standardKineticRotationTransform(superBuffer, be, light).renderInto(ms, buffer);
	}

	public static float getAngleForBe(KineticBlockEntity be, final BlockPos pos, Axis axis) {
		float time = AnimationTickHolder.getRenderTime();
		float offset = getRotationOffsetForPosition(be, pos, axis);
		float angle = ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
		return angle;
	}

	public static SuperByteBuffer standardKineticRotationTransform(SuperByteBuffer buffer, KineticBlockEntity be,
																   int light) {
		final BlockPos pos = be.getBlockPos();
		Axis axis = ((IRotate) be.getBlockState()
			.getBlock()).getRotationAxis(be.getBlockState());
		return kineticRotationTransform(buffer, be, axis, getAngleForBe(be, pos, axis), light);
	}

	public static SuperByteBuffer kineticRotationTransform(SuperByteBuffer buffer, KineticBlockEntity be, Axis axis,
														   float angle, int light) {
		buffer.light(light);
		buffer.rotateCentered(angle, Direction.get(AxisDirection.POSITIVE, axis));

		if (KineticDebugger.isActive()) {
			rainbowMode = true;
			buffer.color(be.hasNetwork() ? Color.generateFromLong(be.network) : Color.WHITE);
		} else {
			float overStressedEffect = be.effects.overStressedEffect;
			if (overStressedEffect != 0) {
				boolean overstressed = overStressedEffect > 0;
				Color color = overstressed ? Color.RED : Color.SPRING_GREEN;
				float weight = overstressed ? overStressedEffect : -overStressedEffect;

				buffer.color(Color.WHITE.mixWith(color, weight));
			} else {
				buffer.color(Color.WHITE);
			}
		}

		return buffer;
	}

	public static float getRotationOffsetForPosition(KineticBlockEntity be, final BlockPos pos, final Axis axis) {
		return KineticBlockEntityVisual.rotationOffset(be.getBlockState(), axis, pos) + be.getRotationAngleOffset(axis);
	}

	public static BlockState shaft(Axis axis) {
		return AllBlocks.SHAFT.getDefaultState()
			.setValue(BlockStateProperties.AXIS, axis);
	}

	public static Axis getRotationAxisOf(KineticBlockEntity be) {
		return ((IRotate) be.getBlockState()
			.getBlock()).getRotationAxis(be.getBlockState());
	}

}
