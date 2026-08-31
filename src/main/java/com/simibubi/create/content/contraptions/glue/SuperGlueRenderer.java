package com.simibubi.create.content.contraptions.glue;

import com.simibubi.create.foundation.ClientOnly;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@ClientOnly
public class SuperGlueRenderer extends EntityRenderer<SuperGlueEntity, EntityRenderState> {

	public SuperGlueRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	/// Renderers describe entities through a render state now. Glue draws
	/// nothing itself -- the outline renderer handles it -- so a bare state
	/// is enough.
	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}

	@Override
	public boolean shouldRender(SuperGlueEntity entity, Frustum frustum, double x, double y, double z) {
		return false;
	}

}
