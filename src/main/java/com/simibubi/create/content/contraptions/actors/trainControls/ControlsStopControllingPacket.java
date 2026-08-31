package com.simibubi.create.content.contraptions.actors.trainControls;

import com.simibubi.create.foundation.ClientOnly;
import com.simibubi.create.foundation.networking.ClientboundCreatePayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;

public enum ControlsStopControllingPacket implements ClientboundCreatePayload {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ControlsStopControllingPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@ClientOnly
	public void handle(LocalPlayer player) {
		ControlsHandler.stopControlling();
	}

	@Override
	public AllPackets getTypeProvider() {
		return AllPackets.CONTROLS_ABORT;
	}
}
