package com.simibubi.create.foundation.advancement;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;

import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/// CriterionTrigger no longer carries the per-player listener registry; it is
/// a codec and a criterion factory, and SimpleCriterionTrigger keeps the
/// bookkeeping. Create's own trigger only ever used that registry to fan out
/// to matching instances, which trigger(player, matcher) already does.
@ParametersAreNonnullByDefault
public abstract class CriterionTriggerBase<T extends CriterionTriggerBase.Instance>
	extends SimpleCriterionTrigger<T> {

	private final Identifier id;

	public CriterionTriggerBase(String id) {
		this.id = Create.asResource(id);
	}

	public Identifier getId() {
		return id;
	}

	protected void trigger(ServerPlayer player, @Nullable List<Supplier<Object>> suppliers) {
		trigger(player, instance -> instance.test(suppliers));
	}

	public abstract static class Instance implements SimpleCriterionTrigger.SimpleInstance {
		protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
	}

}
