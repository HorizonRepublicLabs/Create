package com.simibubi.create.content.equipment.armor;

import java.util.Map;

import com.google.common.collect.Maps;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;

import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/// ArmorMaterial is a plain record in 26.x rather than a registry entry, and
/// the armor texture comes from an equipment asset instead of an item hook.
public class AllArmorMaterials {
	public static final ResourceKey<EquipmentAsset> COPPER_ASSET = asset("copper");
	public static final ResourceKey<EquipmentAsset> CARDBOARD_ASSET = asset("cardboard");

	/// The layered backtank draws two armor textures, which one asset carries.
	public static final ResourceKey<EquipmentAsset> NETHERITE_DIVING_ASSET = asset("netherite_diving");

	/// Netherite's own numbers, pointed at Create's diving textures. The custom
	/// armor renderer that used to supply them is gone.
	public static final ArmorMaterial NETHERITE_DIVING = new ArmorMaterial(
		37, defense(3, 6, 8, 3, 19), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F,
		ItemTags.REPAIRS_NETHERITE_ARMOR, NETHERITE_DIVING_ASSET);

	public static final ArmorMaterial COPPER = new ArmorMaterial(
		11, defense(1, 3, 4, 2, 4), 7, AllSoundEvents.COPPER_ARMOR_EQUIP.getMainEventHolder(), 0.0F, 0.0F,
		ItemTags.REPAIRS_COPPER_ARMOR, COPPER_ASSET);

	public static final ArmorMaterial CARDBOARD = new ArmorMaterial(
		5, defense(1, 1, 1, 1, 2), 4, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
		AllItemTags.CARDBOARD_PLATES.tag, CARDBOARD_ASSET);

	private static ResourceKey<EquipmentAsset> asset(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, Create.asResource(name));
	}

	private static Map<ArmorType, Integer> defense(int boots, int legs, int chest, int helmet, int body) {
		return Maps.newEnumMap(Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, legs, ArmorType.CHESTPLATE, chest,
			ArmorType.HELMET, helmet, ArmorType.BODY, body));
	}
}
