package com.simibubi.create.content.equipment.zapper;

import com.simibubi.create.foundation.render.CreateItemRenderer;

import net.minecraft.resources.Identifier;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ZapperItemRenderer extends CustomRenderedItemModelRenderer {

	protected ZapperItemRenderer(Identifier baseModel) {
		super(baseModel);
	}


	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
		PoseStack ms, SuperRenderTypeBuffer buffer, int light, int overlay) {
		// Block indicator
		if (transformType == ItemDisplayContext.GUI && stack.has(AllDataComponents.SHAPER_BLOCK_USED))
			renderBlockUsed(stack, ms, buffer, light, overlay);
	}

	/// The item pipeline picks the model for a stack itself, including the
	/// inventory model a cross-collision block draws as, so the indicator just
	/// asks for the block's item.
	private void renderBlockUsed(ItemStack stack, PoseStack ms, SuperRenderTypeBuffer buffer, int light, int overlay) {
		BlockState state = stack.get(AllDataComponents.SHAPER_BLOCK_USED);

		ms.pushPose();
		ms.translate(-0.3F, -0.45F, -0.0F);
		ms.scale(0.25F, 0.25F, 0.25F);
		CreateItemRenderer.render(new ItemStack(state.getBlock()), ItemDisplayContext.NONE, ms, buffer, light, overlay);
		ms.popPose();
	}

	protected float getAnimationProgress(float pt, boolean leftHanded, boolean mainHand) {
		float animation = CreateClient.ZAPPER_RENDER_HANDLER.getAnimation(mainHand ^ leftHanded, pt);
		return Mth.clamp(animation * 5, 0, 1);
	}

}
