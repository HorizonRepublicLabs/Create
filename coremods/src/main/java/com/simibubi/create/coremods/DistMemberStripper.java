package com.simibubi.create.coremods;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ProcessorName;

/// Removes members marked @ClientOnly from Create's classes. Without this a dedicated
/// server fails to verify any class holding one, since the client types those members
/// mention are not shipped with it.
public class DistMemberStripper implements ClassProcessor {
	private static final ProcessorName NAME = new ProcessorName("create", "dist_member_stripper");
	private static final String CLIENT_ONLY = "Lcom/simibubi/create/foundation/ClientOnly;";
	private static final Set<String> PACKAGES = Set.of("com/simibubi/create/");

	@Override
	public ProcessorName name() {
		return NAME;
	}

	@Override
	public boolean handlesClass(SelectionContext context) {
		if (context.empty())
			return false;
		String name = context.type()
			.getInternalName();
		for (String prefix : PACKAGES)
			if (name.startsWith(prefix))
				return true;
		return false;
	}

	@Override
	public ComputeFlags processClass(TransformationContext context) {
		ClassNode node = context.node();
		boolean stripped = false;
		Set<String> strippedMethods = new HashSet<>();

		if (node.methods != null)
			stripped |= node.methods.removeIf(method -> {
				if (!clientOnly(method.visibleAnnotations, method.invisibleAnnotations))
					return false;
				strippedMethods.add(method.name);
				return true;
			});

		// The lambdas a client-only method wrote go with it: their own signatures name the
		// client types the method was hiding.
		if (node.methods != null && !strippedMethods.isEmpty())
			stripped |= node.methods.removeIf(method -> isLambdaOf(method.name, strippedMethods));

		if (node.fields != null)
			stripped |= node.fields.removeIf(field -> clientOnly(field.visibleAnnotations, field.invisibleAnnotations));

		if (!stripped)
			return ComputeFlags.NO_REWRITE;

		context.audit("stripped client-only members");
		return ComputeFlags.SIMPLE_REWRITE;
	}

	private static boolean isLambdaOf(String name, Set<String> owners) {
		if (!name.startsWith("lambda$"))
			return false;
		int lastSeparator = name.lastIndexOf('$');
		if (lastSeparator <= "lambda$".length())
			return false;
		return owners.contains(name.substring("lambda$".length(), lastSeparator));
	}

	private static boolean clientOnly(List<AnnotationNode> visible, List<AnnotationNode> invisible) {
		return marksClient(visible) || marksClient(invisible);
	}

	private static boolean marksClient(List<AnnotationNode> annotations) {
		if (annotations == null)
			return false;
		for (AnnotationNode annotation : annotations)
			if (CLIENT_ONLY.equals(annotation.desc))
				return true;
		return false;
	}
}
