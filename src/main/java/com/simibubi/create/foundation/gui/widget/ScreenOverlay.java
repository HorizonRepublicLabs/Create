package com.simibubi.create.foundation.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A set of widgets that are offset on the Z axis, allowing them to render above/below other "layers".
 */
public class ScreenOverlay extends CompositeWidget {
	public final int zOffset;

	public ScreenOverlay(int zOffset) {
		this.zOffset = zOffset;
	}

	/// The gui stack has no z any more, so the offset no longer moves anything;
	/// widgets draw in submission order instead.
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.pose()
			.pushMatrix();
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		graphics.pose()
			.popMatrix();
	}
}
