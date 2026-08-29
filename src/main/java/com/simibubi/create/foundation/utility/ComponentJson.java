package com.simibubi.create.foundation.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;

/// Component.Serializer went away in 26.x; components round-trip through
/// ComponentSerialization.CODEC now. Create stores them as JSON strings in
/// several tags, so these keep that shape.
public class ComponentJson {

	public static String toJson(Component component, HolderLookup.Provider registries) {
		return ComponentSerialization.CODEC.encodeStart(ops(registries), component)
			.result()
			.map(JsonElement::toString)
			.orElse("");
	}

	@Nullable
	public static Component fromJson(String json, HolderLookup.Provider registries) {
		if (json == null || json.isEmpty())
			return null;
		return ComponentSerialization.CODEC.parse(ops(registries), JsonParser.parseString(json))
			.result()
			.orElse(null);
	}

	private static RegistryOps<JsonElement> ops(HolderLookup.Provider registries) {
		return registries.createSerializationContext(JsonOps.INSTANCE);
	}

	private ComponentJson() {}
}
