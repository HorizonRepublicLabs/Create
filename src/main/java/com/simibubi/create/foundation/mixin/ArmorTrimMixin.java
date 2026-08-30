package com.simibubi.create.foundation.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimPattern;

/// The inner and outer texture hooks collapsed into one asset lookup that names
/// the equipment rather than the armour material, so the cardboard swap keys off
/// the asset and reads the layer from the prefix.
@Mixin(ArmorTrim.class)
public abstract class ArmorTrimMixin {
	@Shadow
	@Final
	private Holder<TrimPattern> pattern;

	@Unique
	private final Function<Boolean, Identifier> create$textureCardboard = Util.memoize(leggings -> {
		String assetPath = pattern.value()
			.assetId()
			.getPath();
		return Create.asResource("trims/models/armor/card_" + assetPath + (leggings ? "_leggings" : ""));
	});

	@Inject(method = "layerAssetId", at = @At("HEAD"), cancellable = true)
	private void create$swapTexturesForCardboardTrims(String layerAssetPrefix,
		ResourceKey<EquipmentAsset> equipmentAsset, CallbackInfoReturnable<Identifier> cir) {
		if (equipmentAsset == AllArmorMaterials.CARDBOARD_ASSET)
			cir.setReturnValue(create$textureCardboard.apply(layerAssetPrefix.endsWith("humanoid_leggings")));
	}
}
