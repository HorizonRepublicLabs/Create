package com.simibubi.create.content.trains.entity;

import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer.ContraptionRenderState;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;

import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;

import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class CarriageContraptionEntityRenderer extends ContraptionEntityRenderer<CarriageContraptionEntity> {

	public CarriageContraptionEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(CarriageContraptionEntity entity, Frustum clippingHelper, double cameraX,
		double cameraY, double cameraZ) {
		Carriage carriage = entity.getCarriage();
		if (carriage != null)
			for (CarriageBogey bogey : carriage.bogeys)
				if (bogey != null)
					bogey.couplingAnchors.replace(v -> null);
		return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
	}

	/// The bogeys are drawn from the carriage itself, which the state carries
	/// over alongside the contraption.
	@Override
	public void submit(ContraptionRenderState state, PoseStack ms, SubmitNodeCollector collector,
		CameraRenderState camera) {
		if (!(state.entity instanceof CarriageContraptionEntity entity) || !entity.validForRender
			|| entity.firstPositionUpdate)
			return;

		super.submit(state, ms, collector, camera);

		Carriage carriage = entity.getCarriage();
		if (carriage == null)
			return;

		float partialTicks = AnimationTickHolder.getPartialTicks();
		int overlay = OverlayTexture.NO_OVERLAY;
		SuperRenderTypeBuffer buffers = DefaultSuperRenderTypeBuffer.getInstance();
		buffers.setCollector(collector);

		Vec3 position = entity.getPosition(partialTicks);

		float viewYRot = entity.getViewYRot(partialTicks);
		float viewXRot = entity.getViewXRot(partialTicks);
		int bogeySpacing = carriage.bogeySpacing;

		carriage.bogeys.forEach(bogey -> {
			if (bogey == null)
				return;

			BlockPos bogeyPos = bogey.isLeading ? BlockPos.ZERO
				: BlockPos.ZERO.relative(entity.getInitialOrientation()
					.getCounterClockWise(), bogeySpacing);

			if (!VisualizationManager.supportsVisualization(entity.level()) && !entity.getContraption()
				.isHiddenInPortal(bogeyPos)) {

				ms.pushPose();
				translateBogey(ms, bogey, bogeySpacing, viewYRot, viewXRot, partialTicks);

				int light = getBogeyLightCoords(entity, bogey, partialTicks);

				bogey.getStyle().render(bogey.getSize(), partialTicks, ms, buffers, light,
					overlay, bogey.wheelAngle.getValue(partialTicks), bogey.bogeyData, true);

				ms.popPose();
			}

			bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, partialTicks, bogey.isLeading);
			if (!carriage.isOnTwoBogeys())
				bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, partialTicks, !bogey.isLeading);
		});

		buffers.draw();
		buffers.setCollector(null);
	}

	public static void translateBogey(PoseStack ms, CarriageBogey bogey, int bogeySpacing, float viewYRot,
		float viewXRot, float partialTicks) {
		boolean selfUpsideDown = bogey.isUpsideDown();
		boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
		TransformStack.of(ms)
			.rotateYDegrees(viewYRot + 90)
			.rotateXDegrees(-viewXRot)
			.rotateYDegrees(180)
			.translate(0, 0, bogey.isLeading ? 0 : -bogeySpacing)
			.rotateYDegrees(-180)
			.rotateXDegrees(viewXRot)
			.rotateYDegrees(-viewYRot - 90)
			.rotateYDegrees(bogey.yaw.getValue(partialTicks))
			.rotateXDegrees(bogey.pitch.getValue(partialTicks))
			.translate(0, .5f, 0)
			.rotateZDegrees(selfUpsideDown ? 180 : 0)
			.translateY(selfUpsideDown != leadingUpsideDown ? 2 : 0);
	}

	public static int getBogeyLightCoords(CarriageContraptionEntity entity, CarriageBogey bogey, float partialTicks) {
		var anchorPosition = bogey.getAnchorPosition();

		var lightPos = BlockPos.containing(anchorPosition == null ? entity.getLightProbePosition(partialTicks) : anchorPosition);

		return LightCoordsUtil.pack(entity.level().getBrightness(LightLayer.BLOCK, lightPos),
			entity.level().getBrightness(LightLayer.SKY, lightPos));
	}

}
