package com.simibubi.create.foundation.gui;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

/// This was a copy of Forge's GuiUtils, kept because Forge dropped it. 26.x
/// renders tooltips at the end of the frame through
/// GuiGraphicsExtractor.setTooltipForNextFrame rather than immediately, so
/// there is nothing left to vendor and these just forward to it.
///
/// One behavioural note: the old copy drew straight away and honoured the
/// background and border colours passed in, via RenderTooltipEvent. Vanilla
/// owns tooltip styling now, so those arguments are accepted and ignored.
public class RemovedGuiUtils {
	@NotNull
	private static ItemStack cachedTooltipStack = ItemStack.EMPTY;

	public static void preItemToolTip(@NotNull ItemStack stack) {
		cachedTooltipStack = stack;
	}

	public static void postItemToolTip() {
		cachedTooltipStack = ItemStack.EMPTY;
	}

	public static void drawHoveringText(GuiGraphicsExtractor graphics, List<? extends FormattedText> textLines,
		int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, Font font) {
		drawHoveringText(cachedTooltipStack, graphics, textLines, mouseX, mouseY, screenWidth, screenHeight,
			maxTextWidth, font);
	}

	public static void drawHoveringText(GuiGraphicsExtractor graphics, List<? extends FormattedText> textLines,
		int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, int backgroundColor,
		int borderColorStart, int borderColorEnd, Font font) {
		drawHoveringText(cachedTooltipStack, graphics, textLines, mouseX, mouseY, screenWidth, screenHeight,
			maxTextWidth, font);
	}

	public static void drawHoveringText(@NotNull ItemStack stack, GuiGraphicsExtractor graphics,
		List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
		int maxTextWidth, int backgroundColor, int borderColorStart, int borderColorEnd, Font font) {
		drawHoveringText(stack, graphics, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
	}

	public static void drawHoveringText(@NotNull ItemStack stack, GuiGraphicsExtractor graphics,
		List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
		int maxTextWidth, Font font) {
		if (textLines.isEmpty())
			return;

		List<Component> components = textLines.stream()
			.map(RemovedGuiUtils::toComponent)
			.toList();
		graphics.setTooltipForNextFrame(font, components, stack.getTooltipImage(), mouseX, mouseY);
	}

	private static Component toComponent(FormattedText text) {
		if (text instanceof Component component)
			return component;
		StringBuilder builder = new StringBuilder();
		text.visit(string -> {
			builder.append(string);
			return Optional.empty();
		});
		return Component.literal(builder.toString());
	}
}
