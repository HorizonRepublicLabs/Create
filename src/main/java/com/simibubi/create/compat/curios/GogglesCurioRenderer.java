package com.simibubi.create.compat.curios;

import net.createmod.catnip.api.client.render.DefaultSuperRenderTypeBuffer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import com.simibubi.create.foundation.render.CreateItemRenderer;

import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.Create;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@OnlyIn(Dist.CLIENT)
public class GogglesCurioRenderer implements ICurioRenderer {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Create.asResource("goggles"), "goggles");

	private final HumanoidModel<HumanoidRenderState> model;

	public GogglesCurioRenderer(ModelPart part) {
		this.model = new HumanoidModel<>(part);
	}

	/// Curios draws from a render state and submits through a collector now, so
	/// the head follows the animated model rather than the entity directly.
	@Override
	public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack,
		SlotContext slotContext, PoseStack matrixStack, SubmitNodeCollector collector, int light, S renderState,
		RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float netHeadYaw,
		float headPitch) {
		ICurioRenderer.setupHumanoidAnimations(model, renderState);

		// Translate and rotate with our head
		matrixStack.pushPose();
		matrixStack.translate(model.head.x / 16.0, model.head.y / 16.0, model.head.z / 16.0);
		matrixStack.mulPose(Axis.ZP.rotation(model.head.zRot));
		matrixStack.mulPose(Axis.YP.rotation(model.head.yRot));
		matrixStack.mulPose(Axis.XP.rotation(model.head.xRot));

		// Translate and scale to our head
		matrixStack.translate(0, -0.25, 0);
		matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
		matrixStack.scale(0.625f, 0.625f, 0.625f);

		if (!slotContext.entity()
			.getItemBySlot(EquipmentSlot.HEAD)
			.isEmpty()) {
			matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
			matrixStack.translate(0, -0.25, 0);
		}

		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		buffer.setCollector(collector);
		CreateItemRenderer.render(stack, ItemDisplayContext.HEAD, matrixStack, buffer, light,
			OverlayTexture.NO_OVERLAY, 0);
		buffer.draw();
		buffer.setCollector(null);

		matrixStack.popPose();
	}

	public static MeshDefinition mesh() {
		CubeListBuilder builder = new CubeListBuilder();
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		mesh.getRoot().addOrReplaceChild("head", builder, PartPose.ZERO);
		return mesh;
	}
}
