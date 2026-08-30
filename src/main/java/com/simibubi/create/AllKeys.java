package com.simibubi.create;

import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(Dist.CLIENT)
public enum AllKeys {

	TOOL_MENU("toolmenu", GLFW.GLFW_KEY_LEFT_ALT, "Focus Schematic Overlay"),
	ACTIVATE_TOOL(GLFW.GLFW_KEY_LEFT_CONTROL),
	TOOLBELT("toolbelt", GLFW.GLFW_KEY_LEFT_ALT, "Access Nearby Toolboxes"),
	ROTATE_MENU("rotate_menu", GLFW.GLFW_KEY_UNKNOWN, "Open Block Rotation Menu"),

	SHIFT_MODIFIER("shift_modifier", GLFW.GLFW_KEY_LEFT_SHIFT, "Shift Modifier", true),
	CTRL_MODIFIER("ctrl_modifier", GLFW.GLFW_KEY_LEFT_CONTROL, "Ctrl Modifier", true),
	ALT_MODIFIER("alt_modifier", GLFW.GLFW_KEY_LEFT_ALT, "Alt Modifier", true),
	;

	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Create.asResource("main"));

	private KeyMapping keybind;
	private final String description;
	private final String translation;
	private final int key;
	private final boolean modifiable;
	private final boolean conflictSafe;

	AllKeys(int defaultKey) {
		this("", defaultKey, "");
	}

	AllKeys(String description, int defaultKey, String translation) {
		this(description, defaultKey, translation, false);
	}

	AllKeys(String description, int defaultKey, String translation, boolean conflictSafe) {
		this.description = Create.ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.translation = translation;
		this.conflictSafe = conflictSafe;
	}

	public static void provideLang(BiConsumer<String, String> consumer) {
		for (AllKeys key : values())
			if (key.modifiable)
				consumer.accept(key.description, key.translation);
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		// A key mapping belongs to a registered category now rather than a
		// name, and NeoForge never had the fabric conflict problem catnip's
		// conflict-safe mapping worked around.
		event.registerCategory(CATEGORY);
		for (AllKeys key : values()) {
			key.keybind = new KeyMapping(key.description, key.key, CATEGORY);
			if (!key.modifiable)
				continue;

			event.register(key.keybind);
		}
	}

	public KeyMapping getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		if (!modifiable)
			return isKeyDown(key);
		return keybind.isDown();
	}

	public String getBoundKey() {
		return keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public boolean doesModifierAndCodeMatch(int code) {
		boolean codeMatches = code == keybind.getKey().getValue();

		boolean modifierMatches;
		KeyModifier modifier = keybind.getKeyModifier();
		if (modifier == KeyModifier.NONE) {
			modifierMatches = true;
		} else {
			modifierMatches = KeyModifier.getActiveModifiers().contains(modifier);
		}

		return codeMatches && modifierMatches;
	}

	public static boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow(), key);
	}

	public static boolean isMouseButtonDown(int button) {
		// glfw still wants the raw handle; Window exposes it as handle()
		return GLFW.glfwGetMouseButton(Minecraft.getInstance()
			.getWindow()
			.handle(), button) == 1;
	}

	public static boolean ctrlDown() {
		return isKeyDown(CTRL_MODIFIER.keybind.getKey().getValue());
	}

	public static boolean shiftDown() {
		return isKeyDown(SHIFT_MODIFIER.keybind.getKey().getValue());
	}

	public static boolean altDown() {
		return isKeyDown(ALT_MODIFIER.keybind.getKey().getValue());
	}

}
