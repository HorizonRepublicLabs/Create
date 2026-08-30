package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.item.CustomArmPoseItem;

import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.world.entity.Avatar;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(AvatarRenderer.class)
public class PlayerRendererMixin {
	/// The pose is picked per held stack now, and off an Avatar rather than a client player.
	@Inject(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at = @At("HEAD"), cancellable = true)
	private static void create$onGetArmPose(Avatar player, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<ArmPose> cir) {
		if (stack.getItem() instanceof CustomArmPoseItem armPoseProvider) {
			ArmPose pose = armPoseProvider.getArmPose(stack, player, hand);
			if (pose != null) {
				cir.setReturnValue(pose);
			}
		}
	}
}
