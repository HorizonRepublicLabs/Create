package com.simibubi.create.content.equipment.potatoCannon;

import com.simibubi.create.foundation.render.CreateItemRenderer;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PotatoProjectileRenderer extends EntityRenderer<PotatoProjectileEntity> {

	public PotatoProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(PotatoProjectileEntity entity, float yaw, float pt, PoseStack ms, SuperRenderTypeBuffer buffer,
		int light) {
		ItemStack item = entity.getItem();
		if (item.isEmpty())
			return;
		ms.pushPose();
		ms.translate(0, entity.getBoundingBox()
			.getYsize() / 2 - 1 / 8f, 0);
		entity.getRenderMode()
			.transform(ms, entity, pt);

		CreateItemRenderer.render(item, ItemDisplayContext.GROUND, ms, buffer, light, OverlayTexture.NO_OVERLAY, 0);
		ms.popPose();
	}

	@Override
	public Identifier getTextureLocation(PotatoProjectileEntity entity) {
		return null;
	}

}
