package com.simibubi.create.foundation.networking;

import com.simibubi.create.AllPackets;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/// Common base for Create's payloads.
///
/// catnip's BasePacketPayload supplied type() from a PacketTypeProvider and
/// went away in the 26.x network rework. The payloads still know their
/// AllPackets constant, so the same default lives here instead of being
/// repeated ninety times.
public interface CreatePacketPayload extends CustomPacketPayload {
	AllPackets getTypeProvider();

	@Override
	default Type<? extends CustomPacketPayload> type() {
		return getTypeProvider().getType();
	}
}
