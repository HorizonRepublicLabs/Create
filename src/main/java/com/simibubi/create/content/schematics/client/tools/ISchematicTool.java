package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

public interface ISchematicTool {

    void init();

    void updateSelection();

    boolean handleRightClick();

    boolean handleMouseWheel(double delta);

    void renderTool(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera);

    void renderOverlay(Gui gui, GuiGraphics graphics, float partialTicks, int width, int height);

    void renderOnSchematic(PoseStack ms, SuperRenderTypeBuffer buffer);
}
