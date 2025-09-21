package com.simibubi.create.infrastructure.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.RuntimeDataGenerator;
import com.simibubi.create.foundation.utility.CreatePaths;
import com.simibubi.create.foundation.utility.FilesHelper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.IoSupplier;

public class DumpDynamicPackCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("dumpDynamicPack")
			.requires(cs -> cs.hasPermission(4))
			.executes((ctx) -> {
				Path baseFolder = CreatePaths.DYNAMIC_PACK_DUMP;
				File baseFolderFile = baseFolder.toFile();

				try {
					FileUtils.deleteDirectory(baseFolderFile);
					baseFolderFile.mkdirs();

					for (Entry<String, IoSupplier<InputStream>> entry : RuntimeDataGenerator.PACK.getFiles().entrySet()) {
						Path path = baseFolder.resolve(entry.getKey());
						FilesHelper.createFolderIfMissing(path.getParent());
						Files.copy(entry.getValue().get(), path);
					}
				} catch (IOException e) {
					String message = "Encountered error while dumping dynamic pack files";
					ctx.getSource().sendFailure(Component.literal(message));
					Create.LOGGER.error(message, e);
					return 0;
				}

				ctx.getSource().sendSuccess(() -> Component.literal("Dumped dynamic pack!"), false);
				return 1;
			});
	}
}
