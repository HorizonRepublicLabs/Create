package com.simibubi.create.foundation.utility;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;

/// Catnip no longer carries these, and the six-double list is the shape
/// contraption bounds have always been saved in.
public class AABBNbt {

	public static ListTag write(AABB bb) {
		ListTag list = new ListTag();
		list.add(DoubleTag.valueOf(bb.minX));
		list.add(DoubleTag.valueOf(bb.minY));
		list.add(DoubleTag.valueOf(bb.minZ));
		list.add(DoubleTag.valueOf(bb.maxX));
		list.add(DoubleTag.valueOf(bb.maxY));
		list.add(DoubleTag.valueOf(bb.maxZ));
		return list;
	}

	public static AABB read(ListTag list) {
		if (list.size() < 6)
			return new AABB(0, 0, 0, 0, 0, 0);
		return new AABB(list.getDoubleOr(0, 0), list.getDoubleOr(1, 0), list.getDoubleOr(2, 0), list.getDoubleOr(3, 0),
			list.getDoubleOr(4, 0), list.getDoubleOr(5, 0));
	}
}
