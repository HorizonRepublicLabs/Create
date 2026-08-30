package com.simibubi.create.content.trains;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

/// Gathers every live cube into one render state, the way the vanilla quad
/// group does for billboards.
public class CubeParticleGroup extends ParticleGroup<CubeParticle> {

	private final CubeParticleRenderState renderState = new CubeParticleRenderState();

	public CubeParticleGroup(ParticleEngine engine) {
		super(engine);
	}

	@Override
	public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
		for (CubeParticle particle : particles) {
			net.minecraft.world.phys.Vec3 pos = particle.position();
			if (!frustum.pointInFrustum(pos.x, pos.y, pos.z))
				continue;
			try {
				particle.extract(renderState, camera, partialTickTime);
			} catch (Throwable throwable) {
				CrashReport report = CrashReport.forThrowable(throwable, "Rendering Particle");
				CrashReportCategory category = report.addCategory("Particle being rendered");
				category.setDetail("Particle", particle::toString);
				throw new ReportedException(report);
			}
		}
		return renderState;
	}
}
