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

	public static Vector3f toVector(Vec3 vec) {
		return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
	}
}
