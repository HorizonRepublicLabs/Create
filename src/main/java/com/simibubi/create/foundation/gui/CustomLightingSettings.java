package com.simibubi.create.foundation.gui;

import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.platform.Lighting;

import org.joml.Vector3f;

import com.mojang.math.Axis;

import net.createmod.catnip.api.client.gui.ILightingSettings;

public class CustomLightingSettings implements ILightingSettings {

	private Vector3f light1;
	private Vector3f light2;

	protected CustomLightingSettings(float yRot, float xRot) {
		init(yRot, xRot, 0, 0, false);
	}

	protected CustomLightingSettings(float yRot1, float xRot1, float yRot2, float xRot2) {
		init(yRot1, xRot1, yRot2, xRot2, true);
	}

	protected void init(float yRot1, float xRot1, float yRot2, float xRot2, boolean doubleLight) {
		light1 = new Vector3f(0, 0, 1);
		light1.rotate(Axis.YP.rotationDegrees(yRot1));
		light1.rotate(Axis.XN.rotationDegrees(xRot1));

		if (doubleLight) {
			light2 = new Vector3f(0, 0, 1);
			light2.rotate(Axis.YP.rotationDegrees(yRot2));
			light2.rotate(Axis.XN.rotationDegrees(xRot2));
		} else {
			light2 = new Vector3f();
		}
	}

	/// 26.x drives lighting from preset Lighting.Entry values through the game
	/// renderer, and RenderSystem.setShaderLights now takes a packed GPU buffer
	/// rather than two direction vectors. There is no way to hand it the custom
	/// angles this class computes, so it falls back to the closest stock preset:
	/// these settings only ever lit block models in JEI panels.
	@Override
	public void apply() {
		Minecraft.getInstance()
			.gameRenderer
			.lighting()
			.setupFor(Lighting.Entry.ITEMS_3D);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private float yRot1, xRot1;
		private float yRot2, xRot2;
		private boolean doubleLight;

		public Builder firstLightRotation(float yRot, float xRot) {
			yRot1 = yRot;
			xRot1 = xRot;
			return this;
		}

		public Builder secondLightRotation(float yRot, float xRot) {
			yRot2 = yRot;
			xRot2 = xRot;
			doubleLight = true;
			return this;
		}

		public Builder doubleLight() {
			doubleLight = true;
			return this;
		}

		public CustomLightingSettings build() {
			if (doubleLight) {
				return new CustomLightingSettings(yRot1, xRot1, yRot2, xRot2);
			} else {
				return new CustomLightingSettings(yRot1, xRot1);
			}
		}

	}

}
