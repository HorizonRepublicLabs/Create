package com.simibubi.create.content.logistics.box;

import com.simibubi.create.foundation.render.CreateRenderTypes;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.simibubi.create.foundation.render.CreateCachedBuffers;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class PackageRenderer extends EntityRenderer<PackageEntity, PackageRenderer.PackageRenderState> {

	/// Renderers read a render state rather than the entity now, so the box
	/// model and the facing are gathered up front.
	public static class PackageRenderState extends EntityRenderState {
		public PartialModel model;
		public float yaw;
		public int entityId;
	}


	public PackageRenderer(Context pContext) {
		super(pContext);
		shadowRadius = 0.5f;
	}

	@Override
	public PackageRenderState createRenderState() {
		return new PackageRenderState();
	}

	@Override
	public void extractRenderState(PackageEntity entity, PackageRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		ItemStack box = entity.box;
		if (box.isEmpty() || !PackageItem.isPackage(box))
			box = AllBlocks.CARDBOARD_BLOCK.asStack();
		state.model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
		state.yaw = entity.getYRot();
		state.entityId = entity.getId();
		// flywheel draws the package itself when visualisation is on
		if (VisualizationManager.supportsVisualization(entity.level()))
			state.model = null;
	}

	@Override
	public void submit(PackageRenderState state, PoseStack ms, SubmitNodeCollector collector,
		CameraRenderState camera) {
		super.submit(state, ms, collector, camera);
		if (state.model == null)
			return;
		collector.submitCustomGeometry(ms, CreateRenderTypes.entitySolidBlockMipped(),
			(pose, vc) -> renderBox(state.entityId, state.yaw, state.lightCoords, state.model, vc));
	}

	public static void renderBox(int entityId, float yaw, int light, PartialModel model, VertexConsumer vc) {
		if (model == null)
			return;
		SuperByteBuffer sbb = CreateCachedBuffers.partial(model, Blocks.AIR.defaultBlockState());
		sbb.translate(-.5, 0, -.5)
			.rotateCentered(-AngleHelper.rad(yaw + 90), Direction.UP)
			.light(light)
			.nudge(entityId);
		sbb.renderInto(new PoseStack(), vc);
	}



}
