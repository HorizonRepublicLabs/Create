package com.simibubi.create.foundation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks something that only exists on the client. 26.2 dropped the runtime
/// member-stripping @OnlyIn used to do, so Create strips these itself: a dedicated
/// server never sees a member wearing this, and never has to resolve the client
/// types it mentions. Create's own marker rather than NeoForge's, so the loader does
/// not warn about relying on behaviour it no longer provides.
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface ClientOnly {
}
