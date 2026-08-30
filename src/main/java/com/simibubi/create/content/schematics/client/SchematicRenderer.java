package com.simibubi.create.content.schematics.client;

import com.simibubi.create.foundation.render.DiscardingVertexConsumer;

import net.createmod.catnip.api.client.render.model.BakedModelBufferer;

import java.util.stream.StreamSupport;

import java.util.Iterator;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.level.wrapper.SchematicLevel;
import net.createmod.catnip.api.client.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.neoforged.neoforge.model.data.ModelData;

public class SchematicRenderer {

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private final Map<ChunkSectionLayer, SuperByteBuffer> bufferCache = new LinkedHashMap<>(getLayerCount());
	private boolean changed;
	protected final SchematicLevel schematic;
	private final BlockPos anchor;
	private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
	private final BitSet shouldRenderBlockEntities = new BitSet();
	private final BitSet scratchErroredBlockEntities = new BitSet();

	public SchematicRenderer(SchematicLevel world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.changed = true;

		for (var renderedBlockEntity : schematic.getRenderedBlockEntities()) {
			renderedBlockEntities.add(renderedBlockEntity);
		}
		shouldRenderBlockEntities.set(0, renderedBlockEntities.size());
	}

	public void update() {
		changed = true;
	}

	public void render(PoseStack ms, SuperRenderTypeBuffer buffers) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		if (changed)
			redraw();
		changed = false;

		bufferCache.forEach((layer, buffer) -> {
			buffer.renderInto(ms, buffers.getBuffer(layer));
		});
		scratchErroredBlockEntities.clear();
		BlockEntityRenderHelper.renderBlockEntities(renderedBlockEntities, shouldRenderBlockEntities, scratchErroredBlockEntities, null, schematic, ms, null, buffers, AnimationTickHolder.getPartialTicks());

		// Don't bother looping over errored BEs again.
		shouldRenderBlockEntities.andNot(scratchErroredBlockEntities);
	}

	protected void redraw() {
		bufferCache.clear();

		for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
			SuperByteBuffer buffer = drawLayer(layer);
			if (!buffer.isEmpty())
				bufferCache.put(layer, buffer);
		}
	}

	/// Blocks buffer for every layer in one pass now, rather than a model saying
	/// which layers it belongs to, so the geometry for the other layers is
	/// dropped as it arrives. The schematic is drawn at its own coordinates, so
	/// the anchor comes back off the positions the blocks are read from.
	protected SuperByteBuffer drawLayer(ChunkSectionLayer layer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		SchematicLevel renderWorld = schematic;
		BoundingBox bounds = renderWorld.getBounds();

		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();

		Iterator<BlockPos> positions = StreamSupport
			.stream(BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(),
				bounds.maxZ())
				.spliterator(), false)
			.map(localPos -> localPos.offset(anchor))
			.iterator();

		renderWorld.renderMode = true;
		poseStack.pushPose();
		poseStack.translate(-anchor.getX(), -anchor.getY(), -anchor.getZ());

		BakedModelBufferer.bufferBlocks(positions, new SchematicTintGetter(renderWorld), poseStack, false,
			(bufferedLayer, shade) -> bufferedLayer == layer ? sbbBuilder.unwrap(shade)
				: DiscardingVertexConsumer.INSTANCE);

		poseStack.popPose();
		renderWorld.renderMode = false;

		return sbbBuilder.end();
	}

	private static int getLayerCount() {
		return ChunkSectionLayer.values().length;
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
	}

}
