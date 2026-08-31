package com.simibubi.create.coremods;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ProcessorName;

/// Removes members marked @OnlyIn(Dist.CLIENT) from Create's classes. Without this a
/// dedicated server fails to verify any class holding one, since the client types
/// those members mention are not shipped with it.
public class DistMemberStripper implements ClassProcessor {
	private static final ProcessorName NAME = new ProcessorName("create", "dist_member_stripper");
	private static final String ONLY_IN = "Lnet/neoforged/api/distmarker/OnlyIn;";
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
		for (AnnotationNode annotation : annotations) {
			if (!ONLY_IN.equals(annotation.desc) || annotation.values == null)
				continue;
			for (int i = 0; i < annotation.values.size() - 1; i += 2) {
				if (!"value".equals(annotation.values.get(i)))
					continue;
				if (annotation.values.get(i + 1) instanceof String[] enumValue && enumValue.length == 2
					&& "CLIENT".equals(enumValue[1]))
					return true;
			}
		}
		return false;
	}
}
