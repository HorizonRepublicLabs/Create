package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.foundation.ClientOnly;
import com.simibubi.create.foundation.networking.ClientboundCreatePayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.BigItemStack;

import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public record LogisticalStockResponsePacket(boolean lastPacket, BlockPos pos, List<BigItemStack> items) implements ClientboundCreatePayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, LogisticalStockResponsePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, LogisticalStockResponsePacket::lastPacket,
		BlockPos.STREAM_CODEC, LogisticalStockResponsePacket::pos,
		CatnipStreamCodecBuilders.list(BigItemStack.STREAM_CODEC), LogisticalStockResponsePacket::items,
		LogisticalStockResponsePacket::new
	);

	@Override
	public AllPackets getTypeProvider() {
		return AllPackets.LOGISTICS_STOCK_RESPONSE;
	}

	@ClientOnly
	public void handle(LocalPlayer player) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe)
			stbe.receiveStockPacket(items, lastPacket);
	}
}
