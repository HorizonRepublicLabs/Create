package com.simibubi.create.foundation.item.render;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.resources.Identifier;

/// The renderer a hand-drawn item uses is named in its model, so the names have
/// to resolve back to the renderers themselves.
public class CustomRenderedItems {

	private static final Map<Identifier, Supplier<CustomRenderedItemModelRenderer>> RENDERERS = new HashMap<>();

	public static void register(Identifier id, Supplier<CustomRenderedItemModelRenderer> renderer) {
		RENDERERS.put(id, renderer);
	}

	public static CustomRenderedItemModelRenderer get(Identifier id) {
		Supplier<CustomRenderedItemModelRenderer> supplier = RENDERERS.get(id);
		return supplier == null ? null : supplier.get();
	}
}
