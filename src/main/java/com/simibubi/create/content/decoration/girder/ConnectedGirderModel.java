package com.simibubi.create.content.decoration.girder;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.block.connected.CTModel;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;
import net.neoforged.neoforge.model.data.ModelProperty;

public class ConnectedGirderModel extends CTModel {

	protected static final ModelProperty<ConnectionData> CONNECTION_PROPERTY = new ModelProperty<>();

	public ConnectedGirderModel(BlockStateModel originalModel) {
		super(originalModel, new GirderCTBehaviour());
	}

	/// CTModel's collectParts does the connected-texture shifting; the bracket
	/// geometry is extra parts on top, worked out from the world here rather
	/// than stashed on ModelData first.
	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
		List<BlockStateModelPart> parts) {
		super.collectParts(level, pos, state, random, parts);
		for (Direction d : Iterate.horizontalDirections)
			if (GirderBlock.isConnected(level, pos, state, d))
				AllPartialModels.METAL_GIRDER_BRACKETS.get(d)
					.get()
					.collectParts(level, pos, state, random, parts);
	}

	private static class ConnectionData {
		boolean[] connectedFaces;

		public ConnectionData() {
			connectedFaces = new boolean[4];
			Arrays.fill(connectedFaces, false);
		}

		void setConnected(Direction face, boolean connected) {
			connectedFaces[face.get2DDataValue()] = connected;
		}

		boolean isConnected(Direction face) {
			return connectedFaces[face.get2DDataValue()];
		}
	}

}
