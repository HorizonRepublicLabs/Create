package com.simibubi.create.content.kinetics.waterwheel;

import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.client.outliner.Outliner;
import net.createmod.catnip.api.data.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/// @OnlyIn no longer strips members at runtime, so the outline this draws when the
/// wheel does not fit lives in a class the server never loads.
@OnlyIn(Dist.CLIENT)
public class LargeWaterWheelPlacementFeedback {
	public static void showBounds(LargeWaterWheelBlock block, BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Axis axis = block.getAxisForPlacement(context);
		Vec3 contract = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis)
			.getUnitVec3i());
		if (!(context.getPlayer() instanceof LocalPlayer localPlayer))
			return;
		Outliner.getInstance().showAABB(Pair.of("waterwheel", pos), new AABB(pos).inflate(1)
			.deflate(contract.x, contract.y, contract.z))
			.colored(0xFF_ff5d6c);
		CreateLang.translate("large_water_wheel.not_enough_space")
			.color(0xFF_ff5d6c)
			.sendStatus(localPlayer);
	}
}
