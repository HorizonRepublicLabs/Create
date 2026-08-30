package com.simibubi.create.content.equipment.extendoGrip;

import net.minecraft.world.entity.player.PlayerModelPart;

import net.minecraft.resources.Identifier;

import com.simibubi.create.foundation.render.CreateItemRenderer;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ExtendoGripRenderHandler {

	public static float mainHandAnimation;
	public static float lastMainHandAnimation;
	public static PartialModel pose = AllPartialModels.DEPLOYER_HAND_PUNCHING;

	public static void tick() {
		lastMainHandAnimation = mainHandAnimation;
		mainHandAnimation *= Mth.clamp(mainHandAnimation, 0.8f, 0.99f);

		pose = AllPartialModels.DEPLOYER_HAND_PUNCHING;
		if (!AllItems.EXTENDO_GRIP.isIn(getRenderedOffHandStack()))
			return;
		ItemStack main = getRenderedMainHandStack();
		if (main.isEmpty())
			return;
		if (!(main.getItem() instanceof BlockItem))
			return;
		if (!CreateItemRenderer.isBlockItem(main, Minecraft.getInstance().level))
			return;
		pose = AllPartialModels.DEPLOYER_HAND_HOLDING;
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		ItemStack heldItem = event.getItemStack();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		boolean rightHand = event.getHand() == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;

		ItemStack offhandItem = getRenderedOffHandStack();
		boolean notInOffhand = !AllItems.EXTENDO_GRIP.isIn(offhandItem);
		if (notInOffhand && !AllItems.EXTENDO_GRIP.isIn(heldItem))
			return;

		PoseStack ms = event.getPoseStack();
		var msr = TransformStack.of(ms);
		AbstractClientPlayer abstractclientplayerentity = mc.player;

		float flip = rightHand ? 1.0F : -1.0F;
		float swingProgress = event.getSwingProgress();
		boolean blockItem = heldItem.getItem() instanceof BlockItem;
		float equipProgress = blockItem ? 0 : event.getEquipProgress() / 4;

		ms.pushPose();
		if (event.getHand() == InteractionHand.MAIN_HAND) {

			if (1 - swingProgress > mainHandAnimation && swingProgress > 0)
				mainHandAnimation = 0.95f;
			float animation = Mth.lerp(AnimationTickHolder.getPartialTicks(),
											  ExtendoGripRenderHandler.lastMainHandAnimation,
											  ExtendoGripRenderHandler.mainHandAnimation);
			animation = animation * animation * animation;

			ms.translate(flip * (0.64000005F - .1f), -0.4F + equipProgress * -0.6F, -0.71999997F + .3f);

			ms.pushPose();
			msr.rotateYDegrees(flip * 75.0F);
			ms.translate(flip * -1.0F, 3.6F, 3.5F);
			msr.rotateZDegrees(flip * 120)
				.rotateXDegrees(200)
				.rotateYDegrees(flip * -135.0F);
			ms.translate(flip * 5.6F, 0.0F, 0.0F);
			msr.rotateYDegrees(flip * 40.0F);
			ms.translate(flip * 0.05f, -0.3f, -0.3f);

			AvatarRenderer playerrenderer = (AvatarRenderer) mc.getEntityRenderDispatcher()
				.getRenderer(player);
			// The hand is drawn from the skin and sleeve rather than the player.
			Identifier skinTexture = player.getSkin()
				.body()
				.texturePath();
			if (rightHand)
				playerrenderer.renderRightHand(event.getPoseStack(), event.getSubmitNodeCollector(),
					event.getPackedLight(), skinTexture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE),
					player);
			else
				playerrenderer.renderLeftHand(event.getPoseStack(), event.getSubmitNodeCollector(),
					event.getPackedLight(), skinTexture, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), player);
			ms.popPose();

			// Render gun
			ms.pushPose();
			ms.translate(flip * -0.1f, 0, -0.3f);
			ItemInHandRenderer firstPersonRenderer = mc.getEntityRenderDispatcher().getItemInHandRenderer();
			ItemDisplayContext transform =
				rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
			firstPersonRenderer.item(mc.player, notInOffhand ? heldItem : offhandItem, transform,
				event.getPoseStack(), event.getSubmitNodeCollector(), event.getPackedLight());

			if (!notInOffhand) {
				// The item pipeline applies a stack's camera transform itself, so
				// the grip only adds the offsets it wants on top.
				ms.translate(flip * -.05f, .15f, -1.2f);
				ms.translate(0, 0, -animation * 2.25f);
				if (blockItem && CreateItemRenderer.isBlockItem(heldItem, mc.level)) {
					msr.rotateYDegrees(flip * 45);
					ms.translate(flip * 0.15f, -0.15f, -.05f);
					ms.scale(1.25f, 1.25f, 1.25f);
				}

				firstPersonRenderer.item(mc.player, heldItem, transform, event.getPoseStack(),
					event.getSubmitNodeCollector(), event.getPackedLight());
			}

			ms.popPose();
		}
		ms.popPose();
		event.setCanceled(true);
	}

	private static ItemStack getRenderedMainHandStack() {
		return Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().mainHandItem;
	}

	private static ItemStack getRenderedOffHandStack() {
		return Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().offHandItem;
	}

}
