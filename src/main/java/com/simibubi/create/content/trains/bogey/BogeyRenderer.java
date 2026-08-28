package com.simibubi.create.content.trains.bogey;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;

public interface BogeyRenderer {
	void render(CompoundTag bogeyData, float wheelAngle, float partialTick, PoseStack poseStack, SuperRenderTypeBuffer bufferSource, int packedLight, int packedOverlay, boolean inContraption);
}
