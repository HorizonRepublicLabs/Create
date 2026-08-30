package com.simibubi.create.content.trains;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.gui.texture.CatnipSpecialTextures;
import net.createmod.catnip.api.client.render.PonderRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/// Cube particles draw six faces rather than a billboard, so they cannot ride
/// the vanilla quad group. Each extracted cube is kept as its centre, size and
/// tint until submit hands the whole batch over as custom geometry.
public class CubeParticleRenderState implements ParticleGroupRenderState {

	private final List<Cube> cubes = new ArrayList<>();

	public void add(float x, float y, float z, float size, float r, float g, float b, float a, int light) {
		cubes.add(new Cube(x, y, z, size, r, g, b, a, light));
	}

	@Override
	public void clear() {
		cubes.clear();
	}

	@Override
	public void submit(SubmitNodeCollector collector, CameraRenderState camera) {
		if (cubes.isEmpty())
			return;

		List<Cube> batch = List.copyOf(cubes);
		collector.submitCustomGeometry(new PoseStack(),
			PonderRenderTypes.outlineTranslucent(CatnipSpecialTextures.BLANK.getId(), false), (pose, buffer) -> {
				for (Cube cube : batch)
					cube.render(pose, buffer);
			});
	}

	private record Cube(float x, float y, float z, float size, float r, float g, float b, float a, int light) {

		void render(PoseStack.Pose pose, com.mojang.blaze3d.vertex.VertexConsumer buffer) {
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 4; j++) {
					Vec3 corner = CubeParticle.CUBE[i * 4 + j].scale(-1)
						.scale(size)
						.add(x, y, z);

					buffer.addVertex(pose.pose(), (float) corner.x, (float) corner.y, (float) corner.z)
						.setColor(r, g, b, a)
						.setUv((float) j / 2, j % 2)
						.setOverlay(OverlayTexture.NO_OVERLAY)
						.setLight(light)
						.setNormal(pose.copy(), 0.0F, 1.0F, 0.0F);
				}
			}
		}
	}
}
