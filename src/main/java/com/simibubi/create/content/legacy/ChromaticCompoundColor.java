package com.simibubi.create.content.legacy;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.client.multiplayer.ClientLevel;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.mojang.serialization.Codec;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.ARGB;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/// Item tints are declared by the model as a source per layer rather than one
/// handler asked for a layer index, so each layer names its own source.
public record ChromaticCompoundColor(int layer) implements ItemTintSource {

	public static final MapCodec<ChromaticCompoundColor> CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(Codec.INT.fieldOf("layer")
			.forGetter(ChromaticCompoundColor::layer))
		.apply(i, ChromaticCompoundColor::new));

	@Override
	public MapCodec<? extends ItemTintSource> type() {
		return CODEC;
	}

	@Override
	public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
		Minecraft mc = Minecraft.getInstance();
		float pt = AnimationTickHolder.getPartialTicks();
		float progress = (float) ((mc.player.getViewYRot(pt)) / 180 * Math.PI) + (AnimationTickHolder.getRenderTime() / 10f);
		if (layer == 0)
			return Color.mixColors(
				ARGB.color(110, 87, 115),
				ARGB.color(107, 48, 116),
				(Mth.sin(progress) + 1) / 2
			);
		if (layer == 1)
			return Color.mixColors(
				ARGB.color(212, 93, 121),
				ARGB.color(110, 87, 115),
				(Mth.sin((float) (progress + Math.PI)) + 1) / 2
			);
		if (layer == 2)
			return Color.mixColors(
				ARGB.color(234, 144, 133),
				ARGB.color(212, 93, 121),
				(Mth.sin((float) (progress * 1.5f + Math.PI)) + 1) / 2
			);
		return 0;
	}
}
