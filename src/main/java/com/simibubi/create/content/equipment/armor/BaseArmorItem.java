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

	/// The material and the slot are folded into components at construction and
	/// cannot be read back off the item, so model generation keeps them here.
	protected final ArmorMaterial armorMaterial;
	protected final ArmorType armorType;

	public BaseArmorItem(ArmorMaterial armorMaterial, ArmorType type, Properties properties, Identifier textureLoc) {
		super(properties.humanoidArmor(armorMaterial, type)
			.stacksTo(1));
		this.textureLoc = textureLoc;
		this.armorMaterial = armorMaterial;
		this.armorType = type;
	}

	public ArmorMaterial getArmorMaterial() {
		return armorMaterial;
	}

	public ArmorType getArmorType() {
		return armorType;
	}
}
