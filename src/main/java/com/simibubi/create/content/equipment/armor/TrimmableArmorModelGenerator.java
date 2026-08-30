package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/// Trim models used to be overrides hung off one generated model, keyed by a
/// predicate. They are separate models selected by trim material now, and the
/// generator builds the whole set, so Create only has to name the textures its
/// own trims live under.
public class TrimmableArmorModelGenerator {

	public static <T extends BaseArmorItem> void generate(DataGenContext<Item, T> c, RegistrateItemModelGenerator p) {
		T item = c.get();
		p.generateTrimmableItem(item, item.getArmorMaterial()
			.assetId(), slotTrimPrefix(item), false);
	}

	private static Identifier slotTrimPrefix(BaseArmorItem item) {
		String slot = item.getArmorType()
			.getName();
		if (item.getArmorMaterial() == AllArmorMaterials.CARDBOARD)
			return Create.asResource("trims/items/card_" + slot + "_trim");
		return ItemModelGenerators.prefixForSlotTrim(slot);
	}
}
