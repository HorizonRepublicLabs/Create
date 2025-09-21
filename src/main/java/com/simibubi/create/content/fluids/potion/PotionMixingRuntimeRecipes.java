package com.simibubi.create.content.fluids.potion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Unmodifiable;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.mixin.accessor.PotionBrewingAccessor;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;

public class PotionMixingRuntimeRecipes {
	public static final List<Item> SUPPORTED_CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

	@Unmodifiable
	public static List<RecipeHolder<MixingRecipe>> createRecipes(PotionBrewing potionBrewing, RegistryAccess registryAccess) {
		if (!AllConfigs.server().recipes.allowBrewingInMixer.get())
			return List.of();

		List<RecipeHolder<MixingRecipe>> mixingRecipes = new ArrayList<>();

		List<Item> allowedSupportedContainers = new ArrayList<>();
		List<ItemStack> supportedContainerStacks = new ArrayList<>();
		for (Item container : SUPPORTED_CONTAINERS) {
			ItemStack stack = new ItemStack(container);
			supportedContainerStacks.add(stack);
			if (((PotionBrewingAccessor) potionBrewing).create$isContainer(stack)) {
				allowedSupportedContainers.add(container);
			}
		}

		for (Item container : allowedSupportedContainers) {
			BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
			for (PotionBrewing.Mix<Potion> mix : ((PotionBrewingAccessor) potionBrewing).create$getPotionMixes()) {
				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.from()), bottleType, 1000);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.to()), bottleType, 1000);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla", mix.ingredient(), fromFluid, toFluid));
			}
		}

		for (PotionBrewing.Mix<Item> mix : ((PotionBrewingAccessor) potionBrewing).create$getContainerMixes()) {
			Item from = mix.from().value();
			if (!allowedSupportedContainers.contains(from)) {
				continue;
			}
			Item to = mix.to().value();
			if (!allowedSupportedContainers.contains(to)) {
				continue;
			}
			BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(from);
			BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(to);
			Ingredient ingredient = mix.ingredient();

			List<Reference<Potion>> potions = registryAccess
				.lookupOrThrow(Registries.POTION)
				.listElements()
				.toList();

			for (Reference<Potion> potion : potions) {
				FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), fromBottleType, 1000);
				FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), toBottleType, 1000);

				mixingRecipes.add(createRecipe("potion_mixing_vanilla", ingredient, fromFluid, toFluid));
			}
		}

		for (IBrewingRecipe recipe : potionBrewing.getRecipes()) {
			if (recipe instanceof BrewingRecipe recipeImpl) {
				ItemStack output = recipeImpl.getOutput();
				if (!SUPPORTED_CONTAINERS.contains(output.getItem())) {
					continue;
				}

				Ingredient input = recipeImpl.getInput();
				Ingredient ingredient = recipeImpl.getIngredient();
				FluidStack outputFluid = null;
				for (ItemStack stack : supportedContainerStacks) {
					if (input.test(stack)) {
						ItemStack[] stacks = input.getItems();
						if (stacks.length == 0) {
							continue;
						}
						FluidStack inputFluid = PotionFluidHandler.getFluidFromPotionItem(stacks[0]);
						inputFluid.setAmount(1000);
						if (outputFluid == null) {
							outputFluid = PotionFluidHandler.getFluidFromPotionItem(output);
						}
						outputFluid.setAmount(1000);
						mixingRecipes.add(createRecipe("potion_mixing_modded", ingredient, inputFluid, outputFluid));
					}
				}
			}
		}

		return Collections.unmodifiableList(mixingRecipes);
	}

	private static RecipeHolder<MixingRecipe> createRecipe(String id, Ingredient ingredient, FluidStack fromFluid, FluidStack toFluid) {
		String fromLoc = getLoc(fromFluid);
		String toLoc = getLoc(toFluid);

		BottleType fromBottleType = fromFluid.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
		BottleType toBottleType = toFluid.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.SPLASH);

		ResourceLocation recipeId = Create.asResource("runtime_generated/" + id + "/" + fromBottleType.getSerializedName() + "_" + fromLoc + "_to_" + toBottleType.getSerializedName() + "_" + toLoc);
		MixingRecipe recipe = new Builder<>(MixingRecipe::new, recipeId)
				.require(ingredient)
				.require(FluidIngredient.fromFluidStack(fromFluid))
				.output(toFluid)
				.requiresHeat(HeatCondition.HEATED)
				.build();

		return new RecipeHolder<>(recipeId, recipe);
	}

	private static String getLoc(FluidStack stack) {
		PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

		if (contents != null && contents.potion().isPresent()) {
			return BuiltInRegistries.POTION.getKey(contents.potion().get().value()).toString().replace(":", "_");
		} else {
			return BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString().replace(":", "_");
		}
	}
}
