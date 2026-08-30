package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.Create;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.animation.LerpedFloat.Chaser;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/// The gui no longer draws texture overlays for anyone else, so the blur is
/// blitted here, through the first person overlay hook.
public class CardboardArmorStealthOverlay implements IClientItemExtensions {

	private static final Identifier PACKAGE_BLUR_LOCATION = Create.asResource("textures/misc/package_blur.png");

	private static LerpedFloat opacity = LerpedFloat.linear()
		.startWithValue(0)
		.chase(0, 0.25f, Chaser.EXP);

	public static void clientTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;

		opacity.tickChaser();
		opacity.updateChaseTarget(CardboardArmorHandler.testForStealth(player) ? 1 : 0);
	}

	@Override
	public void renderFirstPersonOverlay(ItemStack stack, EquipmentSlot equipmentSlot, Player player,
		GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		float value = opacity.getValue(deltaTracker.getGameTimeDeltaPartialTick(false));
		if (value == 0)
			return;

		graphics.blit(RenderPipelines.GUI_TEXTURED, PACKAGE_BLUR_LOCATION, 0, 0, 0, 0, graphics.guiWidth(),
			graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight(), ARGB.white(value));
	}

}
