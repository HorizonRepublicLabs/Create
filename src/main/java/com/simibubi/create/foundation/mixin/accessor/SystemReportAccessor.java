package com.simibubi.create.foundation.mixin.accessor;

import java.util.List;

import net.minecraft.CrashReportCategory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.SystemReport;

@Mixin(SystemReport.class)
public interface SystemReportAccessor {
	@Accessor
	static String getOPERATING_SYSTEM() {
		throw new AssertionError();
	}

	@Accessor
	static String getJAVA_VERSION() {
		throw new AssertionError();
	}

	/// The report keeps an ordered list of key/value entries now rather than a
	/// map.
	@Accessor
	List<CrashReportCategory.Entry> getEntries();
}
