package com.simibubi.create.foundation.model;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.item.render.CustomItemModels;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;

import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;

public class ModelSwapper {

	protected CustomBlockModels customBlockModels = new CustomBlockModels();
	protected CustomItemModels customItemModels = new CustomItemModels();

	public CustomBlockModels getCustomBlockModels() {
		return customBlockModels;
	}

	public CustomItemModels getCustomItemModels() {
		return customItemModels;
	}

	/// Baked models are keyed by BlockState now rather than by a model
	/// location, so the swap walks the block's states directly.
	public void onModelBake(ModelEvent.ModifyBakingResult event) {
		Map<BlockState, BlockStateModel> modelRegistry = event.getBakingResult()
			.blockStateModels();
		customBlockModels.forEach((block, modelFunc) -> swapModels(modelRegistry, block, modelFunc));
	}

	public void registerListeners(IEventBus modEventBus) {
		modEventBus.addListener(this::onModelBake);
	}

	public static <T extends BlockStateModel> void swapModels(Map<BlockState, BlockStateModel> modelRegistry,
		Block block, Function<BlockStateModel, T> factory) {
		block.getStateDefinition()
			.getPossibleStates()
			.forEach(state -> swapModel(modelRegistry, state, factory));
	}

	public static <T extends BlockStateModel> void swapModel(Map<BlockState, BlockStateModel> modelRegistry,
		BlockState state, Function<BlockStateModel, T> factory) {
		BlockStateModel existing = modelRegistry.get(state);
		if (existing != null)
			modelRegistry.put(state, factory.apply(existing));
	}

}
