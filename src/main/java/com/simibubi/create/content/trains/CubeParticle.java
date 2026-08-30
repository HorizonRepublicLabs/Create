package com.simibubi.create.content.trains;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CubeParticle extends Particle {

	public static final Vec3[] CUBE = {
		// TOP
		new Vec3(1, 1, -1), new Vec3(1, 1, 1), new Vec3(-1, 1, 1), new Vec3(-1, 1, -1),

		// BOTTOM
		new Vec3(-1, -1, -1), new Vec3(-1, -1, 1), new Vec3(1, -1, 1), new Vec3(1, -1, -1),

		// FRONT
		new Vec3(-1, -1, 1), new Vec3(-1, 1, 1), new Vec3(1, 1, 1), new Vec3(1, -1, 1),

		// BACK
		new Vec3(1, -1, -1), new Vec3(1, 1, -1), new Vec3(-1, 1, -1), new Vec3(-1, -1, -1),

		// LEFT
		new Vec3(-1, -1, -1), new Vec3(-1, 1, -1), new Vec3(-1, 1, 1), new Vec3(-1, -1, 1),

		// RIGHT
		new Vec3(1, -1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, -1), new Vec3(1, -1, -1) };

	/// Own group so the six faces can be submitted as custom geometry; the
	/// vanilla types all assume a single billboard quad.
	public static final ParticleRenderType CUBES = new ParticleRenderType("CREATE_CUBES", "CC");

	protected float rCol = 1;
	protected float gCol = 1;
	protected float bCol = 1;
	protected float alpha = 1;

	protected float scale;
	protected boolean hot;

	public CubeParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z);
		this.xd = motionX;
		this.yd = motionY;
		this.zd = motionZ;

		setScale(0.2F);
	}

	/// The group culls by position, and it lives beside rather than inside the
	/// particle, so the coordinates need reading from outside.
	public Vec3 position() {
		return new Vec3(x, y, z);
	}

	public void setScale(float scale) {
		this.scale = scale;
		this.setSize(scale * 0.5f, scale * 0.5f);
	}

	public void averageAge(int age) {
		this.lifetime = (int) (age + (random.nextDouble() * 2D - 1D) * 8);
	}

	public void setHot(boolean hot) {
		this.hot = hot;
	}

	private boolean billowing = false;

	@Override
	public void tick() {
		if (this.hot && this.age > 0) {
			if (this.yo == this.y) {
				billowing = true;
				stoppedByCollision = false; // Prevent motion being ignored due to vertical collision
				if (this.xd == 0 && this.zd == 0) {
					Vec3 diff = Vec3.atLowerCornerOf(BlockPos.containing(x, y, z))
						.add(0.5, 0.5, 0.5)
						.subtract(x, y, z);
					this.xd = -diff.x * 0.1;
					this.zd = -diff.z * 0.1;
				}
				this.xd *= 1.1;
				this.yd *= 0.9;
				this.zd *= 1.1;
			} else if (billowing) {
				this.yd *= 1.2;
			}
		}
		super.tick();
	}

	/// Cubes shrink over their lifetime, so the size handed to the render state
	/// already carries the fade.
	public void extract(CubeParticleRenderState state, Camera renderInfo, float partialTicks) {
		Vec3 projectedView = renderInfo.position();
		float lerpedX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - projectedView.x());
		float lerpedY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - projectedView.y());
		float lerpedZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - projectedView.z());

		double ageMultiplier =
			1 - Math.pow(Mth.clamp(age + partialTicks, 0, lifetime), 3) / Math.pow(lifetime, 3);

		state.add(lerpedX, lerpedY, lerpedZ, (float) (scale * ageMultiplier), rCol, gCol, bCol, alpha,
			LightCoordsUtil.FULL_BRIGHT);
	}

	public void setColor(float r, float g, float b) {
		this.rCol = r;
		this.gCol = g;
		this.bCol = b;
	}

	@Override
	public ParticleRenderType getGroup() {
		return CUBES;
	}

	public static class Factory implements ParticleProvider<CubeParticleData> {

		@Override
		public Particle createParticle(CubeParticleData data, ClientLevel world, double x, double y, double z, double motionX,
			double motionY, double motionZ, net.minecraft.util.RandomSource random) {
			CubeParticle particle = new CubeParticle(world, x, y, z, motionX, motionY, motionZ);
			particle.setColor(data.r, data.g, data.b);
			particle.setScale(data.scale);
			particle.averageAge(data.avgAge);
			particle.setHot(data.hot);
			return particle;
		}
	}
}
