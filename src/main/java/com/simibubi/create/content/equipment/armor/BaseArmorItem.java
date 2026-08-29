package com.simibubi.create.content.equipment.armor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/// ArmorItem is gone; armor is a plain Item carrying equippable and attribute
/// components. The texture that getArmorTexture used to supply now comes from
/// the material's equipment asset.
public class BaseArmorItem extends Item {
	protected final Identifier textureLoc;

	public BaseArmorItem(ArmorMaterial armorMaterial, ArmorType type, Properties properties, Identifier textureLoc) {
		super(properties.humanoidArmor(armorMaterial, type)
			.stacksTo(1));
		this.textureLoc = textureLoc;
	}
}
