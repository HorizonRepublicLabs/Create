package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

public class CustomRenderedItemModel extends DelegateBlockStateModel {

	public CustomRenderedItemModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public BlockStateModel applyTransform(ItemDisplayContext cameraItemDisplayContext, PoseStack mat,
		boolean leftHand) {
		// Super call returns originalModel, but we want to return this, else BEWLR
		// won't be used.
		super.applyTransform(cameraItemDisplayContext, mat, leftHand);
		return this;
	}

	public BlockStateModel getOriginalModel() {
		return originalModel;
	}

}
