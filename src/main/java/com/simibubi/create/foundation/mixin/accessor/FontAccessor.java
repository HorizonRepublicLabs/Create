package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Font;

/// Glyphs come from a provider now, and the font keeps it to itself. The flap
/// display picks its own glyphs per character, so it needs the way in.
@Mixin(Font.class)
public interface FontAccessor {
	@Accessor("provider")
	Font.Provider create$getProvider();
}
