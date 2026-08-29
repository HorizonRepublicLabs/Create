package com.simibubi.create.content.contraptions.sync;

import com.simibubi.create.foundation.networking.CreatePacketPayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecs;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record LimbSwingUpdatePacket(int entityId, Vec3 position, float limbSwing) implements CreatePacketPayload {
	public static final StreamCodec<ByteBuf, LimbSwingUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, LimbSwingUpdatePacket::entityId,
			CatnipStreamCodecs.VEC3, LimbSwingUpdatePacket::position,
			ByteBufCodecs.FLOAT, LimbSwingUpdatePacket::limbSwing,
	        LimbSwingUpdatePacket::new
	);

	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entity = player.level().getEntity(entityId);
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		data.putInt("LastOverrideLimbSwingUpdate", 0);
		data.putFloat("OverrideLimbSwing", limbSwing);
		if (entity.getInterpolation() != null) {
			entity.getInterpolation()
				.interpolateTo(position, entity.getYRot(), entity.getXRot());
			entity.getInterpolation()
				.setInterpolationLength(2);
		} else {
			entity.snapTo(position.x, position.y, position.z, entity.getYRot(), entity.getXRot());
		}
	}

	@Override
	public AllPackets getTypeProvider() {
		return AllPackets.LIMBSWING_UPDATE;
	}
}
