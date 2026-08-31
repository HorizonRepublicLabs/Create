package com.simibubi.create.content.contraptions;

import com.simibubi.create.foundation.ClientOnly;
import com.simibubi.create.foundation.networking.CreatePacketPayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.simibubi.create.AllPackets;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import io.netty.buffer.ByteBuf;

public record ContraptionDisassemblyPacket(int entityId, StructureTransform transform) implements CreatePacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionDisassemblyPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionDisassemblyPacket::entityId,
			StructureTransform.STREAM_CODEC, ContraptionDisassemblyPacket::transform,
			ContraptionDisassemblyPacket::new
	);

	@ClientOnly
	public void handle(LocalPlayer player) {
		AbstractContraptionEntity.handleDisassemblyPacket(this);
	}

	@Override
	public AllPackets getTypeProvider() {
		return AllPackets.CONTRAPTION_DISASSEMBLE;
	}
}
