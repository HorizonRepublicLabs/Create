package com.simibubi.create.content.equipment.blueprint;

import com.simibubi.create.foundation.recipe.RecipeLookup;

import com.simibubi.create.foundation.networking.CreatePacketPayload;

import net.createmod.catnip.api.network.SelfHandlingPayload;

import com.simibubi.create.AllPackets;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record BlueprintAssignCompleteRecipePacket(Identifier recipeId) implements SelfHandlingPayload, CreatePacketPayload {
	public static final StreamCodec<ByteBuf, com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket> STREAM_CODEC = Identifier.STREAM_CODEC.map(
			com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket::new, com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket::recipeId
	);

	@Override
	public void handle(ServerPlayer player) {
		if (player.containerMenu instanceof BlueprintMenu c) {
			// RecipeManager left Level; recipes are looked up by id through
			// Create's own helper.
			RecipeLookup.byId(player.level(), recipeId)
				.ifPresent(r -> BlueprintItem.assignCompleteRecipe(c.player.level(), c.ghostInventory, r.value()));
		}
	}

	@Override
	public AllPackets getTypeProvider() {
		return AllPackets.BLUEPRINT_COMPLETE_RECIPE;
	}
}
