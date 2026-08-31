package com.simibubi.create.content.equipment.symmetryWand;

import com.simibubi.create.foundation.ClientOnly;
import net.minecraft.util.LightCoordsUtil;

import net.minecraft.core.Direction;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;

import net.createmod.catnip.api.data.Iterate;

import com.mojang.blaze3d.vertex.QuadInstance;

import java.util.List;

import java.util.ArrayList;

import net.minecraft.util.ARGB;

import com.simibubi.create.foundation.render.CreateRenderTypes;

import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

@EventBusSubscriber
public class SymmetryHandler {

	private static int tickCounter = 0;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(EntityPlaceEvent event) {
		if (event.getLevel()
			.isClientSide())
			return;
		if (!(event.getEntity() instanceof Player player))
			return;

		Inventory inv = player.getInventory();
		for (int i = 0; i < Inventory.getSelectionSize(); i++)
			if (AllItems.WAND_OF_SYMMETRY.isIn(inv.getItem(i)))
				SymmetryWandItem.apply(player.level(), inv.getItem(i), player, event.getPos(), event.getPlacedBlock());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDestroyed(BreakBlockEvent event) {
		if (event.getLevel()
			.isClientSide())
			return;

		Player player = event.getPlayer();
		Inventory inv = player.getInventory();
		for (int i = 0; i < Inventory.getSelectionSize(); i++)
			if (AllItems.WAND_OF_SYMMETRY.isIn(inv.getItem(i)))
				SymmetryWandItem.remove(player.level(), inv.getItem(i), player, event.getPos());
	}

	@ClientOnly
	@SubscribeEvent
	public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentParticles event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		RandomSource random = RandomSource.create();

		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack stackInSlot = player.getInventory()
				.getItem(i);
			if (!AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot))
				continue;
			if (!SymmetryWandItem.isEnabled(stackInSlot))
				continue;
			SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
			if (mirror instanceof EmptyMirror)
				continue;

			BlockPos pos = BlockPos.containing(mirror.getPosition());

			float yShift = 0;
			double speed = 1 / 16d;
			yShift = Mth.sin((float) (AnimationTickHolder.getRenderTime() * speed)) / 5f;

			SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
			Camera info = mc.gameRenderer.mainCamera();
			Vec3 view = info.position();

			PoseStack ms = event.getPoseStack();
			ms.pushPose();
			ms.translate(pos.getX() - view.x(), pos.getY() - view.y(), pos.getZ() - view.z());
			ms.translate(0, yShift + .2f, 0);
			mirror.applyModelTransform(ms);
			BlockStateModel model = mirror.getModel()
				.get();
			VertexConsumer builder = buffer.getBuffer(CreateRenderTypes.solidMovingBlock());

			// Models hand out parts and each part its quads now, rather than a
			// block renderer walking the model for a render type.
			QuadInstance quadInstance = new QuadInstance();
			quadInstance.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
			quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
			quadInstance.setColor(-1);
			random.setSeed(Mth.getSeed(pos));
			List<BlockStateModelPart> parts = new ArrayList<>();
			model.collectParts(random, parts);
			PoseStack.Pose last = ms.last();
			for (BlockStateModelPart part : parts) {
				for (Direction direction : Iterate.directions)
					for (BakedQuad quad : part.getQuads(direction))
						builder.putBakedQuad(last, quad, quadInstance);
				for (BakedQuad quad : part.getQuads(null))
					builder.putBakedQuad(last, quad, quadInstance);
			}

			ms.popPose();
			buffer.draw();
		}
	}

	@ClientOnly
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (mc.level == null)
			return;
		if (mc.isPaused())
			return;

		tickCounter++;

		if (tickCounter % 10 == 0) {
			for (int i = 0; i < Inventory.getSelectionSize(); i++) {
				ItemStack stackInSlot = player.getInventory()
					.getItem(i);

				if (stackInSlot != null && AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot)
					&& SymmetryWandItem.isEnabled(stackInSlot)) {

					SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
					if (mirror instanceof EmptyMirror)
						continue;

					RandomSource random = mc.level.getRandom();
					double offsetX = (random.nextDouble() - 0.5) * 0.3;
					double offsetZ = (random.nextDouble() - 0.5) * 0.3;

					Vec3 pos = mirror.getPosition()
						.add(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					Vec3 speed = new Vec3(0, random.nextDouble() * 1 / 8f, 0);
					mc.level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		ClientLevel level = Minecraft.getInstance().level;
		RandomSource random = level.getRandom();

		double density = 0.8f;
		Vec3 start = Vec3.atLowerCornerOf(from)
			.add(0.5, 0.5, 0.5);
		Vec3 end = Vec3.atLowerCornerOf(to)
			.add(0.5, 0.5, 0.5);
		Vec3 diff = end.subtract(start);

		Vec3 step = diff.normalize()
			.scale(density);
		int steps = (int) (diff.length() / step.length());

		for (int i = 3; i < steps - 1; i++) {
			Vec3 pos = start.add(step.scale(i));
			Vec3 speed = new Vec3(0, random.nextDouble() * -40f, 0);

			level.addParticle(new DustParticleOptions(ARGB.colorFromFloat(1, 1, 1, 1), 1), pos.x, pos.y,
				pos.z, speed.x, speed.y, speed.z);
		}

		Vec3 speed = new Vec3(0, random.nextDouble() * 1 / 32f, 0);
		Vec3 pos = start.add(step.scale(2));
		level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);

		speed = new Vec3(0, random.nextDouble() * 1 / 32f, 0);
		pos = start.add(step.scale(steps));
		level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);
	}

}
