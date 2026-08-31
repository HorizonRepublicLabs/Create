package com.simibubi.create.infrastructure.data;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.compat.curios.CuriosDataGenerator;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.data.CreateDatamapProvider;
import com.simibubi.create.foundation.data.DamageTypeTagGen;
import com.simibubi.create.foundation.data.recipe.CreateMechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.data.recipe.CreateSequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateStandardRecipeGen;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;

import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class CreateDatagen {
	/// The event is split by side now, and no longer filters by mod -- each mod
	/// only hears about its own run.
	public static void gatherDataHighPriority(GatherDataEvent event) {
		bindItemComponents(event);
		addExtraRegistrateData();
	}

	/// Item components bind when a datapack loads, and datagen never loads one, so
	/// nothing that builds a stack -- ponder scenes, recipe results -- could run.
	private static void bindItemComponents(GatherDataEvent event) {
		HolderLookup.Provider registries = event.getLookupProvider()
			.join();

		// Binding runs the initializers, and NeoForge's development-only check on those
		// rejects vanilla's own tag-backed components, so it steps aside for the bind.
		boolean ide = SharedConstants.IS_RUNNING_IN_IDE;
		SharedConstants.IS_RUNNING_IN_IDE = false;
		try {
			BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries)
				.forEach(DataComponentInitializers.PendingComponents::apply);
		} finally {
			SharedConstants.IS_RUNNING_IN_IDE = ide;
		}
	}

	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(true, AllSoundEvents.provider(generator));

		GeneratedEntriesProvider generatedEntriesProvider = new GeneratedEntriesProvider(output, lookupProvider);
		lookupProvider = generatedEntriesProvider.getRegistryProvider();
		generator.addProvider(true, generatedEntriesProvider);

		generator.addProvider(true, new CreateRecipeSerializerTagsProvider(output, lookupProvider));
		generator.addProvider(true, new CreateContraptionTypeTagsProvider(output, lookupProvider));
		generator.addProvider(true, new CreateMountedItemStorageTypeTagsProvider(output, lookupProvider));
		generator.addProvider(true, new DamageTypeTagGen(output, lookupProvider));
		generator.addProvider(true, new AllAdvancements(output, lookupProvider));
		generator.addProvider(true, new CreateStandardRecipeGen(output, lookupProvider));
		generator.addProvider(true, new CreateMechanicalCraftingRecipeGen(output, lookupProvider));
		generator.addProvider(true, new CreateSequencedAssemblyRecipeGen(output, lookupProvider));
		generator.addProvider(true, new CreateDatamapProvider(output, lookupProvider));
		generator.addProvider(true, new VanillaHatOffsetGenerator(output, lookupProvider));
		generator.addProvider(true, new CuriosDataGenerator(output, lookupProvider));
		generator.addProvider(true, new CreateEnchantmentTagsProvider(output, lookupProvider));
		generator.addProvider(true, new CreateWikiBlockInfoProvider(output));
		generator.addProvider(event instanceof GatherDataEvent.Client, new CreateEquipmentAssetProvider(output));

		CreateRecipeProvider.registerAllProcessing(generator, output, lookupProvider);
	}

	private static void addExtraRegistrateData() {
		CreateRegistrateTags.addGenerators();

		Create.registrate().addDataGenerator(ProviderType.LANG, provider -> {
			BiConsumer<String, String> langConsumer = provider::add;

			provideDefaultLang("interface", langConsumer);
			provideDefaultLang("tooltips", langConsumer);
			AllAdvancements.provideLang(langConsumer);
			AllSoundEvents.provideLang(langConsumer);
			AllKeys.provideLang(langConsumer);
			providePonderLang(langConsumer);
			new TagLangGenerator(langConsumer).generate();
		});
	}

	private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
		String path = "assets/create/lang/default/" + fileName + ".json";
		JsonElement jsonElement = FilesHelper.loadJsonResource(path);
		if (jsonElement == null) {
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsJsonPrimitive().getAsString();
			consumer.accept(key, value);
		}
	}

	private static void providePonderLang(BiConsumer<String, String> consumer) {
		// Register this since FMLClientSetupEvent does not run during datagen
		PonderIndex.addPlugin(new CreatePonderPlugin());

		PonderIndex.getLangAccess().provideLang(Create.ID, consumer);
	}
}
