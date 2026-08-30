package com.simibubi.create.infrastructure.ponder;

import net.createmod.ponder.api.client.element.MinecartElement.MinecartConstructor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;

/// Minecarts are built from their entity type and placed afterwards rather than
/// taking a position, so the scenes name the type they want.
public class PonderCarts {

	public static final MinecartConstructor MINECART = of(EntityTypes.MINECART);
	public static final MinecartConstructor CHEST_MINECART = of(EntityTypes.CHEST_MINECART);

	private static MinecartConstructor of(EntityType<? extends AbstractMinecart> type) {
		return (level, x, y, z) -> {
			AbstractMinecart cart = type.create(level, net.minecraft.world.entity.EntitySpawnReason.LOAD);
			if (cart != null)
				cart.setPos(x, y, z);
			return cart;
		};
	}

	private PonderCarts() {
		// only the constants
		Minecart.class.getName();
		MinecartChest.class.getName();
	}
}
