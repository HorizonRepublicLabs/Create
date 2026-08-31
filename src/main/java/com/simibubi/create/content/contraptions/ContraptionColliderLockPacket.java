package com.simibubi.create.content.contraptions;

import com.simibubi.create.foundation.ClientOnly;
import com.simibubi.create.foundation.networking.CreatePacketPayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.createmod.catnip.api.network.SelfHandlingPayload;

import net.createmod.catnip.api.network.NetworkHelper;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ContraptionColliderLockPacket(int contraption, double offset, int sender) implements CreatePacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionColliderLockPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ContraptionColliderLockPacket::contraption,
			ByteBufCodecs.DOUBLE, ContraptionColliderLockPacket::offset,
			ByteBufCodecs.VAR_INT, ContraptionColliderLockPacket::sender,
	        ContraptionColliderLockPacket::new
	);

	@ClientOnly
	public void handle(LocalPlayer player) {
		ContraptionCollider.lockPacketReceived(contraption, sender, offset);
	}

	@Override
	public AllPackets getTypeProvider() {
		return AllPackets.CONTRAPTION_COLLIDER_LOCK;
	}

	public record ContraptionColliderLockPacketRequest(int contraption, double offset) implements SelfHandlingPayload, CreatePacketPayload {
		public static final StreamCodec<ByteBuf, ContraptionColliderLockPacketRequest> STREAM_CODEC = StreamCodec.composite(
		        ByteBufCodecs.VAR_INT, ContraptionColliderLockPacketRequest::contraption,
				ByteBufCodecs.DOUBLE, ContraptionColliderLockPacketRequest::offset,
		        ContraptionColliderLockPacketRequest::new
		);

		@Override
		public void handle(ServerPlayer player) {
			NetworkHelper.INSTANCE.sendToClientsTrackingEntity(player, new ContraptionColliderLockPacket(contraption, offset, player.getId()));
		}

		@Override
		public AllPackets getTypeProvider() {
			return AllPackets.CONTRAPTION_COLLIDER_LOCK_REQUEST;
		}
	}

}
