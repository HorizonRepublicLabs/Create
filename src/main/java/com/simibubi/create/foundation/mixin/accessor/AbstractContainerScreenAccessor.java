package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/// A container screen's size is final now, but Create's screens size themselves
/// from their own background textures before init runs.
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Mutable
	@Accessor("imageWidth")
	void create$setImageWidth(int imageWidth);

	@Mutable
	@Accessor("imageHeight")
	void create$setImageHeight(int imageHeight);
}
