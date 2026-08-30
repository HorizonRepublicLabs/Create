package com.simibubi.create.foundation.item.render;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

/// Item models are no longer block models that can be wrapped and flagged for
/// custom rendering, so the model a hand-drawn item starts from is named
/// directly and resolved the same way its moving parts are.
public class CustomRenderedItemModel {

	private final PartialModel base;

	public CustomRenderedItemModel(Identifier baseModel) {
		this.base = PartialModel.of(baseModel);
	}

	public BlockStateModel getOriginalModel() {
		return base.get();
	}
}
