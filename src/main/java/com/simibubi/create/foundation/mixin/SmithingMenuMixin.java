package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {
	@Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;awardUsedRecipes(Lnet/minecraft/world/entity/player/Player;Ljava/util/List;)V"))
	private void create$awardAdvancementWhenTrimmingCardboardArmor(Player player, ItemStack stack, CallbackInfo ci) {
		if (AllItems.CARDBOARD_HELMET.isIn(stack) ||
			AllItems.CARDBOARD_CHESTPLATE.isIn(stack) ||
			AllItems.CARDBOARD_LEGGINGS.isIn(stack) ||
			AllItems.CARDBOARD_BOOTS.isIn(stack)) {
			AllAdvancements.CARDBOARD_ARMOR_TRIM.awardTo(player);
		}
	}

	// Only add enchantments to the backtank if it supports them.
	// 26.2 assembles the result inside a lambda, so read it back off the result slot instead.
	@Inject(method = "createResult", at = @At("TAIL"))
	private void create$preventUnbreakingOnBacktanks(CallbackInfo ci) {
		SmithingMenu self = (SmithingMenu) (Object) this;
		ItemStack result = self.getSlot(self.getResultSlot()).getItem();
		if (AllItems.COPPER_BACKTANK.isIn(result) || AllItems.NETHERITE_BACKTANK.isIn(result)) {
			ItemEnchantments.Mutable mutableEnchantments =
				new ItemEnchantments.Mutable(result.getTagEnchantments());
			mutableEnchantments.removeIf(enchant -> !result.supportsEnchantment(enchant));
			result.set(DataComponents.ENCHANTMENTS, mutableEnchantments.toImmutable());
		}
	}
}
