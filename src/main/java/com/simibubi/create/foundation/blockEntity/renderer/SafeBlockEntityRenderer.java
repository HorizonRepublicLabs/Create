package com.simibubi.create.foundation.blockEntity.renderer;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.feature.ModelFeatureRenderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.simibubi.create.foundation.mixin.accessor.LevelRendererAccessor;

import com.simibubi.create.foundation.mixin.accessor.LevelRendererAccessor;

import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class SafeBlockEntityRenderer<T extends BlockEntity>
	implements BlockEntityRenderer<T, SafeBlockEntityRenderer.SafeRenderState<T>> {

	/// 26.x splits rendering into an extract pass and a submit pass, with only
	/// this state carried between them. Create's renderers read the block entity
	/// while drawing, so it is carried across rather than every renderer being
	/// rewritten to snapshot what it needs. That does keep a live reference to
	/// the block entity into the submit pass.
	public static class SafeRenderState<T extends BlockEntity> extends BlockEntityRenderState {
		public @Nullable T blockEntity;
		public float partialTicks;
	}

	@Override
	public SafeRenderState<T> createRenderState() {
		return new SafeRenderState<>();
	}

	@Override
	public void extractRenderState(T be, SafeRenderState<T> state, float partialTicks, Vec3 cameraPosition,
		ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
		state.blockEntity = be;
		state.partialTicks = partialTicks;
	}

	@Override
	public final void submit(SafeRenderState<T> state, PoseStack poseStack, SubmitNodeCollector collector,
		CameraRenderState camera) {
		T be = state.blockEntity;
		if (be == null || isInvalid(be))
			return;
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		buffer.setCollector(collector);
		renderSafe(be, state.partialTicks, poseStack, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY);
		buffer.draw();
		buffer.setCollector(null);
	}

	protected abstract void renderSafe(T be, float partialTicks, PoseStack ms, SuperRenderTypeBuffer bufferSource, int light,
		int overlay);

	public boolean isInvalid(T be) {
		return !be.hasLevel() || be.getBlockState()
			.getBlock() == Blocks.AIR;
	}

	public boolean shouldCullItem(Vec3 itemPos, Level level) {
		if (level instanceof PonderLevel)
			return false;

		LevelRendererAccessor accessor = (LevelRendererAccessor) Minecraft.getInstance().levelRenderer;
		Frustum frustum = accessor.create$getCapturedFrustum() != null ?
			accessor.create$getCapturedFrustum() :
			accessor.create$getCullingFrustum();

		AABB itemBB = new AABB(
				itemPos.x - 0.25,
				itemPos.y - 0.25,
				itemPos.z - 0.25,
				itemPos.x + 0.25,
				itemPos.y + 0.25,
				itemPos.z + 0.25
		);

		return !frustum.isVisible(itemBB);
	}

	@Override
	public @NotNull AABB getRenderBoundingBox(@NotNull T blockEntity) {
		if (blockEntity instanceof CachedRenderBBBlockEntity cbe)
			return cbe.getRenderBoundingBox();

		return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
	}
}
