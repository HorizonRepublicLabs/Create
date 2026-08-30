package com.simibubi.create.foundation.networking;

import net.minecraft.client.player.LocalPlayer;

/// catnip's clientbound payloads no longer handle themselves -- the handler is
/// registered separately against a ClientboundPayloadHandler. Create's packets
/// still carry their handling code, so this keeps that a real contract until
/// the handlers are wired up at registration.
public interface ClientboundCreatePayload extends CreatePacketPayload {
	void handle(LocalPlayer player);
}
