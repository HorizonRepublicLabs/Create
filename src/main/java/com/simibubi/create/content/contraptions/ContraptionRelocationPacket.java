package com.simibubi.create.content.contraptions;

import com.simibubi.create.foundation.ClientOnly;
import com.simibubi.create.foundation.networking.ClientboundCreatePayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ContraptionRelocationPacket(int entityId) implements ClientboundCreatePayload {
	public static final StreamCodec<ByteBuf, ContraptionRelocationPacket> STREAM_CODEC = ByteBufCodecs.INT.map(
			ContraptionRelocationPacket::new, ContraptionRelocationPacket::entityId
	);

	@ClientOnly
	public void handle(LocalPlayer player) {
		OrientedContraptionEntity.handleRelocationPacket(this);
	}

	@Override
	public AllPackets getTypeProvider() {
		return AllPackets.CONTRAPTION_RELOCATION;
	}
}
