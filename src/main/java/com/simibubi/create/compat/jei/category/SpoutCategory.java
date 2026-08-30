package com.simibubi.create.compat.jei.category;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import net.neoforged.neoforge.capabilities.Capabilities;

import net.neoforged.neoforge.transfer.transaction.Transaction;

import net.neoforged.neoforge.transfer.fluid.FluidResource;

import net.neoforged.neoforge.transfer.ResourceHandler;

import net.neoforged.neoforge.transfer.access.ItemAccess;

import com.simibubi.create.foundation.fluid.FluidHelper;

import com.simibubi.create.foundation.fluid.FluidCaps;

import net.minecraft.core.registries.Registries;

import net.minecraft.resources.ResourceKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@ParametersAreNonnullByDefault
public class SpoutCategory extends CreateRecipeCategory<FillingRecipe> {

	private final AnimatedSpout spout = new AnimatedSpout();

	public SpoutCategory(Info<FillingRecipe> info) {
		super(info);
	}

	public static void consumeRecipes(Consumer<RecipeHolder<FillingRecipe>> consumer, IIngredientManager ingredientManager) {
		Collection<FluidStack> fluidStacks = ingredientManager.getAllIngredients(NeoForgeTypes.FLUID_STACK);
		for (ItemStack stack : ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
			if (PotionFluidHandler.isPotionItem(stack)) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
				Identifier id = Create.asResource("potions");
				SizedFluidIngredient fluidIngredient = new SizedFluidIngredient(
					DataComponentFluidIngredient.of(false, fluidFromPotionItem), fluidFromPotionItem.getAmount());
				FillingRecipe recipe = new StandardProcessingRecipe.Builder<>(FillingRecipe::new, id)
						.withItemIngredients(bottle)
					.withFluidIngredients(fluidIngredient)
						.withSingleItemOutput(stack)
						.build();
				consumer.accept(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe));
				continue;
			}

			IFluidHandler capability = FluidCaps.of(stack);
			if (capability == null)
				continue;

			int numTanks = capability.getTanks();
			FluidStack existingFluid = numTanks == 1 ? capability.getFluidInTank(0) : FluidStack.EMPTY;

			for (FluidStack fluidStack : fluidStacks) {
				// Hoist the fluid equality check to avoid the work of copying the stack + populating capabilities
				// when most fluids will not match
				if (numTanks == 1 && (!existingFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(existingFluid, fluidStack)))
					continue;

				// The container is reached through an access to the stack, which
				// holds what it turned into once the fill goes through.
				ItemStack copy = stack.copy();
				ItemAccess access = ItemAccess.forStack(copy);
				ResourceHandler<FluidResource> fhi = access.getCapability(Capabilities.Fluid.ITEM);
				if (fhi != null) {
					if (!GenericItemFilling.isFluidHandlerValid(copy, IFluidHandler.of(fhi)))
						continue;
					FluidStack fluidCopy = fluidStack.copy();
					fluidCopy.setAmount(1000);
					try (Transaction transaction = Transaction.openRoot()) {
						fhi.insert(FluidResource.of(fluidCopy), 1000, transaction);
						transaction.commit();
					}
					ItemStack container = access.getResource()
						.toStack();
					if (ItemHelper.sameItem(container, copy))
						continue;
					if (container.isEmpty())
						continue;

					Ingredient bucket = Ingredient.of(stack.getItem());
					Identifier itemName = RegisteredObjectsHelper.getKeyOrThrow(stack.getItem());
					Identifier fluidName = RegisteredObjectsHelper.getKeyOrThrow(fluidCopy.getFluid());
					Identifier id = Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
							+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath());
					SizedFluidIngredient fluidIngredient = new SizedFluidIngredient(
						DataComponentFluidIngredient.of(false, fluidCopy), fluidCopy.getAmount());
					FillingRecipe recipe = new StandardProcessingRecipe.Builder<>(FillingRecipe::new, id)
							.withItemIngredients(bucket)
						.withFluidIngredients(fluidIngredient)
							.withSingleItemOutput(container)
							.build();
					consumer.accept(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe));
				}
			}
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FillingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.placementInfo()
			.ingredients().get(0));

		addFluidSlot(builder, 27, 32, recipe.getRequiredFluid());

		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(getResultItem(recipe));
	}

	@Override
	public void draw(FillingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
		spout.withFluids(FluidHelper.matchingStacks(recipe.getRequiredFluid()))
			.draw(graphics, getBackground().getWidth() / 2 - 13, 22);
	}

}
