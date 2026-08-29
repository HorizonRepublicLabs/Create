package com.simibubi.create.content.equipment.blueprint;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import net.minecraft.client.renderer.item.ItemModelResolver;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;

import java.util.List;

import java.util.ArrayList;

import com.simibubi.create.foundation.render.CreateItemRenderer;

import com.simibubi.create.foundation.render.CreateCachedBuffers;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import org.joml.Matrix3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.data.Couple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity, BlueprintRenderer.BlueprintRenderState> {

	/// Everything submit needs is gathered here: the frame model, the facing,
	/// and the resolved display items for each section.
	public static class BlueprintRenderState extends EntityRenderState {
		public PartialModel frame;
		public int size;
		public float yaw;
		public float xRot;
		public final List<SlotItem> items = new ArrayList<>();
	}

	public record SlotItem(ItemStackRenderState item, int x, int y, boolean primary) {}

	private final ItemModelResolver itemModelResolver;

	public BlueprintRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public BlueprintRenderState createRenderState() {
		return new BlueprintRenderState();
	}

	@Override
	public void extractRenderState(BlueprintEntity entity, BlueprintRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.size = entity.size;
		state.yaw = entity.getYRot();
		state.xRot = entity.getXRot();
		state.frame = entity.size == 3 ? AllPartialModels.CRAFTING_BLUEPRINT_3x3
			: entity.size == 2 ? AllPartialModels.CRAFTING_BLUEPRINT_2x2 : AllPartialModels.CRAFTING_BLUEPRINT_1x1;

		state.items.clear();
		for (int x = 0; x < entity.size; x++) {
			for (int y = 0; y < entity.size; y++) {
				BlueprintSection section = entity.getSection(x * entity.size + y);
				int slotX = x;
				int slotY = y;
				section.getDisplayItems()
					.forEachWithContext((stack, primary) -> {
						if (stack.isEmpty())
							return;
						ItemStackRenderState resolved = new ItemStackRenderState();
						itemModelResolver.updateForTopItem(resolved, stack, ItemDisplayContext.GUI, entity.level(), null,
							0);
						state.items.add(new SlotItem(resolved, slotX, slotY, primary));
					});
			}
		}
	}

	@Override
	public void submit(BlueprintRenderState state, PoseStack ms, SubmitNodeCollector collector,
		CameraRenderState camera) {
		float yaw = state.yaw;
		SuperByteBuffer sbb = CreateCachedBuffers.partial(state.frame, Blocks.AIR.defaultBlockState());
		sbb.rotateYDegrees(-yaw)
			.rotateXDegrees(90.0F + state.xRot)
			.translate(-.5, -1 / 32f, -.5);
		if (state.size == 2)
			sbb.translate(.5, 0, -.5);
		sbb.disableDiffuse()
			.light(state.lightCoords);
		collector.submitCustomGeometry(ms, Sheets.cutoutBlockItemSheet(), (pose, vc) -> sbb.renderInto(new PoseStack(), vc));

		super.submit(state, ms, collector, camera);

		ms.pushPose();

		float fakeNormalXRotation = -15;
		int bl = state.lightCoords >> 4 & 0xf;
		int sl = state.lightCoords >> 20 & 0xf;
		boolean vertical = state.xRot != 0;
		if (state.xRot == -90)
			fakeNormalXRotation = -45;
		else if (state.xRot == 90 || yaw % 180 != 0) {
			bl /= 1.35;
			sl /= 1.35;
		}
		int itemLight = Mth.floor(sl + .5) << 20 | (Mth.floor(bl + .5) & 0xf) << 4;

		TransformStack.of(ms)
			.rotateYDegrees(vertical ? 0 : -yaw)
			.rotateXDegrees(fakeNormalXRotation);
		Matrix3f copy = new Matrix3f(ms.last()
			.normal());

		ms.popPose();
		ms.pushPose();

		TransformStack.of(ms)
			.rotateYDegrees(-yaw)
			.rotateXDegrees(state.xRot)
			.translate(0, 0, 1 / 32f + .001);

		if (state.size == 3)
			ms.translate(-1, -1, 0);

		PoseStack squashedMS = new PoseStack();
		squashedMS.last()
			.pose()
			.mul(ms.last()
				.pose());

		for (SlotItem slot : state.items) {
			squashedMS.pushPose();
			squashedMS.translate(slot.x(), slot.y(), 0);
			squashedMS.scale(.5f, .5f, 1 / 1024f);
			if (!slot.primary()) {
				squashedMS.translate(0.325f, -0.325f, 1);
				squashedMS.scale(.625f, .625f, 1);
			}
			squashedMS.last()
				.normal()
				.set(copy);
			slot.item()
				.submit(squashedMS, collector, itemLight, OverlayTexture.NO_OVERLAY, 0);
			squashedMS.popPose();
		}

		ms.popPose();
	}

}
