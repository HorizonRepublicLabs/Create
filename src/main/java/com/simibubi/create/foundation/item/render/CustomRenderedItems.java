package com.simibubi.create.foundation.item.render;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.resources.Identifier;

/// The renderer a hand-drawn item uses is named in its model, so the names have
/// to resolve back to the renderers themselves.
public class CustomRenderedItems {

	private static final Map<Identifier, CustomRenderedItemModelRenderer> RENDERERS = new HashMap<>();

	/// Built as it is registered: a renderer asks for its partial models in its
	/// constructor, and those have to be claimed before models are baked.
	public static void register(Identifier id, Supplier<CustomRenderedItemModelRenderer> renderer) {
		RENDERERS.put(id, renderer.get());
	}

	public static CustomRenderedItemModelRenderer get(Identifier id) {
		return RENDERERS.get(id);
	}
}
