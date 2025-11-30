package com.simibubi.create.infrastructure.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public record LayeredOreConfiguration(
        List<LayerPattern> layerPatterns, int size, float discardChanceOnAirExposure)
        implements FeatureConfiguration {
    public static final Codec<LayeredOreConfiguration> CODEC =
            RecordCodecBuilder.create(instance -> {
                return instance.group(
                                Codec.list(LayerPattern.CODEC)
                                        .fieldOf("layer_patterns")
                                        .forGetter(config -> config.layerPatterns),
                                Codec.intRange(0, 64)
                                        .fieldOf("size")
                                        .forGetter(config -> config.size),
                                Codec.floatRange(0.0F, 1.0F)
                                        .fieldOf("discard_chance_on_air_exposure")
                                        .forGetter(config -> config.discardChanceOnAirExposure))
                        .apply(instance, LayeredOreConfiguration::new);
            });
}
