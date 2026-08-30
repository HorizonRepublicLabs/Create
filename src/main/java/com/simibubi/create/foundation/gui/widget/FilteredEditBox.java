package com.simibubi.create.foundation.gui.widget;

import java.util.function.Predicate;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/// EditBox no longer refuses text that fails a predicate, so the boxes that
/// only accept certain values check for themselves.
public class FilteredEditBox extends EditBox {

	private Predicate<String> filter = s -> true;

	public FilteredEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}

	public void setFilter(Predicate<String> filter) {
		this.filter = filter;
	}

	@Override
	public void setValue(String value) {
		if (filter.test(value))
			super.setValue(value);
	}

	@Override
	public void insertText(String input) {
		String start = getValue();
		super.insertText(input);
		if (!filter.test(getValue()))
			super.setValue(start);
	}
}
