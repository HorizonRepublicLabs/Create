package com.simibubi.create.foundation.model;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.phys.Vec3;

/// 26.x turned BakedQuad into a record: positions are Vector3fc components and
/// UVs are packed into longs, rather than everything living in one int[] laid
/// out by the vertex format. These keep Create's per-vertex edits working
/// against that shape.
public final class BakedQuadHelper {

	private BakedQuadHelper() {}

	public static float getU(BakedQuad quad, int vertex) {
		return UVPair.unpackU(quad.packedUV(vertex));
	}

	public static float getV(BakedQuad quad, int vertex) {
		return UVPair.unpackV(quad.packedUV(vertex));
	}

	public static Vec3 getXYZ(BakedQuad quad, int vertex) {
		Vector3fc pos = quad.position(vertex);
		return new Vec3(pos.x(), pos.y(), pos.z());
	}

	/// Rebuilds a quad with new UVs; the record is immutable so every edit
	/// produces a fresh one.
	public static BakedQuad withUVs(BakedQuad quad, float[] us, float[] vs) {
		return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
			UVPair.pack(us[0], vs[0]), UVPair.pack(us[1], vs[1]), UVPair.pack(us[2], vs[2]),
			UVPair.pack(us[3], vs[3]), quad.direction(), quad.materialInfo(), quad.bakedNormals(),
			quad.bakedColors());
	}

	public static BakedQuad withPositions(BakedQuad quad, Vector3fc p0, Vector3fc p1, Vector3fc p2, Vector3fc p3) {
		return new BakedQuad(p0, p1, p2, p3, quad.packedUV0(), quad.packedUV1(), quad.packedUV2(),
			quad.packedUV3(), quad.direction(), quad.materialInfo(), quad.bakedNormals(), quad.bakedColors());
	}

	public static Editor edit(BakedQuad quad) {
		return new Editor(quad);
	}

	/// The record is immutable, so per-vertex edits collect here and produce a
	/// new quad at the end. Keeps the shape of the old int[] helpers.
	public static class Editor {

		private final BakedQuad source;
		private final Vector3f[] positions = new Vector3f[4];
		private final float[] us = new float[4];
		private final float[] vs = new float[4];

		Editor(BakedQuad quad) {
			this.source = quad;
			for (int i = 0; i < 4; i++) {
				Vector3fc pos = quad.position(i);
				positions[i] = new Vector3f(pos.x(), pos.y(), pos.z());
				us[i] = UVPair.unpackU(quad.packedUV(i));
				vs[i] = UVPair.unpackV(quad.packedUV(i));
			}
		}

		public float getU(int vertex) {
			return us[vertex];
		}

		public float getV(int vertex) {
			return vs[vertex];
		}

		public Editor setU(int vertex, float u) {
			us[vertex] = u;
			return this;
		}

		public Editor setV(int vertex, float v) {
			vs[vertex] = v;
			return this;
		}

		public Vec3 getXYZ(int vertex) {
			Vector3f pos = positions[vertex];
			return new Vec3(pos.x(), pos.y(), pos.z());
		}

		public Editor setXYZ(int vertex, Vec3 xyz) {
			positions[vertex].set((float) xyz.x, (float) xyz.y, (float) xyz.z);
			return this;
		}

		public BakedQuad build() {
			return new BakedQuad(positions[0], positions[1], positions[2], positions[3],
				UVPair.pack(us[0], vs[0]), UVPair.pack(us[1], vs[1]), UVPair.pack(us[2], vs[2]),
				UVPair.pack(us[3], vs[3]), source.getDirection(), source.materialInfo(), source.bakedNormals(),
				source.bakedColors());
		}
	}

	public static Vector3f toVector(Vec3 vec) {
		return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
	}
}
