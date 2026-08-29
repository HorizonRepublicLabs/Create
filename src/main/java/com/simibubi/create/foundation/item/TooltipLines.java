package com.simibubi.create.foundation.item;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

/// appendHoverText collects lines through a Consumer now rather than filling a
/// List. Create's tooltips are written against the list, and several build them
/// across early returns, so this forwards adds straight to the consumer instead
/// of buffering and flushing at the end.
public class TooltipLines {
	public static List<Component> forwarding(Consumer<Component> builder) {
		return new AbstractList<>() {
			@Override
			public boolean add(Component line) {
				builder.accept(line);
				return true;
			}

			@Override
			public void add(int index, Component line) {
				builder.accept(line);
			}

			@Override
			public boolean addAll(java.util.Collection<? extends Component> lines) {
				lines.forEach(builder);
				return !lines.isEmpty();
			}

			@Override
			public Component get(int index) {
				throw new UnsupportedOperationException("tooltip lines are write-only");
			}

			@Override
			public int size() {
				return 0;
			}
		};
	}
}
