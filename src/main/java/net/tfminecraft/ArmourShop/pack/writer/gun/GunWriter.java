package net.tfminecraft.ArmourShop.pack.writer.gun;


import net.tfminecraft.ArmourShop.pack.model.PackKind;
import net.tfminecraft.ArmourShop.pack.model.PackPaths;
import net.tfminecraft.ArmourShop.pack.model.PackSubmission;
import net.tfminecraft.ArmourShop.pack.util.Model3dUtil;
import net.tfminecraft.ArmourShop.pack.util.YamlUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Writes gun skins: shared texture + carry/reload/aim models as IA items
 * (STONE_HOE / STONE_HOE / CROSSBOW) and appends GaG {@code skins.yml} with
 * {@code ia.tfmc_submissions:} ids.
 * Aim is one CROSSBOW item; {@code {slug}_aim_charged.json} is a copy of the
 * aim model so the loaded crossbow state uses the same look.
 */
public final class GunWriter {

	public static final String TEXTURE_STEM = Model3dUtil.TEXTURE_STEM;
	public static final String CARRY_STEM = "carry";
	public static final String RELOAD_STEM = "reload";
	public static final String AIM_STEM = "aim";

	/** IA loaded-with-arrow suffix for the aim CROSSBOW item. */
	public static final String AIM_CHARGED_SUFFIX = "_charged";

	public static final String[] MODEL_STEMS = {
		CARRY_STEM, RELOAD_STEM, AIM_STEM
	};

	private GunWriter() {}

	public static List<Path> write(
		Path contentsRoot,
		PackSubmission submission,
		String baseSet,
		Path skinsYml
	) throws IOException {
		if (submission.kind() != PackKind.GUN) {
			throw new IllegalArgumentException(
				"GunWriter requires GUN, got " + submission.kind()
			);
		}
		if (skinsYml == null) {
			throw new IllegalArgumentException("skinsYml is null");
		}

		String slug = submission.slug();
		YamlUtil.validateSlug(slug);

		Path modelsDir = PackPaths.itemModelsDir(contentsRoot);
		Path itemTexDir = PackPaths.itemTexturesDir(contentsRoot);
		Path configsDir = PackPaths.configsDir(contentsRoot);
		Files.createDirectories(modelsDir);
		Files.createDirectories(itemTexDir);
		Files.createDirectories(configsDir);

		List<Path> written = new ArrayList<>();

		Path pngPath = itemTexDir.resolve(slug + ".png");
		Files.write(pngPath, submission.requireFile(TEXTURE_STEM));
		written.add(pngPath);

		byte[] aimNormalized = null;
		for (String stem : MODEL_STEMS) {
			byte[] normalized = Model3dUtil.normalizeModel(
				submission.requireFile(stem),
				slug
			);
			Path modelPath = modelsDir.resolve(slug + "_" + stem + ".json");
			Files.write(modelPath, normalized);
			written.add(modelPath);
			if (AIM_STEM.equals(stem)) {
				aimNormalized = normalized;
			}
		}
		if (aimNormalized != null) {
			Path chargedPath = modelsDir.resolve(
				slug + "_" + AIM_STEM + AIM_CHARGED_SUFFIX + ".json"
			);
			Files.write(chargedPath, aimNormalized);
			written.add(chargedPath);
		}

		Path yamlPath = configsDir.resolve(slug + ".yml");
		Files.writeString(yamlPath, buildYaml(submission), StandardCharsets.UTF_8);
		written.add(yamlPath);

		GunsSkinsYml.upsert(skinsYml, slug, baseSet);
		return List.copyOf(written);
	}

	/**
	 * Best-effort delete of gun pack files and skins.yml key.
	 */
	public static List<Path> remove(Path contentsRoot, String slug, Path skinsYml)
		throws IOException
	{
		String s = slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT);
		if (s.isEmpty()) {
			throw new IllegalArgumentException("slug is blank");
		}
		List<Path> removed = new ArrayList<>();

		deleteQuiet(
			PackPaths.configsDir(contentsRoot).resolve(s + ".yml"),
			removed
		);
		deleteQuiet(
			PackPaths.itemTexturesDir(contentsRoot).resolve(s + ".png"),
			removed
		);
		Path modelsDir = PackPaths.itemModelsDir(contentsRoot);
		for (String stem : MODEL_STEMS) {
			deleteQuiet(modelsDir.resolve(s + "_" + stem + ".json"), removed);
		}
		deleteQuiet(
			modelsDir.resolve(s + "_" + AIM_STEM + AIM_CHARGED_SUFFIX + ".json"),
			removed
		);
		if (skinsYml != null) {
			GunsSkinsYml.remove(skinsYml, s);
		}
		return removed;
	}

	static String buildYaml(PackSubmission submission) {
		String slug = submission.slug();
		String name = YamlUtil.escapeDoubleQuoted(submission.displayName());
		StringBuilder sb = new StringBuilder();
		sb.append("info:\n");
		sb.append("  namespace: ").append(PackPaths.NAMESPACE).append('\n');
		sb.append("items:\n");
		appendItem(sb, slug, name, CARRY_STEM, "STONE_HOE");
		appendItem(sb, slug, name, RELOAD_STEM, "STONE_HOE");
		appendItem(sb, slug, name, AIM_STEM, "CROSSBOW");
		return sb.toString();
	}

	private static void appendItem(
		StringBuilder sb,
		String slug,
		String displayName,
		String stem,
		String material
	) {
		String itemId = slug + "_" + stem;
		sb.append("  ").append(itemId).append(":\n");
		sb.append("    display_name: \"").append(displayName).append("\"\n");
		sb.append("    permission: ").append(slug).append('\n');
		sb.append("    resource:\n");
		sb.append("      material: ").append(material).append('\n');
		sb.append("      generate: false\n");
		sb.append("      model_path: item/").append(itemId).append('\n');
	}

	private static void deleteQuiet(Path path, List<Path> removed) throws IOException {
		if (Files.deleteIfExists(path)) {
			removed.add(path);
		}
	}
}
