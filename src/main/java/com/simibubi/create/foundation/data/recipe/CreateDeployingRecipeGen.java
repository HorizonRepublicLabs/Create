package com.simibubi.create.foundation.data.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.DeployingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

/**
 * Create's own Data Generation for Deploying recipes
 * @see DeployingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateDeployingRecipeGen extends DeployingRecipeGen {

	GeneratedRecipe COPPER_TILES = copperChain(AllBlocks.COPPER_TILES);
	GeneratedRecipe COPPER_SHINGLES = copperChain(AllBlocks.COPPER_SHINGLES);

	GeneratedRecipe

	COGWHEEL = create("cogwheel", b -> b.require(I.shaft())
		.require(I.planks())
		.output(I.cog())),

	LARGE_COGWHEEL = create("large_cogwheel", b -> b.require(I.cog())
		.require(I.planks())
		.output(I.largeCog()));

	GeneratedRecipe

		COPPER_BLOCK = oxidizationChain(
		List.of(() -> Blocks.COPPER_BLOCK.weathering().unaffected(), () -> Blocks.COPPER_BLOCK.weathering().exposed(), () -> Blocks.COPPER_BLOCK.weathering().weathered(), () -> Blocks.COPPER_BLOCK.weathering().oxidized()),
		List.of(() -> Blocks.COPPER_BLOCK.waxed().unaffected(), () -> Blocks.COPPER_BLOCK.waxed().exposed(), () -> Blocks.COPPER_BLOCK.waxed().weathered(), () -> Blocks.COPPER_BLOCK.waxed().oxidized())),

	COPPER_BULB = oxidizationChain(
		List.of(() -> Blocks.COPPER_BULB.weathering().unaffected(), () -> Blocks.COPPER_BULB.weathering().exposed(), () -> Blocks.COPPER_BULB.weathering().weathered(), () -> Blocks.COPPER_BULB.weathering().oxidized()),
		List.of(() -> Blocks.COPPER_BULB.waxed().unaffected(), () -> Blocks.COPPER_BULB.waxed().exposed(), () -> Blocks.COPPER_BULB.waxed().weathered(), () -> Blocks.COPPER_BULB.waxed().oxidized())),

	CHISELED_COPPER = oxidizationChain(
		List.of(() -> Blocks.CHISELED_COPPER.weathering().unaffected(), () -> Blocks.CHISELED_COPPER.weathering().exposed(), () -> Blocks.CHISELED_COPPER.weathering().weathered(), () -> Blocks.CHISELED_COPPER.weathering().oxidized()),
		List.of(() -> Blocks.CHISELED_COPPER.waxed().unaffected(), () -> Blocks.CHISELED_COPPER.waxed().exposed(), () -> Blocks.CHISELED_COPPER.waxed().weathered(), () -> Blocks.CHISELED_COPPER.waxed().oxidized())),

	COPPER_GRATE = oxidizationChain(
		List.of(() -> Blocks.COPPER_GRATE.weathering().unaffected(), () -> Blocks.COPPER_GRATE.weathering().exposed(), () -> Blocks.COPPER_GRATE.weathering().weathered(), () -> Blocks.COPPER_GRATE.weathering().oxidized()),
		List.of(() -> Blocks.COPPER_GRATE.waxed().unaffected(), () -> Blocks.COPPER_GRATE.waxed().exposed(), () -> Blocks.COPPER_GRATE.waxed().weathered(), () -> Blocks.COPPER_GRATE.waxed().oxidized())),

	COPPER_DOOR = oxidizationChain(
		List.of(() -> Blocks.COPPER_DOOR.weathering().unaffected(), () -> Blocks.COPPER_DOOR.weathering().exposed(), () -> Blocks.COPPER_DOOR.weathering().weathered(), () -> Blocks.COPPER_DOOR.weathering().oxidized()),
		List.of(() -> Blocks.COPPER_DOOR.waxed().unaffected(), () -> Blocks.COPPER_DOOR.waxed().exposed(), () -> Blocks.COPPER_DOOR.waxed().weathered(), () -> Blocks.COPPER_DOOR.waxed().oxidized())),

	COPPER_TRAPDOOR = oxidizationChain(
		List.of(() -> Blocks.COPPER_TRAPDOOR.weathering().unaffected(), () -> Blocks.COPPER_TRAPDOOR.weathering().exposed(), () -> Blocks.COPPER_TRAPDOOR.weathering().weathered(), () -> Blocks.COPPER_TRAPDOOR.weathering().oxidized()),
		List.of(() -> Blocks.COPPER_TRAPDOOR.waxed().unaffected(), () -> Blocks.COPPER_TRAPDOOR.waxed().exposed(), () -> Blocks.COPPER_TRAPDOOR.waxed().weathered(), () -> Blocks.COPPER_TRAPDOOR.waxed().oxidized())),

	CUT_COPPER = oxidizationChain(
		List.of(() -> Blocks.CUT_COPPER.weathering().unaffected(), () -> Blocks.CUT_COPPER.weathering().exposed(), () -> Blocks.CUT_COPPER.weathering().weathered(), () -> Blocks.CUT_COPPER.weathering().oxidized()),
		List.of(() -> Blocks.CUT_COPPER.waxed().unaffected(), () -> Blocks.CUT_COPPER.waxed().exposed(), () -> Blocks.CUT_COPPER.waxed().weathered(), () -> Blocks.CUT_COPPER.waxed().oxidized())),

	CUT_COPPER_STAIRS = oxidizationChain(
		List.of(() -> Blocks.CUT_COPPER_STAIRS.weathering().unaffected(), () -> Blocks.CUT_COPPER_STAIRS.weathering().exposed(), () -> Blocks.CUT_COPPER_STAIRS.weathering().weathered(), () -> Blocks.CUT_COPPER_STAIRS.weathering().oxidized()),
		List.of(() -> Blocks.CUT_COPPER_STAIRS.waxed().unaffected(), () -> Blocks.CUT_COPPER_STAIRS.waxed().exposed(), () -> Blocks.CUT_COPPER_STAIRS.waxed().weathered(), () -> Blocks.CUT_COPPER_STAIRS.waxed().oxidized())),

	CUT_COPPER_SLAB = oxidizationChain(
		List.of(() -> Blocks.CUT_COPPER_SLAB.weathering().unaffected(), () -> Blocks.CUT_COPPER_SLAB.weathering().exposed(), () -> Blocks.CUT_COPPER_SLAB.weathering().weathered(), () -> Blocks.CUT_COPPER_SLAB.weathering().oxidized()),
		List.of(() -> Blocks.CUT_COPPER_SLAB.waxed().unaffected(), () -> Blocks.CUT_COPPER_SLAB.waxed().exposed(), () -> Blocks.CUT_COPPER_SLAB.waxed().weathered(), () -> Blocks.CUT_COPPER_SLAB.waxed().oxidized()));

	public CreateDeployingRecipeGen(PackOutput output, CompletableFuture<Provider> registries) {
		super(output, registries, Create.ID);
	}
}
