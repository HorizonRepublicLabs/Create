package com.simibubi.create.content.kinetics.simpleRelays;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import java.util.Collections;
import java.util.List;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.impl.neoforge.render.VirtualRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class BracketedKineticBlockModel extends DelegateBlockStateModel {


	public BracketedKineticBlockModel(BlockStateModel template) {
		super(template);
	}

	/// The bracket replaces the shaft's own geometry entirely when one is
	/// attached, so this collects the bracket's parts instead of the delegate's.
	/// Virtual render worlds keep the plain model.
	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
		List<BlockStateModelPart> parts) {
		ModelData data = level.getModelData(pos);
		if (VirtualRenderHelper.isVirtual(data)) {
			super.collectParts(level, pos, state, random, parts);
			return;
		}

		BracketedBlockEntityBehaviour attachmentBehaviour =
			BlockEntityBehaviour.get(level, pos, BracketedBlockEntityBehaviour.TYPE);
		BlockState bracketState = attachmentBehaviour == null ? null : attachmentBehaviour.getBracket();
		if (bracketState != null)
			Minecraft.getInstance()
				.getModelManager()
				.getBlockStateModelSet()
				.get(bracketState)
				.collectParts(level, pos, bracketState, random, parts);
	}


}
