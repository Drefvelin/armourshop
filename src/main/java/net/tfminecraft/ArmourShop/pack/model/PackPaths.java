package net.tfminecraft.ArmourShop.pack.model;

import java.nio.file.Path;

/**
 * Paths under ItemsAdder contents/ for the tfmc_submissions namespace.
 */
public final class PackPaths {

	public static final String NAMESPACE = "tfmc_submissions";

	private PackPaths() {}

	/** `{contents}/tfmc_submissions` */
	public static Path namespaceRoot(Path contentsRoot) {
		return contentsRoot.resolve(NAMESPACE);
	}

	public static Path configsDir(Path contentsRoot) {
		return namespaceRoot(contentsRoot).resolve("configs");
	}

	public static Path assetsRoot(Path contentsRoot) {
		return namespaceRoot(contentsRoot)
			.resolve("resourcepack")
			.resolve("assets")
			.resolve(NAMESPACE);
	}

	public static Path texturesRoot(Path contentsRoot) {
		return assetsRoot(contentsRoot).resolve("textures");
	}

	public static Path armorIconsDir(Path contentsRoot) {
		return texturesRoot(contentsRoot).resolve("armor_icons");
	}

	public static Path armorLayersDir(Path contentsRoot) {
		return texturesRoot(contentsRoot).resolve("armor_layers");
	}

	public static Path itemTexturesDir(Path contentsRoot) {
		return texturesRoot(contentsRoot).resolve("item");
	}

	public static Path itemModelsDir(Path contentsRoot) {
		return assetsRoot(contentsRoot).resolve("models").resolve("item");
	}
}
