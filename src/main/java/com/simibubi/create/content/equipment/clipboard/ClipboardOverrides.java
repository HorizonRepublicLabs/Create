package com.simibubi.create.content.equipment.clipboard;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.api.lang.Lang;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClipboardOverrides {

	public enum ClipboardType implements StringRepresentable {
		EMPTY("empty_clipboard"), WRITTEN("clipboard"), EDITING("clipboard_and_quill");

		public static final Codec<ClipboardType> CODEC = StringRepresentable.fromValues(ClipboardType::values);
		public static final StreamCodec<ByteBuf, ClipboardType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ClipboardType.class);

		public final String file;
		public static Identifier ID = Create.asResource("clipboard_type");

		ClipboardType(String file) {
			this.file = file;
		}

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerModelOverridesClient(ClipboardBlockItem item) {
		ItemProperties.register(item, ClipboardType.ID, (pStack, pLevel, pEntity, pSeed) ->
			pStack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY).type().ordinal()
		);
	}

	public static ItemModelGenShim.Builder addOverrideModels(DataGenContext<Item, ClipboardBlockItem> c,
		RegistrateItemModelGenerator p) {
		ItemModelGenShim.Builder builder = p.generated(c::get);
		for (ClipboardType type : ClipboardType.values()) {
			int i = type.ordinal();
			builder.override()
					.predicate(ClipboardType.ID, i)
					.model(p.getBuilder(c.getName() + "_" + i)
							.parent(new Identifier("item/generated"))
							.texture("layer0", Create.asResource("item/" + type.file)))
					.end();
		}
		return builder;
	}

}
