package com.simibubi.create.foundation.item.render;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/// An item that draws itself is an item model of its own now rather than a
/// block model flagged for custom rendering. The base model still supplies the
/// transforms and particle, and Create's renderer draws the geometry -- base
/// included, since some of them swap it out by display context.
public class CustomRenderedItemModelWrapper implements ItemModel {

	private final CustomRenderedSpecialRenderer renderer;
	private final ModelRenderProperties properties;
	private final Matrix4fc transformation;

	public CustomRenderedItemModelWrapper(CustomRenderedSpecialRenderer renderer, ModelRenderProperties properties,
		Matrix4fc transformation) {
		this.renderer = renderer;
		this.properties = properties;
		this.transformation = transformation;
	}

	@Override
	public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver,
		ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		output.appendModelIdentityElement(this);
		ItemStackRenderState.LayerRenderState layer = output.newLayer();

		if (item.hasFoil()) {
			layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
			output.setAnimated();
		}

		layer.setLocalTransform(transformation);
		layer.setupSpecialModel(renderer, renderer.extractArgument(item));
		properties.applyToLayer(layer, displayContext);
	}

	/// The renderer is named rather than carried, so a model can be written for
	/// an item whose renderer only exists on the client.
	public record Unbaked(Identifier base, Identifier renderer,
		Optional<Transformation> transformation) implements ItemModel.Unbaked {

		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Identifier.CODEC.fieldOf("base")
				.forGetter(Unbaked::base),
			Identifier.CODEC.fieldOf("renderer")
				.forGetter(Unbaked::renderer),
			Transformation.EXTENDED_CODEC.optionalFieldOf("transformation")
				.forGetter(Unbaked::transformation))
			.apply(i, Unbaked::new));

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(base);
		}

		@Override
		public ItemModel bake(ItemModel.BakingContext context, Matrix4fc parentTransformation) {
			Matrix4fc modelTransform = Transformation.compose(parentTransformation, transformation);
			CustomRenderedItemModelRenderer itemRenderer = CustomRenderedItems.get(renderer);
			if (itemRenderer == null)
				return context.missingItemModel(modelTransform);

			ModelBaker baker = context.blockModelBaker();
			ResolvedModel model = baker.getModel(base);
			ModelRenderProperties properties =
				ModelRenderProperties.fromResolvedModel(baker, model, model.getTopTextureSlots());

			return new CustomRenderedItemModelWrapper(new CustomRenderedSpecialRenderer(itemRenderer), properties,
				modelTransform);
		}
	}
}
