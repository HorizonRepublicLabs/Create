package com.simibubi.create.content.equipment.hats;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.createmod.catnip.api.client.render.SuperByteBuffer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import com.simibubi.create.foundation.render.CreateCachedBuffers;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.schedule.hat.TrainHatInfo;
import com.simibubi.create.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.simibubi.create.foundation.mixin.accessor.EntityRenderDispatcherAccessor;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CreateHatArmorLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
	extends RenderLayer<S, M> {

	/// The hat and its placement info come off the entity, which submit cannot
	/// reach, so they are gathered during extraction.
	public static class HatRenderState extends LivingEntityRenderState {
		public PartialModel hat;
		public TrainHatInfo info;
	}


	public CreateHatArmorLayer(RenderLayerParent<S, M> renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack ms, SubmitNodeCollector collector, int light, S state, float yRot,
		float xRot) {
		if (!(state instanceof HatRenderState hatState) || hatState.hat == null)
			return;
		PartialModel hat = hatState.hat;
		TrainHatInfo info = hatState.info;
		M entityModel = getParentModel();
		ms.pushPose();

		var msr = TransformStack.of(ms);
		List<ModelPart> partsToHead = new ArrayList<>();

		// AgeableListModel and HierarchicalModel are gone; every model has a
		// root part, and the baby scaling the ageable branch applied is handled
		// by the renderer before the layer runs.
		partsToHead.addAll(TrainHatInfo.getAdjustedPart(info, entityModel.root(), "head"));

		if (!partsToHead.isEmpty()) {
			partsToHead.forEach(part -> part.translateAndRotate(ms));

			ModelPart lastChild = partsToHead.get(partsToHead.size() - 1);
			if (!lastChild.isEmpty()) {
				Cube cube = lastChild.cubes.get(Mth.clamp(info.cubeIndex(), 0, lastChild.cubes.size() - 1));
				ms.translate(info.offset().x() / 16.0F, (cube.minY - cube.maxY + info.offset().y()) / 16.0F, info.offset().z() / 16.0F);
				float max = Math.max(cube.maxX - cube.minX, cube.maxZ - cube.minZ) / 8.0F * info.scale();
				ms.scale(max, max, max);
			}

			ms.scale(1, -1, -1);
			ms.translate(0, -2.25F / 16.0F, 0);
			msr.rotateXDegrees(-8.5F);
			BlockState air = Blocks.AIR.defaultBlockState();
			SuperByteBuffer sbb = CreateCachedBuffers.partial(hat, air)
				.disableDiffuse()
				.light(light);
			collector.submitCustomGeometry(ms, Sheets.cutoutBlockItemSheet(),
				(pose, vc) -> sbb.renderInto(new PoseStack(), vc));
		}

		ms.popPose();
	}

	public static void registerOnAll(EntityRenderDispatcher renderManager) {
		for (EntityRenderer<? extends Avatar, ?> renderer : renderManager.getPlayerRenderers()
			.values())
			registerOn(renderer);
		for (EntityRenderer<?, ?> renderer : ((EntityRenderDispatcherAccessor) renderManager).create$getRenderers().values())
			registerOn(renderer);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void registerOn(EntityRenderer<?, ?> entityRenderer) {
		if (!(entityRenderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer))
			return;

		livingRenderer.addLayer(new CreateHatArmorLayer(livingRenderer));
	}

}
