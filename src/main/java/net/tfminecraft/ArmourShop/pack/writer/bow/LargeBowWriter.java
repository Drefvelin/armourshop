package net.tfminecraft.ArmourShop.pack.writer.bow;


import net.tfminecraft.ArmourShop.pack.model.BowFrames;
import net.tfminecraft.ArmourShop.pack.model.PackKind;
import net.tfminecraft.ArmourShop.pack.model.PackPaths;
import net.tfminecraft.ArmourShop.pack.model.PackSubmission;
import net.tfminecraft.ArmourShop.pack.util.YamlUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes large_bow skins (generate: false + enlarged bow display + pull models).
 */
public final class LargeBowWriter {

	/** Locked display from TFMC longbow oak_1 (enlarged vs vanilla bow). */
	private static final String DISPLAY_JSON =
		"  \"display\": {\n"
			+ "    \"thirdperson_righthand\": {\n"
			+ "      \"rotation\": [ -80, 260, -40 ],\n"
			+ "      \"translation\": [ -1, -2, 5.8 ],\n"
			+ "      \"scale\": [ 1.8, 1.8, 0.9 ]\n"
			+ "    },\n"
			+ "    \"thirdperson_lefthand\": {\n"
			+ "      \"rotation\": [ -80, -280, 40 ],\n"
			+ "      \"translation\": [ -1, -2, 5.8 ],\n"
			+ "      \"scale\": [ 1.8, 1.8, 0.9 ]\n"
			+ "    },\n"
			+ "    \"firstperson_righthand\": {\n"
			+ "      \"rotation\": [ 0, -90, 25 ],\n"
			+ "      \"translation\": [ 1.13, 3.2, 1.13 ],\n"
			+ "      \"scale\": [ 1.3, 1.3, 0.68 ]\n"
			+ "    },\n"
			+ "    \"firstperson_lefthand\": {\n"
			+ "      \"rotation\": [ 0, 90, -25 ],\n"
			+ "      \"translation\": [ 1.13, 3.2, 1.13 ],\n"
			+ "      \"scale\": [ 1.3, 1.3, 0.68 ]\n"
			+ "    }\n"
			+ "  }";

	private LargeBowWriter() {}

	public static List<Path> write(Path contentsRoot, PackSubmission submission)
		throws IOException
	{
		if (submission.kind() != PackKind.LARGE_BOW) {
			throw new IllegalArgumentException(
				"LargeBowWriter requires LARGE_BOW, got " + submission.kind()
			);
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
		for (String stem : BowFrames.BOW_STEMS) {
			byte[] png = submission.requireFile(stem);
			Path pngPath = itemTexDir.resolve(BowFrames.textureFileName(slug, stem));
			Files.write(pngPath, png);
			written.add(pngPath);

			String modelName = slug + BowFrames.fileSuffix(stem) + ".json";
			Path modelPath = modelsDir.resolve(modelName);
			Files.writeString(
				modelPath,
				buildThinModel(slug, stem),
				StandardCharsets.UTF_8
			);
			written.add(modelPath);
		}

		Path yamlPath = configsDir.resolve(slug + ".yml");
		Files.writeString(yamlPath, buildYaml(submission), StandardCharsets.UTF_8);
		written.add(yamlPath);
		return written;
	}

	static String buildThinModel(String slug, String stem) {
		String tex = PackPaths.NAMESPACE + ":item/" + slug + BowFrames.fileSuffix(stem);
		return "{\n"
			+ "  \"parent\": \"minecraft:item/bow\",\n"
			+ DISPLAY_JSON + ",\n"
			+ "  \"textures\": {\n"
			+ "    \"layer0\": \"" + tex + "\"\n"
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
		sb.append("      material: BOW\n");
		sb.append("      generate: false\n");
		sb.append("      model_path: item/").append(slug).append('\n');
		return sb.toString();
	}
}
