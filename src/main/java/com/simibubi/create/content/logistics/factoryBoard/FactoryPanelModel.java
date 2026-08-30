package com.simibubi.create.content.logistics.factoryBoard;

import net.minecraft.world.level.BlockGetter;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import com.simibubi.create.foundation.model.DataDrivenModel;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelState;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelType;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class FactoryPanelModel extends DataDrivenModel<FactoryPanelModel.FactoryPanelModelData> {


	public FactoryPanelModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Override
	protected FactoryPanelModelData gatherData(BlockGetter world, BlockPos pos, BlockState state) {
		FactoryPanelModelData data = new FactoryPanelModelData();
		for (PanelSlot slot : PanelSlot.values()) {
			FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(world, new FactoryPanelPosition(pos, slot));
			if (behaviour == null)
				continue;
			data.states.put(slot, behaviour.count == 0 ? PanelState.PASSIVE : PanelState.ACTIVE);
			data.type = behaviour.panelBE().restocker ? PanelType.PACKAGER : PanelType.NETWORK;
		}
		data.ponder = world instanceof PonderLevel;
		return data;
	}

	@Override
	protected List<BakedQuad> transformQuads(List<BakedQuad> base, FactoryPanelModelData modelData,
		BlockState state, RandomSource rand, Direction side) {
		if (side != null)
			return List.of();
		List<BakedQuad> quads = new ArrayList<>(base);
		for (PanelSlot panelSlot : PanelSlot.values())
			if (modelData.states.containsKey(panelSlot))
				addPanel(quads, state, panelSlot, modelData.type, modelData.states.get(panelSlot), rand,
					modelData.ponder);
		return quads;
	}

	public void addPanel(List<BakedQuad> quads, BlockState state, PanelSlot slot, PanelType type, PanelState panelState,
		RandomSource rand, boolean ponder) {
		PartialModel factoryPanel = panelState == PanelState.PASSIVE
			? type == PanelType.NETWORK ? AllPartialModels.FACTORY_PANEL : AllPartialModels.FACTORY_PANEL_RESTOCKER
			: type == PanelType.NETWORK ? AllPartialModels.FACTORY_PANEL_WITH_BULB
				: AllPartialModels.FACTORY_PANEL_RESTOCKER_WITH_BULB;

		List<BlockStateModelPart> panelParts = new ArrayList<>();
		factoryPanel.get()
			.collectParts(rand, panelParts);
		List<BakedQuad> quadsToAdd = new ArrayList<>();
		for (BlockStateModelPart part : panelParts)
			quadsToAdd.addAll(part.getQuads(null));

		float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
		float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);

		for (BakedQuad bakedQuad : quadsToAdd) {
			Vec3 quadNormal = Vec3.atLowerCornerOf(bakedQuad.direction()
				.getUnitVec3i());
			quadNormal = VecHelper.rotate(quadNormal, 180, Axis.Y);
			quadNormal = VecHelper.rotate(quadNormal, xRot + 90, Axis.X);
			quadNormal = VecHelper.rotate(quadNormal, yRot, Axis.Y);

			// The per-vertex normal the old code computed was thrown away -- it
			// wrote a constant -- so only the positions are carried over.
			BakedQuadHelper.Editor edit = BakedQuadHelper.edit(bakedQuad);
			for (int i = 0; i < 4; i++) {
				Vec3 vertex = edit.getXYZ(i);
				vertex = vertex.add(slot.xOffset * .5, 0, slot.yOffset * .5);
				vertex = VecHelper.rotateCentered(vertex, 180, Axis.Y);
				vertex = VecHelper.rotateCentered(vertex, xRot + 90, Axis.X);
				vertex = VecHelper.rotateCentered(vertex, yRot, Axis.Y);
				edit.setXYZ(i, vertex);
			}

			quads.add(edit.build());
		}
	}

	public static class FactoryPanelModelData {
		public PanelType type;
		public EnumMap<PanelSlot, PanelState> states = new EnumMap<>(PanelSlot.class);
		private boolean ponder;
	}

}
