package com.simibubi.create.content.equipment.clipboard;

import com.simibubi.create.foundation.data.VariantModels;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.ItemDisplayContext;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;

import net.minecraft.client.renderer.item.SelectItemModel;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.data.models.model.ItemModelUtils;

import com.mojang.serialization.MapCodec;

import java.util.List;

import java.util.ArrayList;

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

	/// Item model overrides are gone; a model selects between cases on a named
	/// property instead, so the clipboard's type becomes one.
	@OnlyIn(Dist.CLIENT)
	public record TypeProperty() implements SelectItemModelProperty<ClipboardType> {

		public static final Codec<ClipboardType> VALUE_CODEC = ClipboardType.CODEC;
		public static final SelectItemModelProperty.Type<TypeProperty, ClipboardType> TYPE =
			SelectItemModelProperty.Type.create(MapCodec.unit(new TypeProperty()), VALUE_CODEC);

		@Override
		public ClipboardType get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed,
			ItemDisplayContext displayContext) {
			return stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY)
				.type();
		}

		@Override
		public SelectItemModelProperty.Type<TypeProperty, ClipboardType> type() {
			return TYPE;
		}

		@Override
		public Codec<ClipboardType> valueCodec() {
			return VALUE_CODEC;
		}
	}

	public static void addOverrideModels(DataGenContext<Item, ClipboardBlockItem> c, RegistrateItemModelGenerator p) {
		List<SelectItemModel.SwitchCase<ClipboardType>> cases = new ArrayList<>();
		for (ClipboardType type : ClipboardType.values()) {
			Identifier model = p.modLoc("item/" + type.file);
			VariantModels.models(p)
				.withExistingParent("item/" + type.file, Identifier.withDefaultNamespace("item/generated"))
				.texture("layer0", Create.asResource("item/" + type.file))
				.build();
			cases.add(ItemModelUtils.when(type, ItemModelUtils.plainModel(model)));
		}
		p.itemModelOutput.accept(c.get(), ItemModelUtils.select(new TypeProperty(), cases));
	}

}
