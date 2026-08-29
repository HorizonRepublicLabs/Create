package com.simibubi.create.content.equipment.potatoCannon;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.item.ItemModelResolver;

import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;

import org.joml.Matrix4f;

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

public class PotatoProjectileRenderer
	extends EntityRenderer<PotatoProjectileEntity, PotatoProjectileRenderer.ProjectileRenderState> {

	/// The render mode's transform needs the entity and the camera, neither of
	/// which submit can reach, so it runs during extraction and the resulting
	/// matrix travels in the state.
	public static class ProjectileRenderState extends ItemClusterRenderState {
		public final Matrix4f transform = new Matrix4f();
		public float ysize;
	}

	private final ItemModelResolver itemModelResolver;

	public PotatoProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public ProjectileRenderState createRenderState() {
		return new ProjectileRenderState();
	}

	@Override
	public void extractRenderState(PotatoProjectileEntity entity, ProjectileRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.extractItemGroupRenderState(entity, entity.getItem(), itemModelResolver);
		state.ysize = (float) entity.getBoundingBox()
			.getYsize();
		PoseStack scratch = new PoseStack();
		entity.getRenderMode()
			.transform(scratch, entity, partialTicks);
		state.transform.set(scratch.last()
			.pose());
	}

	@Override
	public void submit(ProjectileRenderState state, PoseStack ms, SubmitNodeCollector collector,
		CameraRenderState camera) {
		if (state.item.isEmpty())
			return;
		ms.pushPose();
		ms.translate(0, state.ysize / 2 - 1 / 8f, 0);
		ms.mulPose(state.transform);
		state.item.submit(ms, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		ms.popPose();
		super.submit(state, ms, collector, camera);
	}

}
