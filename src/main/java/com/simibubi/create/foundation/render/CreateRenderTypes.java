package com.simibubi.create.foundation.render;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.simibubi.create.Create;

import net.minecraft.util.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

/// 26.x replaced the RenderStateShard composite-state system with
/// RenderSetup built on top of a RenderPipeline, so the shader, blend and cull
/// state that used to be assembled here now comes from the pipeline.
public class CreateRenderTypes {

	private static final RenderType ENTITY_SOLID_BLOCK_MIPPED =
		RenderType.create(createLayerName("entity_solid_block_mipped"),
			RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
				.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
				.useLightmap()
				.useOverlay()
				.affectsCrumbling()
				.createRenderSetup());

	private static final RenderType ENTITY_CUTOUT_BLOCK_MIPPED =
		RenderType.create(createLayerName("entity_cutout_block_mipped"),
			RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
				.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
				.useLightmap()
				.useOverlay()
				.affectsCrumbling()
				.createRenderSetup());

	private static final RenderType ENTITY_TRANSLUCENT_BLOCK_MIPPED =
		RenderType.create(createLayerName("entity_translucent_block_mipped"),
			RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
				.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
				.useLightmap()
				.useOverlay()
				.sortOnUpload()
				.createRenderSetup());

	/// Was an explicit ADDITIVE_TRANSPARENCY + NO_CULL composite. The emissive
	/// entity pipeline is the closest stock equivalent; worth a look in game.
	private static final RenderType ADDITIVE =
		RenderType.create(createLayerName("additive"),
			RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE)
				.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
				.useLightmap()
				.useOverlay()
				.createRenderSetup());

	// FIXME: Create's glowing shader is still written against the pre-26.x
	// ShaderInstance uniform layout, which the pipeline system replaced with
	// bind groups. Until the GLSL in assets/create/shaders/core is ported and
	// registered through RegisterRenderPipelinesEvent, these fall back to the
	// stock entity pipelines: items render correctly but without the glow.
	private static final RenderType ITEM_GLOWING_SOLID =
		RenderType.create(createLayerName("item_glowing_solid"),
			RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
				.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
				.useLightmap()
				.useOverlay()
				.createRenderSetup());

	private static final RenderType ITEM_GLOWING_TRANSLUCENT =
		RenderType.create(createLayerName("item_glowing_translucent"),
			RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
				.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
				.useLightmap()
				.useOverlay()
				.sortOnUpload()
				.createRenderSetup());

	private static final Function<Identifier, RenderType> CHAIN = Util.memoize(texture ->
		RenderType.create("chain_conveyor_chain",
			RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
				.withTexture("Sampler0", texture)
				.useLightmap()
				.useOverlay()
				.createRenderSetup()));

	public static final BiFunction<Identifier, Boolean, RenderType> TRAIN_MAP = Util.memoize(CreateRenderTypes::getTrainMap);

	private static RenderType getTrainMap(Identifier location, boolean linearFiltering) {
		return RenderType.create("create_train_map",
			RenderSetup.builder(RenderPipelines.TEXT)
				.withTexture("Sampler0", location)
				.useLightmap()
				.createRenderSetup());
	}

	public static RenderType entitySolidBlockMipped() {
		return ENTITY_SOLID_BLOCK_MIPPED;
	}

	public static RenderType entityCutoutBlockMipped() {
		return ENTITY_CUTOUT_BLOCK_MIPPED;
	}

	public static RenderType entityTranslucentBlockMipped() {
		return ENTITY_TRANSLUCENT_BLOCK_MIPPED;
	}

	/// Contraptions and other moving blocks draw with the mipped entity
	/// variants, which is what the old moving-block types resolved to.
	public static RenderType solidMovingBlock() {
		return ENTITY_SOLID_BLOCK_MIPPED;
	}

	public static RenderType cutoutMovingBlock() {
		return ENTITY_CUTOUT_BLOCK_MIPPED;
	}

	public static RenderType translucentMovingBlock() {
		return ENTITY_TRANSLUCENT_BLOCK_MIPPED;
	}

	public static RenderType additive() {
		return ADDITIVE;
	}

	public static RenderType itemGlowingSolid() {
		return ITEM_GLOWING_SOLID;
	}

	public static RenderType itemGlowingTranslucent() {
		return ITEM_GLOWING_TRANSLUCENT;
	}

	public static RenderType chain(Identifier location) {
		return CHAIN.apply(location);
	}

	private static String createLayerName(String name) {
		return Create.ID + ":" + name;
	}

	private CreateRenderTypes() {}
}
