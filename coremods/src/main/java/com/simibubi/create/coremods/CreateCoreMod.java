package com.simibubi.create.coremods;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;

/// 26.2 dropped the runtime member-stripping that @OnlyIn used to do, which leaves
/// Create's client-only methods in classes the dedicated server loads. The server
/// strips them itself instead.
public class CreateCoreMod implements ClassProcessorProvider {
	@Override
	public void createProcessors(Context context, Collector collector) {
		if (FMLLoader.getCurrent().getDist() == Dist.DEDICATED_SERVER)
			collector.add(new DistMemberStripper());
	}
}
