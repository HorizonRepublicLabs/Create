package com.simibubi.create.infrastructure.data;

import java.util.function.BiConsumer;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;

import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

/// Armor textures come from an equipment asset now rather than an item hook,
/// and one asset already carries both humanoid layers, which is what Create's
/// two-layer armor was drawing by hand.
public class CreateEquipmentAssetProvider extends EquipmentAssetProvider {

	public CreateEquipmentAssetProvider(PackOutput output) {
		super(output);
	}

	@Override
	protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
		output.accept(AllArmorMaterials.COPPER_ASSET, humanoid("copper_diving"));
		output.accept(AllArmorMaterials.CARDBOARD_ASSET, humanoid("cardboard"));
		output.accept(AllArmorMaterials.NETHERITE_DIVING_ASSET, humanoid("netherite_diving"));
	}

	private static EquipmentClientInfo humanoid(String name) {
		return EquipmentClientInfo.builder()
			.addHumanoidLayers(Create.asResource(name))
			.build();
	}

	@Override
	public String getName() {
		return "Create's Equipment Assets";
	}
}
