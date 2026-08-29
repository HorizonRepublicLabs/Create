package com.simibubi.create.content.fluids;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

import com.simibubi.create.foundation.model.DataDrivenModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.minecraft.util.TriState;

public class PipeAttachmentModel extends DataDrivenModel<PipeAttachmentModel.PipeModelData> {

	private boolean ao;

	public static PipeAttachmentModel withAO(BlockStateModel template) {
		return new PipeAttachmentModel(template, true);
	}

	public static PipeAttachmentModel withoutAO(BlockStateModel template) {
		return new PipeAttachmentModel(template, false);
	}

	public PipeAttachmentModel(BlockStateModel template, boolean ao) {
		super(template);
		this.ao = ao;
	}

	@Override
	protected PipeModelData gatherData(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		PipeModelData data = new PipeModelData();
		FluidTransportBehaviour transport = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		BracketedBlockEntityBehaviour bracket = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);

		if (transport != null)
			for (Direction d : Iterate.directions)
				data.putAttachment(d, transport.getRenderedRimAttachment(world, pos, state, d));
		if (bracket != null)
			data.putBracket(bracket.getBracket());

		data.setEncased(FluidPipeBlock.shouldDrawCasing(world, pos, state));
		return data;
	}

	@Override
	protected List<BakedQuad> transformQuads(List<BakedQuad> base, PipeModelData pipeData, BlockState state,
		RandomSource rand, Direction side) {
		List<BakedQuad> quads = new ArrayList<>(base);
		addQuads(quads, state, side, rand, pipeData);
		return quads;
	}

	/// Collects a model's quads for one side; geometry comes from parts now.
	private static void collectInto(List<BakedQuad> quads, BlockStateModel model, RandomSource rand,
		Direction side) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(rand, parts);
		for (BlockStateModelPart part : parts)
			quads.addAll(part.getQuads(side));
	}

	private void addQuads(List<BakedQuad> quads, BlockState state, Direction side, RandomSource rand,
		PipeModelData pipeData) {
		BlockStateModel bracket = pipeData.getBracket();
		if (bracket != null)
			collectInto(quads, bracket, rand, side);
		for (Direction d : Iterate.directions) {
			AttachmentTypes type = pipeData.getAttachment(d);
			for (ComponentPartials partial : type.partials) {
				collectInto(quads, AllPartialModels.PIPE_ATTACHMENTS.get(partial)
					.get(d)
					.get(), rand, side);
			}
		}
		if (pipeData.isEncased())
			collectInto(quads, AllPartialModels.FLUID_PIPE_CASING.get(), rand, side);
	}

	public static class PipeModelData {
		private AttachmentTypes[] attachments;
		private boolean encased;
		private BlockStateModel bracket;

		public PipeModelData() {
			attachments = new AttachmentTypes[6];
			Arrays.fill(attachments, AttachmentTypes.NONE);
		}

		public void putBracket(BlockState state) {
			if (state != null) {
				this.bracket = Minecraft.getInstance()
					.getModelManager()
					.getBlockStateModelSet()
					.get(state);
			}
		}

		public BlockStateModel getBracket() {
			return bracket;
		}

		public void putAttachment(Direction face, AttachmentTypes rim) {
			attachments[face.get3DDataValue()] = rim;
		}

		public AttachmentTypes getAttachment(Direction face) {
			return attachments[face.get3DDataValue()];
		}

		public void setEncased(boolean encased) {
			this.encased = encased;
		}

		public boolean isEncased() {
			return encased;
		}
	}

}
