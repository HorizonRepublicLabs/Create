package com.simibubi.create.content.kinetics.steamEngine;

import net.minecraft.util.RandomSource;

import net.minecraft.util.ARGB;

import net.minecraft.client.renderer.state.level.QuadParticleRenderState;

import net.minecraft.client.particle.SingleQuadParticle;

import com.simibubi.create.foundation.render.BlockEntityRenderHelper;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SteamJetParticle extends SimpleAnimatedParticle {

	private float yaw, pitch;

	protected SteamJetParticle(ClientLevel world, SteamJetParticleData data, double x, double y, double z, double dx,
		double dy, double dz, SpriteSet sprite) {
		super(world, x, y, z, sprite, world.getRandom().nextFloat() * .5f);
		xd = 0;
		yd = 0;
		zd = 0;
		gravity = 0;
		quadSize = .375f;
		setLifetime(21);
		setPos(x, y, z);
		roll = oRoll = world.getRandom().nextFloat() * Mth.PI;
		yaw = (float) Mth.atan2(dx, dz) - Mth.PI;
		pitch = (float) Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) - Mth.PI / 2;
		this.setSpriteFromAge(sprite);
	}

	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	/// Four quads around the jet's axis. A particle hands each quad to the
	/// render state as a rotation now, so the vertex offset that used to be
	/// applied before rotating becomes an offset of the quad's centre.
	@Override
	public void extract(QuadParticleRenderState particleState, Camera camera, float pPartialTicks) {
		Vec3 vec3 = camera.position();
		float f = (float) (x - vec3.x);
		float f1 = (float) (y - vec3.y);
		float f2 = (float) (z - vec3.z);
		float f3 = Mth.lerp(pPartialTicks, this.oRoll, this.roll);
		float f4 = this.getQuadSize(pPartialTicks);
		int light = this.getLightCoords(pPartialTicks);
		int color = ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol);

		for (int i = 0; i < 4; i++) {
			Quaternionf quaternion = Axis.YP.rotation(yaw);
			quaternion.mul(Axis.XP.rotation(pitch));
			quaternion.mul(Axis.YP.rotation(f3 + Mth.PI / 2 * i + roll));

			Vector3f offset = new Vector3f(0, 1, 0).rotate(quaternion)
				.mul(f4);

			particleState.add(getLayer(), f + offset.x(), f1 + offset.y(), f2 + offset.z(), quaternion.x, quaternion.y,
				quaternion.z, quaternion.w, f4, this.getU0(), this.getU1(), this.getV0(), this.getV1(), color, light);
		}
	}

	@Override
	public int getLightCoords(float partialTick) {
		BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
		return this.level.isLoaded(blockpos) ? BlockEntityRenderHelper.lightColorAt(level, blockpos) : 0;
	}

	public static class Factory implements ParticleProvider<SteamJetParticleData> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle createParticle(SteamJetParticleData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, RandomSource randomSource) {
			return new SteamJetParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

}
