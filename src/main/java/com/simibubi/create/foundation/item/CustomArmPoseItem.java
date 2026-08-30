package com.simibubi.create.foundation.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface CustomArmPoseItem {
	@Nullable
	ArmPose getArmPose(ItemStack stack, Avatar player, InteractionHand hand);
}
