package net.tfminecraft.ArmourShop.pack.model;

import java.nio.file.Path;

/**
 * Paths under ItemsAdder contents/ for pack namespaces.
 */
public final class PackPaths {

	public static final String NAMESPACE = "tfmc_submissions";
	public static final String STAFF_NAMESPACE = "tfmc_armorshop";

	private PackPaths() {}

	/** `{contents}/tfmc_submissions` */
	public static Path namespaceRoot(Path contentsRoot) {
		return namespaceRoot(contentsRoot, NAMESPACE);
	}

	public static Path namespaceRoot(Path contentsRoot, String namespace) {
		return contentsRoot.resolve(requireNamespace(namespace));
	}

	public static Path configsDir(Path contentsRoot) {
		return configsDir(contentsRoot, NAMESPACE);
	}

	public static Path configsDir(Path contentsRoot, String namespace) {
		return namespaceRoot(contentsRoot, namespace).resolve("configs");
	}

	public static Path assetsRoot(Path contentsRoot) {
		return assetsRoot(contentsRoot, NAMESPACE);
	}

	public static Path assetsRoot(Path contentsRoot, String namespace) {
		String ns = requireNamespace(namespace);
		return namespaceRoot(contentsRoot, ns)
			.resolve("resourcepack")
			.resolve("assets")
			.resolve(ns);
	}

	public static Path texturesRoot(Path contentsRoot) {
		return texturesRoot(contentsRoot, NAMESPACE);
	}

	public static Path texturesRoot(Path contentsRoot, String namespace) {
		return assetsRoot(contentsRoot, namespace).resolve("textures");
	}

	public static Path armorIconsDir(Path contentsRoot) {
		return armorIconsDir(contentsRoot, NAMESPACE);
	}

	public static Path armorIconsDir(Path contentsRoot, String namespace) {
		return texturesRoot(contentsRoot, namespace).resolve("armor_icons");
	}

	public static Path armorLayersDir(Path contentsRoot) {
		return armorLayersDir(contentsRoot, NAMESPACE);
	}

	public static Path armorLayersDir(Path contentsRoot, String namespace) {
		return texturesRoot(contentsRoot, namespace).resolve("armor_layers");
	}

	public static Path itemTexturesDir(Path contentsRoot) {
		return itemTexturesDir(contentsRoot, NAMESPACE);
	}

	public static Path itemTexturesDir(Path contentsRoot, String namespace) {
		return texturesRoot(contentsRoot, namespace).resolve("item");
	}

	public static Path itemModelsDir(Path contentsRoot) {
		return itemModelsDir(contentsRoot, NAMESPACE);
	}

	public static Path itemModelsDir(Path contentsRoot, String namespace) {
		return assetsRoot(contentsRoot, namespace).resolve("models").resolve("item");
	}

	private static String requireNamespace(String namespace) {
		if (namespace == null || namespace.isBlank()) {
			throw new IllegalArgumentException("namespace is required");
		}
		return namespace.trim();
	}
}
