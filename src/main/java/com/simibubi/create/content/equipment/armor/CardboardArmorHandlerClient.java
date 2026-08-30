package com.simibubi.create.content.equipment.armor;

import net.minecraft.client.multiplayer.ClientLevel;

import com.simibubi.create.foundation.render.CreateRenderTypes;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageRenderer;
import com.simibubi.create.foundation.utility.TickBasedCache;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class CardboardArmorHandlerClient {

	private static final Cache<UUID, Integer> BOXES_PLAYERS_ARE_HIDING_AS = new TickBasedCache<>(20, true);

	@SubscribeEvent
	public static void keepCacheAliveDesignDespiteNotRendering(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!CardboardArmorHandler.testForStealth(player))
			return;
		try {
			getCurrentBoxIndex(player);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	/// A player is rendered from an extracted state now, so the box is drawn
	/// against the collector with what that state carries.
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void playerRendersAsBoxWhenSneaking(RenderPlayerEvent.Pre<?> event) {
		ClientLevel level = Minecraft.getInstance().level;
		Player player = level != null && level.getEntity(event.getRenderState().id) instanceof Player p ? p : null;
		if (player == null || !CardboardArmorHandler.testForStealth(player))
			return;

		event.setCanceled(true);

		if (player == Minecraft.getInstance().player
			&& Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON)
			return;

		AvatarRenderState renderState = event.getRenderState();
		PoseStack ms = event.getPoseStack();
		ms.pushPose();

		Vec3 renderOffset = event.getRenderer()
			.getRenderOffset(renderState);
		ms.translate(0, -renderOffset.y, 0);

		float movement = (float) player.position()
			.subtract(player.xo, player.yo, player.zo)
			.length();

		if (player.onGround())
			ms.translate(0,
				Math.min(Math.abs(Mth.cos((AnimationTickHolder.getRenderTime() % 256) / 2.0f)) * -renderOffset.y, movement * 5),
				0);

		float scale = renderState.scale;
		ms.scale(scale, scale, scale);

		try {
			PartialModel model = AllPartialModels.PACKAGES_TO_HIDE_AS.get(getCurrentBoxIndex(player));
			int entityId = player.getId();
			int light = renderState.lightCoords;
			float yaw = renderState.bodyRot;
			event.getSubmitNodeCollector()
				.submitCustomGeometry(ms, CreateRenderTypes.entitySolidBlockMipped(),
					(pose, vc) -> PackageRenderer.renderBox(entityId, yaw, light, model, vc));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		ms.popPose();
	}

	private static Integer getCurrentBoxIndex(Player player) throws ExecutionException {
		return BOXES_PLAYERS_ARE_HIDING_AS.get(player.getUUID(),
			() -> player.level().getRandom().nextInt(AllPartialModels.PACKAGES_TO_HIDE_AS.size()));
	}

}
