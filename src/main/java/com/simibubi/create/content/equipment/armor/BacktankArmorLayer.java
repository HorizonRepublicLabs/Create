package com.simibubi.create.content.equipment.armor;

import java.util.List;

import com.simibubi.create.foundation.render.CreateCachedBuffers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.mixin.accessor.EntityRenderDispatcherAccessor;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.level.block.state.BlockState;

public class BacktankArmorLayer<S extends HumanoidRenderState, M extends EntityModel<? super S>>
	extends RenderLayer<S, M> {

	public BacktankArmorLayer(RenderLayerParent<S, M> renderer) {
		super(renderer);
	}

	/// Render layers submit geometry against a render state now rather than
	/// drawing from the entity, so the worn backtank comes off the state's
	/// chest equipment and the buffers go through a custom geometry node.
	@Override
	public void submit(PoseStack ms, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		if (state.hasPose(Pose.SLEEPING))
			return;

		ItemStack worn = state.chestEquipment;
		if (!(worn.getItem() instanceof BacktankItem item))
			return;

		M entityModel = getParentModel();
		if (!(entityModel instanceof HumanoidModel<?> model))
			return;

		boolean hasGlint = worn.hasFoil();
		BlockState renderedState = item.getBlock()
			.defaultBlockState()
			.setValue(BacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
		SuperByteBuffer backtank = CachedBuffers.block(renderedState);
		SuperByteBuffer cogs = CreateCachedBuffers.partial(BacktankRenderer.getCogsModel(renderedState), renderedState);
		SuperByteBuffer nob = CreateCachedBuffers.partial(BacktankRenderer.getShaftModel(renderedState), renderedState);

		ms.pushPose();

		model.body.translateAndRotate(ms);
		ms.translate(-1 / 2f, 10 / 16f, 1f);
		ms.scale(1, -1, -1);

		nob.translate(0, -3f / 16, 0);

		cogs.center()
			.rotateYDegrees(180)
			.uncenter()
			.translate(0, 6.5f / 16, 11f / 16)
			.rotate(AngleHelper.rad(2 * AnimationTickHolder.getRenderTime() % 360), Direction.EAST)
			.translate(0, -6.5f / 16, -11f / 16);

		RenderType renderType = hasGlint ? RenderTypes.armorEntityGlint() : Sheets.cutoutBlockItemSheet();
		collector.submitCustomGeometry(ms, renderType, (pose, vc) -> {
			for (SuperByteBuffer buffer : List.of(backtank, nob, cogs))
				buffer.disableDiffuse()
					.light(light)
					.renderInto(new PoseStack(), vc);
		});

		ms.popPose();
	}

	public static void registerOnAll(EntityRenderDispatcher renderManager) {
		for (EntityRenderer<? extends Avatar, ?> renderer : renderManager.getPlayerRenderers().values())
			registerOn(renderer);
		for (EntityRenderer<?, ?> renderer : ((EntityRenderDispatcherAccessor) renderManager).create$getRenderers().values())
			registerOn(renderer);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void registerOn(EntityRenderer<?, ?> entityRenderer) {
		if (!(entityRenderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer))
			return;
		if (!(livingRenderer.getModel() instanceof HumanoidModel))
			return;
		livingRenderer.addLayer(new BacktankArmorLayer(livingRenderer));
	}

}
