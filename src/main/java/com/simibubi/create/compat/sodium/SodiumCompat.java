package com.simibubi.create.compat.sodium;

import com.simibubi.create.Create;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.TextureAtlas;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Fixes the Mechanical Saw's sprite and Factory Gauge's sprite
 */
public class SodiumCompat {
	public static final Identifier SAW_TEXTURE = Create.asResource("block/saw_reversed");
	public static final Identifier FACTORY_PANEL_TEXTURE = Create.asResource("block/factory_panel_connections_animated");

	public static void init(IEventBus modEventBus, IEventBus neoEventBus) {
		Minecraft mc = Minecraft.getInstance();
		neoEventBus.addListener((RenderLevelStageEvent.AfterOpaqueFeatures event) -> {
			{
				TextureAtlas atlas = mc.getAtlasManager()
					.getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS);
				TextureAtlasSprite sawSprite = atlas.getSprite(SAW_TEXTURE);
				SpriteUtil.INSTANCE.markSpriteActive(sawSprite);

				TextureAtlasSprite factoryPanelSprite = atlas.getSprite(FACTORY_PANEL_TEXTURE);
				SpriteUtil.INSTANCE.markSpriteActive(factoryPanelSprite);
			}
		});
	}
}
