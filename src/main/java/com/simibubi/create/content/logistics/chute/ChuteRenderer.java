package com.simibubi.create.content.logistics.chute;

import com.simibubi.create.foundation.render.CreateItemRenderer;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.chute.ChuteBlock.Shape;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

public class ChuteRenderer extends SafeBlockEntityRenderer<ChuteBlockEntity> {

	public ChuteRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(ChuteBlockEntity be, float partialTicks, PoseStack ms, SuperRenderTypeBuffer buffer, int light,
		int overlay) {
		if (be.item.isEmpty())
			return;
		BlockState blockState = be.getBlockState();
		if (blockState.getValue(ChuteBlock.FACING) != Direction.DOWN)
			return;
		if (blockState.getValue(ChuteBlock.SHAPE) != Shape.WINDOW
			&& (be.bottomPullDistance == 0 || be.itemPosition.getValue(partialTicks) > .5f))
			return;

		renderItem(be, partialTicks, ms, buffer, light, overlay);
	}

	public static void renderItem(ChuteBlockEntity be, float partialTicks, PoseStack ms, SuperRenderTypeBuffer buffer,
		int light, int overlay) {
		var msr = TransformStack.of(ms);
		ms.pushPose();
		msr.center();
		float itemScale = .5f;
		float itemPosition = be.itemPosition.getValue(partialTicks);
		ms.translate(0, -.5 + itemPosition, 0);
		if (PackageItem.isPackage(be.item)) {
			ms.scale(1.5f, 1.5f, 1.5f);
		} else {
			ms.scale(itemScale, itemScale, itemScale);
			msr.rotateXDegrees(itemPosition * 180);
			msr.rotateYDegrees(itemPosition * 180);
		}
		CreateItemRenderer.render(be.item, ItemDisplayContext.FIXED, ms, buffer, light, overlay, 0);
		ms.popPose();
	}

}
