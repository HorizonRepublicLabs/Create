package com.simibubi.create.foundation.item.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class CustomItemModels {

	private final Multimap<Identifier, NonNullFunction<BlockStateModel, ? extends BlockStateModel>> modelFuncs = MultimapBuilder.hashKeys().arrayListValues().build();
	private final Map<Item, NonNullFunction<BlockStateModel, ? extends BlockStateModel>> finalModelFuncs = new IdentityHashMap<>();
	private boolean funcsLoaded = false;

	public void register(Identifier item, NonNullFunction<BlockStateModel, ? extends BlockStateModel> func) {
		modelFuncs.put(item, func);
	}

	public void forEach(NonNullBiConsumer<Item, NonNullFunction<BlockStateModel, ? extends BlockStateModel>> consumer) {
		loadEntriesIfMissing();
		finalModelFuncs.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (!funcsLoaded) {
			loadEntries();
			funcsLoaded = true;
		}
	}

	private void loadEntries() {
		finalModelFuncs.clear();
		modelFuncs.asMap().forEach((location, funcList) -> {
			Item item = BuiltInRegistries.ITEM.get(location);
			if (item == Items.AIR) {
				return;
			}

			NonNullFunction<BlockStateModel, ? extends BlockStateModel> finalFunc = null;
			for (NonNullFunction<BlockStateModel, ? extends BlockStateModel> func : funcList) {
				if (finalFunc == null) {
					finalFunc = func;
				} else {
					finalFunc = finalFunc.andThen(func);
				}
			}

			finalModelFuncs.put(item, finalFunc);
		});
	}

}
