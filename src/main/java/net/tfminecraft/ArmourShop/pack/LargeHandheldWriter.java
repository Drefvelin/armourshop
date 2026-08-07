package net.tfminecraft.ArmourShop.pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes large_handheld skins (generate: false + grip template parent).
 */
public final class LargeHandheldWriter {

	public static final String TEXTURE_STEM = "texture";

	private LargeHandheldWriter() {}

	public static List<Path> write(Path contentsRoot, PackSubmission submission)
		throws IOException
	{
		if (submission.kind() != PackKind.LARGE_HANDHELD) {
			throw new IllegalArgumentException(
				"LargeHandheldWriter requires LARGE_HANDHELD, got " + submission.kind()
			);
		}
		GripPreset grip = submission.gripPreset();
		if (grip == null) {
			throw new IllegalArgumentException("gripPreset is required");
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
		written.addAll(ensureGripTemplates(contentsRoot));

		Path pngPath = itemTexDir.resolve(slug + ".png");
		Files.write(pngPath, submission.requireFile(TEXTURE_STEM));
		written.add(pngPath);

		Path thinModel = modelsDir.resolve(slug + ".json");
		Files.writeString(thinModel, buildThinModel(slug, grip), StandardCharsets.UTF_8);
		written.add(thinModel);

		Path yamlPath = configsDir.resolve(slug + ".yml");
		Files.writeString(yamlPath, buildYaml(submission), StandardCharsets.UTF_8);
		written.add(yamlPath);
		return written;
	}

	/**
	 * Copy grip templates from classpath into the namespace models folder if missing.
	 */
	public static List<Path> ensureGripTemplates(Path contentsRoot) throws IOException {
		Path modelsDir = PackPaths.itemModelsDir(contentsRoot);
		Files.createDirectories(modelsDir);
		List<Path> ensured = new ArrayList<>();
		for (GripPreset grip : GripPreset.values()) {
			Path dest = modelsDir.resolve(grip.modelFileName());
			if (!Files.isRegularFile(dest)) {
				String resource = "pack/grip_templates/" + grip.modelFileName();
				try (InputStream in = LargeHandheldWriter.class
					.getClassLoader()
					.getResourceAsStream(resource)) {
					if (in == null) {
						throw new IOException("Missing classpath resource: " + resource);
					}
					Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
				}
			}
			ensured.add(dest);
		}
		return ensured;
	}

	static String buildThinModel(String slug, GripPreset grip) {
		return "{\n"
			+ "  \"parent\": \"" + grip.parentModelPath() + "\",\n"
			+ "  \"textures\": {\n"
			+ "    \"layer0\": \"" + PackPaths.NAMESPACE + ":item/" + slug + "\"\n"
			+ "  }\n"
			+ "}\n";
	}

	static String buildYaml(PackSubmission submission) {
		String slug = submission.slug();
		String name = YamlUtil.escapeDoubleQuoted(submission.displayName());
		StringBuilder sb = new StringBuilder();
		sb.append("info:\n");
		sb.append("  namespace: ").append(PackPaths.NAMESPACE).append('\n');
		sb.append("items:\n");
		sb.append("  ").append(slug).append(":\n");
		sb.append("    display_name: \"").append(name).append("\"\n");
		sb.append("    permission: ").append(slug).append('\n');
		sb.append("    resource:\n");
		sb.append("      material: PAPER\n");
		sb.append("      generate: false\n");
		sb.append("      model_path: item/").append(slug).append('\n');
		return sb.toString();
	}
}
