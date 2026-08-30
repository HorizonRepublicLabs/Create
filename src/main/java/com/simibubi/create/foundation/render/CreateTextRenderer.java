package com.simibubi.create.foundation.render;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/// The font no longer draws into a buffer itself: it prepares the glyphs and
/// hands them over one at a time. This keeps the shape Create's world-space
/// text was written against.
public class CreateTextRenderer {

	public static void drawInBatch(Font font, Component text, float x, float y, int color, boolean dropShadow,
		Matrix4fc pose, SuperRenderTypeBuffer buffer, Font.DisplayMode displayMode, int backgroundColor, int light) {
		drawInBatch(font, text.getVisualOrderText(), x, y, color, dropShadow, pose, buffer, displayMode,
			backgroundColor, light);
	}

	public static void drawInBatch(Font font, FormattedCharSequence text, float x, float y, int color,
		boolean dropShadow, Matrix4fc pose, SuperRenderTypeBuffer buffer, Font.DisplayMode displayMode,
		int backgroundColor, int light) {
		visit(font.prepareText(text, x, y, color, dropShadow, false, backgroundColor), pose, buffer, displayMode,
			light);
	}

	/// The outline passes behind the text, as it did when the font drew both.
	public static void drawInBatch8xOutline(Font font, FormattedCharSequence text, float x, float y, int color,
		int outlineColor, Matrix4fc pose, SuperRenderTypeBuffer buffer, int light) {
		visit(font.prepare8xTextOutline(text, x, y, outlineColor), pose, buffer, Font.DisplayMode.NORMAL, light);
		visit(font.prepareText(text, x, y, color, false, false, 0), pose, buffer, Font.DisplayMode.POLYGON_OFFSET,
			light);
	}

	private static void visit(Font.PreparedText prepared, Matrix4fc pose, SuperRenderTypeBuffer buffer,
		Font.DisplayMode displayMode, int light) {
		Matrix4f copy = new Matrix4f(pose);
		prepared.visit(new Font.GlyphVisitor() {
			@Override
			public void acceptRenderable(TextRenderable renderable) {
				VertexConsumer vc = buffer.getBuffer(renderable.renderType(displayMode, false));
				renderable.render(copy, vc, light, false);
			}
		});
	}
}
