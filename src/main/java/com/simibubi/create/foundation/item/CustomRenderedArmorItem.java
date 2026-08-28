package com.simibubi.create.foundation.item;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface CustomRenderedArmorItem {
	@OnlyIn(Dist.CLIENT)
	void renderArmorPiece(HumanoidArmorLayer<?, ?, ?> layer, PoseStack poseStack, SuperRenderTypeBuffer bufferSource, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<?> originalModel, ItemStack stack);
}
