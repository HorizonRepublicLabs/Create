package com.simibubi.create.foundation.render;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.world.level.BlockAndLightGetter;

import net.minecraft.client.renderer.block.BlockAndTintGetter;

import net.minecraft.util.LightCoordsUtil;

import net.minecraft.world.level.LightLayer;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import java.util.BitSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.simibubi.create.infrastructure.config.AllConfigs;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityRenderHelper {

	/// LevelRenderer.getLightColor is gone in 26.x; this packs the same thing --
	/// sky brightness with block brightness raised to the block's own emission.
	public static int lightColorAt(BlockAndLightGetter level, BlockPos pos) {
		int sky = level.getBrightness(LightLayer.SKY, pos);
		int block = Math.max(level.getBlockState(pos)
			.getLightEmission(), level.getBrightness(LightLayer.BLOCK, pos));
		return LightCoordsUtil.pack(block, sky);
	}

	/**
	 * Renders the given list of BlockEntities, skipping those not marked in shouldRenderBEs,
	 * and marking those that error in erroredBEsOut.
	 *
	 * @param blockEntities   The list of BlockEntities to render.
	 * @param shouldRenderBEs A BitSet marking which BlockEntities in the list should be rendered. This will not be modified.
	 * @param erroredBEsOut   A BitSet to mark BlockEntities that error during rendering. This will be modified.
	 */
	public static void renderBlockEntities(List<BlockEntity> blockEntities, BitSet shouldRenderBEs, BitSet erroredBEsOut, @javax.annotation.Nullable VirtualRenderWorld renderLevel, Level realLevel, PoseStack ms, @javax.annotation.Nullable Matrix4f lightTransform, SuperRenderTypeBuffer buffer,
										   float pt) {
		for (int i = shouldRenderBEs.nextSetBit(0); i >= 0 && i < blockEntities.size(); i = shouldRenderBEs.nextSetBit(i + 1)) {
			BlockEntity blockEntity = blockEntities.get(i);
			if (VisualizationManager.supportsVisualization(realLevel) && VisualizationHelper.skipVanillaRender(blockEntity))
				continue;

			BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = Minecraft.getInstance()
				.getBlockEntityRenderDispatcher()
				.getRenderer(blockEntity);
			if (renderer == null) {
				// Don't bother looping over it again if we can't do anything with it.
				erroredBEsOut.set(i);
				continue;
			}

			BlockPos pos = blockEntity.getBlockPos();
			ms.pushPose();
			TransformStack.of(ms)
				.translate(pos);

			try {
				int realLevelLight = BlockEntityRenderHelper.lightColorAt(realLevel, getLightPos(lightTransform, pos));

				int light;
				if (renderLevel != null) {
					renderLevel.setExternalLight(realLevelLight);
					light = BlockEntityRenderHelper.lightColorAt(renderLevel, pos);
				} else {
					light = realLevelLight;
				}

				// A block entity is extracted into a render state and submitted
				// now; the light the contraption worked out is written onto that
				// state before it goes to the collector.
				SubmitNodeCollector collector = buffer.getCollector();
				if (collector == null)
					continue;

				CameraRenderState cameraRenderState = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
				BlockEntityRenderState state = renderer.createRenderState();
				renderer.extractRenderState(blockEntity, state, pt, cameraRenderState.pos, null);
				state.lightCoords = light;
				renderer.submit(state, ms, collector, cameraRenderState);

			} catch (Exception e) {
				// Prevent this BE from causing more issues in the future.
				erroredBEsOut.set(i);

				String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType()) + " could not be rendered virtually.";
				if (AllConfigs.client().explainRenderErrors.get()) Create.LOGGER.error(message, e);
				else Create.LOGGER.error(message);
			}

			ms.popPose();
		}

		if (renderLevel != null) {
			renderLevel.resetExternalLight();
		}
	}

	private static BlockPos getLightPos(@Nullable Matrix4f lightTransform, BlockPos contraptionPos) {
		if (lightTransform != null) {
			Vector4f lightVec = new Vector4f(contraptionPos.getX() + .5f, contraptionPos.getY() + .5f, contraptionPos.getZ() + .5f, 1);
			lightVec.mul(lightTransform);
			return BlockPos.containing(lightVec.x(), lightVec.y(), lightVec.z());
		} else {
			return contraptionPos;
		}
	}

}
