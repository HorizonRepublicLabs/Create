package com.simibubi.create.content.logistics.redstoneRequester;

import com.simibubi.create.foundation.item.TooltipLines;

import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

import java.util.List;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class RedstoneRequesterBlockItem extends LogisticallyLinkedBlockItem {

	public RedstoneRequesterBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, TooltipDisplay display,
		Consumer<Component> builder, TooltipFlag tooltipFlag) {
		List<Component> tooltipComponents = TooltipLines.forwarding(builder);
		if (!isTuned(stack))
			return;

		if (!stack.has(AllDataComponents.AUTO_REQUEST_DATA)) {
			super.appendHoverText(stack, tooltipContext, display, builder, tooltipFlag);
			return;
		}

		CreateLang.translate("logistically_linked.tooltip")
			.style(ChatFormatting.GOLD)
			.addTo(tooltipComponents);
		RedstoneRequesterBlock.appendRequesterTooltip(stack, tooltipComponents);
	}

}
