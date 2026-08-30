package com.simibubi.create.foundation.data;

import java.util.function.Supplier;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/// The connected-texture wrapper hands back a BlockStateModel, and a method with
/// that in its signature cannot be verified on a dedicated server, so it lives in
/// a class the server never loads.
@OnlyIn(Dist.CLIENT)
public class CreateRegistrateClientHooks {
	public static void registerCTBehaviour(Block entry, Supplier<ConnectedTextureBehaviour> behaviorSupplier) {
		ConnectedTextureBehaviour behavior = behaviorSupplier.get();
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), model -> new CTModel(model, behavior));
	}
}
